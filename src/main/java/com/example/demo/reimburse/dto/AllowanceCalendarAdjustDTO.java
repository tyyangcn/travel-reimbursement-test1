package com.example.demo.reimburse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日补助调整参数。
 */
@Data
public class AllowanceCalendarAdjustDTO {

    @NotBlank(message = "补助日历ID不能为空")
    private String calendarId;

    @NotNull(message = "餐费补助选择状态不能为空")
    @Min(value = 0, message = "餐费补助选择状态只能是0或1")
    @Max(value = 1, message = "餐费补助选择状态只能是0或1")
    private Integer mealSelected;

    @NotNull(message = "餐费补助金额不能为空")
    @DecimalMin(value = "0.00", message = "餐费补助金额不能小于0")
    private BigDecimal mealActualAmount;

    @NotNull(message = "交通补助选择状态不能为空")
    @Min(value = 0, message = "交通补助选择状态只能是0或1")
    @Max(value = 1, message = "交通补助选择状态只能是0或1")
    private Integer trafficSelected;

    @NotNull(message = "交通补助金额不能为空")
    @DecimalMin(value = "0.00", message = "交通补助金额不能小于0")
    private BigDecimal trafficActualAmount;

    @NotNull(message = "通讯补助选择状态不能为空")
    @Min(value = 0, message = "通讯补助选择状态只能是0或1")
    @Max(value = 1, message = "通讯补助选择状态只能是0或1")
    private Integer communicationSelected;

    @NotNull(message = "通讯补助金额不能为空")
    @DecimalMin(value = "0.00", message = "通讯补助金额不能小于0")
    private BigDecimal communicationActualAmount;
}
