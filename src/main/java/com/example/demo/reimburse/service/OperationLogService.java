package com.example.demo.reimburse.service;

import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.vo.OperationLogVO;

import java.util.List;

public interface OperationLogService {
    void record(
            ReimbursementEntity reimbursement,
            String operationType,
            String operationContent
    );

    List<OperationLogVO> listByReimId(String reimId);
}
