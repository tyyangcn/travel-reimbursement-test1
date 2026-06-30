package com.example.demo.reimburse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReimbursementDetailVO {

    private String id;
    private String reimNo;
    private LocalDate documentDate;
    private String reimTitle;
    private String travelReason;

    private String reimUserId;
    private String reimUserNo;
    private String reimUserName;

    private String reimDeptId;
    private String reimDeptNo;
    private String reimDeptName;

    private String companyId;
    private String companyNo;
    private String companyName;

    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;

    private BigDecimal mealAllowanceAmount;
    private BigDecimal trafficAllowanceAmount;
    private BigDecimal communicationAllowanceAmount;
    private BigDecimal totalAllowanceAmount;

    private Integer status;
    private String statusName;
    private String remark;

    private String createUserName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}