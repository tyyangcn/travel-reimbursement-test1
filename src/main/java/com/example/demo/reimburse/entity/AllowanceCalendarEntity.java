package com.example.demo.reimburse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fk_reim_allowance_calendar")
public class AllowanceCalendarEntity {

    @TableId
    private String id;
    private String reimId;
    private String allowanceId;
    private String tripId;
    private LocalDate allowanceDate;
    private Integer weekdayNo;

    /** 是否选择餐费补助：0否，1是。 */
    private Integer mealSelected;
    private BigDecimal mealStandardAmount;
    private BigDecimal mealActualAmount;

    /** 是否选择交通补助：0否，1是。 */
    private Integer trafficSelected;
    private BigDecimal trafficStandardAmount;
    private BigDecimal trafficActualAmount;

    /** 是否选择通讯补助：0否，1是。 */
    private Integer communicationSelected;
    private BigDecimal communicationStandardAmount;
    private BigDecimal communicationActualAmount;

    /** 当日标准金额合计。 */
    private BigDecimal dailyStandardAmount;

    /** 当日实际金额合计。 */
    private BigDecimal dailyActualAmount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}