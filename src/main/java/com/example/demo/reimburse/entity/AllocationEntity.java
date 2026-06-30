package com.example.demo.reimburse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fk_reim_allocation")
public class AllocationEntity {

    @TableId
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

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}