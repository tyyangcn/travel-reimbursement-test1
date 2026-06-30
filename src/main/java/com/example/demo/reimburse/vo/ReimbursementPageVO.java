package com.example.demo.reimburse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReimbursementPageVO {
    private String id;
    private String reimNo;
    private String reimTitle;
    private String travelReason;
    private String companyName;
    private String reimDeptName;
    private String reimDeptNo;
    private String reimUserName;
    private String reimUserNo;
    private String businessTypeName;
    private Integer status;
    private String statusName;
    private BigDecimal totalAllowanceAmount;
    private LocalDateTime createTime;
}
