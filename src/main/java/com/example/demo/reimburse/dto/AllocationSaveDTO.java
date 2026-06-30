package com.example.demo.reimburse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllocationSaveDTO {

    @NotBlank(message = "费用归属公司ID不能为空")
    private String companyId;

    @NotBlank(message = "费用归属公司编号不能为空")
    private String companyNo;

    @NotBlank(message = "费用归属公司名称不能为空")
    private String companyName;

    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    @NotBlank(message = "项目编号不能为空")
    private String projectNo;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotNull(message = "分摊比例不能为空")
    @DecimalMin(value = "0.0000", message = "分摊比例不能小于0")
    @DecimalMax(value = "1.0000", message = "分摊比例不能大于1")
    private BigDecimal allocationRatio;

    @NotNull(message = "分摊金额不能为空")
    @DecimalMin(value = "0.00", message = "分摊金额不能小于0")
    private BigDecimal allocationAmount;
}