package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.dto.AllocationSaveDTO;
import com.example.demo.reimburse.entity.AllocationEntity;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.mapper.AllocationMapper;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.service.AllocationService;
import com.example.demo.reimburse.vo.AllocationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AllocationServiceImpl implements AllocationService {

    private static final int STATUS_DRAFT = 0;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;

    private final AllocationMapper allocationMapper;
    private final ReimbursementMapper reimbursementMapper;

    public AllocationServiceImpl(
            AllocationMapper allocationMapper,
            ReimbursementMapper reimbursementMapper) {
        this.allocationMapper = allocationMapper;
        this.reimbursementMapper = reimbursementMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(String reimId, List<AllocationSaveDTO> allocationList) {
        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity reimbursement = reimbursementMapper.selectById(reimId);
        if (reimbursement == null || Integer.valueOf(DELETED).equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT).equals(reimbursement.getStatus())) {
            throw new BusinessException(400, "只有草稿报销单可以维护费用分摊");
        }

        if (allocationList == null) {
            throw new BusinessException(400, "费用分摊不能为空");
        }

        deleteByReimId(reimId);

        BigDecimal ratioTotal = BigDecimal.ZERO;
        BigDecimal amountTotal = BigDecimal.ZERO;
        int sortNo = 1;

        for (AllocationSaveDTO dto : allocationList) {
            if (dto == null) {
                throw new BusinessException(400, "费用分摊明细不能为空");
            }

            if (dto.getAllocationRatio() == null) {
                throw new BusinessException(400, "分摊比例不能为空");
            }

            if (dto.getAllocationAmount() == null) {
                throw new BusinessException(400, "分摊金额不能为空");
            }

            ratioTotal = ratioTotal.add(dto.getAllocationRatio());
            amountTotal = amountTotal.add(dto.getAllocationAmount());

            AllocationEntity entity = new AllocationEntity();
            BeanUtils.copyProperties(dto, entity);

            entity.setId(createId());
            entity.setReimId(reimId);
            entity.setSortNo(sortNo++);
            entity.setDeleted(NOT_DELETED);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            if (allocationMapper.insert(entity) != 1) {
                throw new IllegalStateException("保存费用分摊失败");
            }
        }

        if (ratioTotal.compareTo(new BigDecimal("1.0000")) != 0) {
            throw new BusinessException(400, "费用分摊比例合计必须等于1");
        }

        if (amountTotal.compareTo(reimbursement.getTotalAllowanceAmount()) != 0) {
            throw new BusinessException(400, "费用分摊金额合计必须等于报销单总金额");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationVO> listByReimId(String reimId) {
        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity reimbursement = reimbursementMapper.selectById(reimId);
        if (reimbursement == null || Integer.valueOf(DELETED).equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        List<AllocationEntity> entities = allocationMapper.selectList(
                new LambdaQueryWrapper<AllocationEntity>()
                        .eq(AllocationEntity::getReimId, reimId)
                        .eq(AllocationEntity::getDeleted, NOT_DELETED)
                        .orderByAsc(AllocationEntity::getSortNo)
        );

        List<AllocationVO> result = new ArrayList<>();
        for (AllocationEntity entity : entities) {
            AllocationVO vo = new AllocationVO();
            BeanUtils.copyProperties(entity, vo);
            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByReimId(String reimId) {
        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        allocationMapper.delete(
                new LambdaQueryWrapper<AllocationEntity>()
                        .eq(AllocationEntity::getReimId, reimId)
        );
    }

    private String createId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
