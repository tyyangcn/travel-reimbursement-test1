package com.example.demo.reimburse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fk_reim_operation_log")
public class OperationLogEntity {
    @TableId
    private String id;
    private String reimId;
    private String reimNo;
    private String operationType;
    private String operatorId;
    private String operatorName;
    private String operationResult;
    private String operationContent;
    private LocalDateTime operationTime;
    private String requestId;
    private String ipAddress;
}
