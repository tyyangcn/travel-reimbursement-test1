package com.example.demo.reimburse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AllocationVO {

    private String id;

    private String reimId;

    private String companyId;

    private String companyNo;

    private String companyName;

    private String projectId;

    private String projectNo;

    private String projectName;

    private BigDecimal allocationRatio;

    private BigDecimal allocationAmount;

    private Integer sortNo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}