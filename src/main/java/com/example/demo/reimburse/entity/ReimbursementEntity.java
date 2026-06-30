package com.example.demo.reimburse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fk_reim_main")
public class ReimbursementEntity {
    @TableId
    private String id;
    private String reimNo;//报销单编号
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
    private BigDecimal totalAllowanceAmount;//补助总金额
    private Integer status;//单据状态：0草稿，1审批中，2已作废
    private String remark;
    private Integer deleted;//删除标识：0未删除，1已删除
    private String createUserId;
    private String createUserName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
