package com.example.demo.reimburse.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {
    private String id;
    private String operationType;
    private String operationTypeName;
    private String operatorName;
    private String operationResult;
    private String operationContent;
    private LocalDateTime operationTime;
}
