package com.example.demo.reimburse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReimbursementUpdateDTO {

    @NotNull(message = "单据日期不能为空")
    private LocalDate documentDate;

    @NotBlank(message = "报销标题不能为空")
    @Size(max = 500, message = "报销标题不能超过500个字符")
    private String reimTitle;

    @NotBlank(message = "出差事由不能为空")
    @Size(max = 500, message = "出差事由不能超过500个字符")
    private String travelReason;

    @NotBlank(message = "报销人ID不能为空")
    @Size(max = 32, message = "报销人ID不能超过32个字符")
    private String reimUserId;

    @NotBlank(message = "报销人工号不能为空")
    @Size(max = 50, message = "报销人工号不能超过50个字符")
    private String reimUserNo;

    @NotBlank(message = "报销人姓名不能为空")
    @Size(max = 100, message = "报销人姓名不能超过100个字符")
    private String reimUserName;

    @NotBlank(message = "报销部门ID不能为空")
    @Size(max = 32, message = "报销部门ID不能超过32个字符")
    private String reimDeptId;

    @NotBlank(message = "报销部门编号不能为空")
    @Size(max = 50, message = "报销部门编号不能超过50个字符")
    private String reimDeptNo;

    @NotBlank(message = "报销部门名称不能为空")
    @Size(max = 200, message = "报销部门名称不能超过200个字符")
    private String reimDeptName;

    @NotBlank(message = "费用归属公司ID不能为空")
    @Size(max = 32, message = "费用归属公司ID不能超过32个字符")
    private String companyId;

    @NotBlank(message = "费用归属公司编号不能为空")
    @Size(max = 50, message = "费用归属公司编号不能超过50个字符")
    private String companyNo;

    @NotBlank(message = "费用归属公司名称不能为空")
    @Size(max = 200, message = "费用归属公司名称不能超过200个字符")
    private String companyName;

    @NotBlank(message = "业务类型ID不能为空")
    @Size(max = 32, message = "业务类型ID不能超过32个字符")
    private String businessTypeId;

    @NotBlank(message = "业务类型编号不能为空")
    @Size(max = 50, message = "业务类型编号不能超过50个字符")
    private String businessTypeNo;

    @NotBlank(message = "业务类型名称不能为空")
    @Size(max = 200, message = "业务类型名称不能超过200个字符")
    private String businessTypeName;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}