package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.dto.AllocationSaveDTO;
import com.example.demo.reimburse.dto.ReimbursementCreateDTO;
import com.example.demo.reimburse.dto.ReimbursementQueryDTO;
import com.example.demo.reimburse.dto.ReimbursementUpdateDTO;
import com.example.demo.reimburse.dto.TripCreateDTO;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.service.AllocationService;
import com.example.demo.reimburse.service.AllowanceService;
import com.example.demo.reimburse.service.OperationLogService;
import com.example.demo.reimburse.service.ReimbursementService;
import com.example.demo.reimburse.service.TripService;
import com.example.demo.reimburse.vo.AllocationVO;
import com.example.demo.reimburse.vo.AllowanceVO;
import com.example.demo.reimburse.vo.ReimbursementDetailVO;
import com.example.demo.reimburse.vo.ReimbursementPageVO;
import com.example.demo.reimburse.vo.TripVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {
    private static final String REIM_NO_PREFIX = "RCBX";
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_UNDER_APPROVAL = 1;
    private static final int STATUS_VOIDED = 2;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private static final int MAX_TITLE_LENGTH = 500;
    private static final String COPY_TITLE_SUFFIX = " - 副本";

    private static final DateTimeFormatter REIM_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ReimbursementMapper reimbursementMapper;
    private final TripService tripService;
    private final AllocationService allocationService;
    private final AllowanceService allowanceService;
    private final OperationLogService operationLogService;

    public ReimbursementServiceImpl(
            ReimbursementMapper reimbursementMapper,
            TripService tripService,
            AllocationService allocationService,
            AllowanceService allowanceService,
            OperationLogService operationLogService) {
        this.reimbursementMapper = reimbursementMapper;
        this.tripService = tripService;
        this.allocationService = allocationService;
        this.allowanceService = allowanceService;
        this.operationLogService = operationLogService;
    }
    @Override
    public IPage<ReimbursementPageVO> page(ReimbursementQueryDTO queryDTO) {
        Page<ReimbursementPageVO> page = new Page<>(
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
        return reimbursementMapper.selectPageByCondition(page, queryDTO);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(ReimbursementCreateDTO createDTO) {
        LocalDateTime now = LocalDateTime.now();
        String id = UUID.randomUUID()
                .toString()
                .replace("-", "");
        ReimbursementEntity entity = new ReimbursementEntity();
        entity.setId(id);
        entity.setReimNo(generateReimNo(now, id));
        entity.setDocumentDate(createDTO.getDocumentDate());
        entity.setReimTitle(createDTO.getReimTitle());
        entity.setTravelReason(createDTO.getTravelReason());

        entity.setReimUserId(createDTO.getReimUserId());
        entity.setReimUserNo(createDTO.getReimUserNo());
        entity.setReimUserName(createDTO.getReimUserName());

        entity.setReimDeptId(createDTO.getReimDeptId());
        entity.setReimDeptNo(createDTO.getReimDeptNo());
        entity.setReimDeptName(createDTO.getReimDeptName());

        entity.setCompanyId(createDTO.getCompanyId());
        entity.setCompanyNo(createDTO.getCompanyNo());
        entity.setCompanyName(createDTO.getCompanyName());

        entity.setBusinessTypeId(createDTO.getBusinessTypeId());
        entity.setBusinessTypeNo(createDTO.getBusinessTypeNo());
        entity.setBusinessTypeName(createDTO.getBusinessTypeName());

        entity.setMealAllowanceAmount(BigDecimal.ZERO);
        entity.setTrafficAllowanceAmount(BigDecimal.ZERO);
        entity.setCommunicationAllowanceAmount(BigDecimal.ZERO);
        entity.setTotalAllowanceAmount(BigDecimal.ZERO);

        entity.setStatus(STATUS_DRAFT);
        entity.setDeleted(NOT_DELETED);
        entity.setRemark(createDTO.getRemark());

        // 暂无登录系统，暂时约定创建人就是报销人
        entity.setCreateUserId(createDTO.getReimUserId());
        entity.setCreateUserName(createDTO.getReimUserName());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        int affectedRows = reimbursementMapper.insert(entity);
        if (affectedRows != 1) {
            throw new IllegalStateException("新增报销单失败");
        }
        operationLogService.record(entity, "CREATE", "新增报销单");
        return id;
    }

    @Override
    public ReimbursementDetailVO getById(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity entity =
                reimbursementMapper.selectById(id);

        if (entity == null
                || Integer.valueOf(DELETED).equals(entity.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        ReimbursementDetailVO detailVO =
                new ReimbursementDetailVO();

        BeanUtils.copyProperties(entity, detailVO);
        detailVO.setStatusName(
                getStatusName(entity.getStatus())
        );

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(
            String id,
            ReimbursementUpdateDTO updateDTO) {

        if (id == null || id.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity existingEntity =
                reimbursementMapper.selectById(id);

        if (existingEntity == null
                || Integer.valueOf(DELETED)
                .equals(existingEntity.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT)
                .equals(existingEntity.getStatus())) {
            throw new BusinessException(
                    400,
                    "只有草稿状态的报销单允许修改"
            );
        }

        ReimbursementEntity updateEntity =
                new ReimbursementEntity();

        updateEntity.setId(id);
        updateEntity.setDocumentDate(updateDTO.getDocumentDate());
        updateEntity.setReimTitle(updateDTO.getReimTitle());
        updateEntity.setTravelReason(updateDTO.getTravelReason());

        updateEntity.setReimUserId(updateDTO.getReimUserId());
        updateEntity.setReimUserNo(updateDTO.getReimUserNo());
        updateEntity.setReimUserName(updateDTO.getReimUserName());

        updateEntity.setReimDeptId(updateDTO.getReimDeptId());
        updateEntity.setReimDeptNo(updateDTO.getReimDeptNo());
        updateEntity.setReimDeptName(updateDTO.getReimDeptName());

        updateEntity.setCompanyId(updateDTO.getCompanyId());
        updateEntity.setCompanyNo(updateDTO.getCompanyNo());
        updateEntity.setCompanyName(updateDTO.getCompanyName());

        updateEntity.setBusinessTypeId(updateDTO.getBusinessTypeId());
        updateEntity.setBusinessTypeNo(updateDTO.getBusinessTypeNo());
        updateEntity.setBusinessTypeName(updateDTO.getBusinessTypeName());

        updateEntity.setRemark(updateDTO.getRemark());
        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                reimbursementMapper.updateById(updateEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("修改报销单失败");
        }
        operationLogService.record(existingEntity, "UPDATE", "修改报销单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity existingEntity =
                reimbursementMapper.selectById(id);

        if (existingEntity == null
                || Integer.valueOf(DELETED).equals(existingEntity.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT)
                .equals(existingEntity.getStatus())) {
            throw new BusinessException(
                    400,
                    "只有草稿状态的报销单允许提交"
            );
        }

        validateBeforeSubmit(existingEntity);

        ReimbursementEntity updateEntity = new ReimbursementEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(STATUS_UNDER_APPROVAL);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                reimbursementMapper.updateById(updateEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("提交报销单失败");
        }
        operationLogService.record(existingEntity, "SUBMIT", "提交报销单");
    }

    private void validateBeforeSubmit(
            ReimbursementEntity reimbursement) {
        List<TripVO> tripList =
                tripService.listByReimId(reimbursement.getId());
        if (tripList.isEmpty()) {
            throw new BusinessException(
                    400,
                    "请至少补录一条行程后再提交"
            );
        }

        List<AllowanceVO> allowanceList =
                allowanceService.listByReimId(reimbursement.getId());
        if (allowanceList.size() != tripList.size()) {
            throw new BusinessException(
                    400,
                    "部分行程尚未生成补助，请检查后再提交"
            );
        }

        List<AllocationVO> allocationList =
                allocationService.listByReimId(reimbursement.getId());
        if (allocationList.isEmpty()) {
            throw new BusinessException(
                    400,
                    "请完成费用分摊后再提交"
            );
        }

        BigDecimal ratioTotal = allocationList.stream()
                .map(AllocationVO::getAllocationRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (ratioTotal.compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(
                    400,
                    "费用分摊比例合计必须等于100%"
            );
        }

        BigDecimal amountTotal = allocationList.stream()
                .map(AllocationVO::getAllocationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amountTotal.compareTo(
                reimbursement.getTotalAllowanceAmount()
        ) != 0) {
            throw new BusinessException(
                    400,
                    "费用分摊金额合计必须等于补助总金额"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidReimbursement(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity existingEntity =
                reimbursementMapper.selectById(id);

        if (existingEntity == null
                || Integer.valueOf(DELETED).equals(existingEntity.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (Integer.valueOf(STATUS_VOIDED).equals(existingEntity.getStatus())) {
            throw new BusinessException(400, "报销单已作废，请勿重复操作");
        }

        ReimbursementEntity updateEntity = new ReimbursementEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(STATUS_VOIDED);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                reimbursementMapper.updateById(updateEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("作废报销单失败");
        }
        operationLogService.record(existingEntity, "VOID", "作废报销单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReimbursement(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity existingEntity =
                reimbursementMapper.selectById(id);

        if (existingEntity == null
                || Integer.valueOf(DELETED).equals(existingEntity.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT).equals(existingEntity.getStatus())) {
            throw new BusinessException(400, "只有草稿状态的报销单允许删除");
        }

        ReimbursementEntity updateEntity = new ReimbursementEntity();
        updateEntity.setId(id);
        updateEntity.setDeleted(DELETED);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                reimbursementMapper.updateById(updateEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("删除报销单失败");
        }
        operationLogService.record(existingEntity, "DELETE", "删除报销单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copyReimbursement(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity existingEntity =
                reimbursementMapper.selectById(id);

        if (existingEntity == null
                || Integer.valueOf(DELETED).equals(existingEntity.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        String newReimId = create(buildCopyCreateDTO(existingEntity));

        List<TripVO> tripList = tripService.listByReimId(id);
        for (TripVO tripVO : tripList) {
            TripCreateDTO tripCreateDTO = new TripCreateDTO();
            BeanUtils.copyProperties(tripVO, tripCreateDTO);
            tripService.create(newReimId, tripCreateDTO);
        }

        List<AllocationVO> allocationList =
                allocationService.listByReimId(id);
        if (!allocationList.isEmpty()) {
            ReimbursementEntity copiedEntity =
                    reimbursementMapper.selectById(newReimId);
            allocationService.save(
                    newReimId,
                    buildCopyAllocationList(
                            allocationList,
                            copiedEntity.getTotalAllowanceAmount()
                    )
            );
        }

        ReimbursementEntity copiedEntity =
                reimbursementMapper.selectById(newReimId);
        operationLogService.record(
                copiedEntity,
                "COPY",
                "复制自报销单：" + existingEntity.getReimNo()
        );
        return newReimId;
    }

    private ReimbursementCreateDTO buildCopyCreateDTO(
            ReimbursementEntity sourceEntity) {
        ReimbursementCreateDTO createDTO = new ReimbursementCreateDTO();
        createDTO.setDocumentDate(LocalDate.now());
        createDTO.setReimTitle(buildCopyTitle(sourceEntity.getReimTitle()));
        createDTO.setTravelReason(sourceEntity.getTravelReason());
        createDTO.setReimUserId(sourceEntity.getReimUserId());
        createDTO.setReimUserNo(sourceEntity.getReimUserNo());
        createDTO.setReimUserName(sourceEntity.getReimUserName());
        createDTO.setReimDeptId(sourceEntity.getReimDeptId());
        createDTO.setReimDeptNo(sourceEntity.getReimDeptNo());
        createDTO.setReimDeptName(sourceEntity.getReimDeptName());
        createDTO.setCompanyId(sourceEntity.getCompanyId());
        createDTO.setCompanyNo(sourceEntity.getCompanyNo());
        createDTO.setCompanyName(sourceEntity.getCompanyName());
        createDTO.setBusinessTypeId(sourceEntity.getBusinessTypeId());
        createDTO.setBusinessTypeNo(sourceEntity.getBusinessTypeNo());
        createDTO.setBusinessTypeName(sourceEntity.getBusinessTypeName());
        createDTO.setRemark(sourceEntity.getRemark());
        return createDTO;
    }

    private List<AllocationSaveDTO> buildCopyAllocationList(
            List<AllocationVO> sourceList,
            BigDecimal totalAmount) {
        List<AllocationSaveDTO> result =
                new ArrayList<>(sourceList.size());
        BigDecimal allocatedAmount = BigDecimal.ZERO;

        for (int index = 0; index < sourceList.size(); index++) {
            AllocationVO source = sourceList.get(index);
            AllocationSaveDTO target = new AllocationSaveDTO();
            BeanUtils.copyProperties(source, target);

            boolean isLast = index == sourceList.size() - 1;
            BigDecimal amount = isLast
                    ? totalAmount.subtract(allocatedAmount)
                    : totalAmount
                    .multiply(source.getAllocationRatio())
                    .setScale(2, RoundingMode.HALF_UP);

            target.setAllocationAmount(amount);
            allocatedAmount = allocatedAmount.add(amount);
            result.add(target);
        }

        return result;
    }

    private String buildCopyTitle(String sourceTitle) {
        int sourceMaxLength =
                MAX_TITLE_LENGTH - COPY_TITLE_SUFFIX.length();
        String normalizedTitle =
                sourceTitle.length() > sourceMaxLength
                        ? sourceTitle.substring(0, sourceMaxLength)
                        : sourceTitle;
        return normalizedTitle + COPY_TITLE_SUFFIX;
    }

    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }

        return switch (status) {
            case STATUS_DRAFT -> "草稿";
            case STATUS_UNDER_APPROVAL -> "审批中";
            case STATUS_VOIDED -> "已作废";
            default -> "未知";
        };
    }


    private String generateReimNo(LocalDateTime now, String id) {
        return REIM_NO_PREFIX
                + now.format(REIM_NO_FORMATTER)
                + id.substring(0, 4);
    }
}
