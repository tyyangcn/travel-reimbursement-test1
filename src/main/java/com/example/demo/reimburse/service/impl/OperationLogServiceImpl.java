package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.entity.OperationLogEntity;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.mapper.OperationLogMapper;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.service.OperationLogService;
import com.example.demo.reimburse.vo.OperationLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    private static final int DELETED = 1;
    private static final String RESULT_SUCCESS = "SUCCESS";

    private final OperationLogMapper operationLogMapper;
    private final ReimbursementMapper reimbursementMapper;

    public OperationLogServiceImpl(
            OperationLogMapper operationLogMapper,
            ReimbursementMapper reimbursementMapper) {
        this.operationLogMapper = operationLogMapper;
        this.reimbursementMapper = reimbursementMapper;
    }

    @Override
    public void record(
            ReimbursementEntity reimbursement,
            String operationType,
            String operationContent) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setId(createId());
        entity.setReimId(reimbursement.getId());
        entity.setReimNo(reimbursement.getReimNo());
        entity.setOperationType(operationType);
        entity.setOperatorId(reimbursement.getCreateUserId());
        entity.setOperatorName(reimbursement.getCreateUserName());
        entity.setOperationResult(RESULT_SUCCESS);
        entity.setOperationContent(operationContent);
        entity.setOperationTime(LocalDateTime.now());

        if (operationLogMapper.insert(entity) != 1) {
            throw new IllegalStateException("新增操作日志失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogVO> listByReimId(String reimId) {
        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);
        if (reimbursement == null
                || Integer.valueOf(DELETED).equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        List<OperationLogEntity> entityList =
                operationLogMapper.selectList(
                        new LambdaQueryWrapper<OperationLogEntity>()
                                .eq(OperationLogEntity::getReimId, reimId)
                                .orderByDesc(
                                        OperationLogEntity::getOperationTime
                                )
                );
        List<OperationLogVO> result =
                new ArrayList<>(entityList.size());
        for (OperationLogEntity entity : entityList) {
            OperationLogVO vo = new OperationLogVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setOperationTypeName(
                    getOperationTypeName(entity.getOperationType())
            );
            result.add(vo);
        }
        return result;
    }

    private String getOperationTypeName(String operationType) {
        return switch (operationType) {
            case "CREATE" -> "新增";
            case "UPDATE" -> "修改";
            case "SUBMIT" -> "提交";
            case "VOID" -> "作废";
            case "DELETE" -> "删除";
            case "COPY" -> "复制";
            default -> operationType;
        };
    }

    private String createId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}
