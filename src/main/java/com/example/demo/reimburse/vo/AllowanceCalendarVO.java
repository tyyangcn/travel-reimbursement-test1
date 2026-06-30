package com.example.demo.reimburse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AllowanceCalendarVO {

    private String id;
    private String allowanceId;
    private String tripId;

    /** 补助日期。 */
    private LocalDate allowanceDate;

    /** 星期序号：1至7。 */
    private Integer weekdayNo;

    /** 餐费是否选中。 */
    private Integer mealSelected;

    private BigDecimal mealStandardAmount;
    private BigDecimal mealActualAmount;

    /** 交通补助是否选中。 */
    private Integer trafficSelected;

    private BigDecimal trafficStandardAmount;
    private BigDecimal trafficActualAmount;

    /** 通讯补助是否选中。 */
    private Integer communicationSelected;

    private BigDecimal communicationStandardAmount;
    private BigDecimal communicationActualAmount;

    /** 当日标准补助合计。 */
    private BigDecimal dailyStandardAmount;

    /** 当日实际补助合计。 */
    private BigDecimal dailyActualAmount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}