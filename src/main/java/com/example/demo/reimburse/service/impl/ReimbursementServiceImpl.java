package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.dto.ReimbursementCreateDTO;
import com.example.demo.reimburse.dto.ReimbursementQueryDTO;
import com.example.demo.reimburse.dto.ReimbursementUpdateDTO;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.service.ReimbursementService;
import com.example.demo.reimburse.vo.ReimbursementDetailVO;
import com.example.demo.reimburse.vo.ReimbursementPageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {
    private static final String REIM_NO_PREFIX = "RCBX";
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_UNDER_APPROVAL = 1;
    private static final int STATUS_VOIDED = 2;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;

    private static final DateTimeFormatter REIM_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ReimbursementMapper reimbursementMapper;

    public ReimbursementServiceImpl(ReimbursementMapper reimbursementMapper) {
        this.reimbursementMapper = reimbursementMapper;
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

        ReimbursementEntity updateEntity = new ReimbursementEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(STATUS_UNDER_APPROVAL);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                reimbursementMapper.updateById(updateEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("提交报销单失败");
        }
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
