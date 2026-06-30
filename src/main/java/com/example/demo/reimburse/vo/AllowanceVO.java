package com.example.demo.reimburse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 补助汇总展示对象。
 */
@Data
public class AllowanceVO {

    private String id;
    private String reimId;
    private String tripId;

    private String travelerId;
    private String travelerName;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer allowanceDays;

    private String departureCityName;
    private String allowanceCityNo;
    private String allowanceCityName;
    private String cityType;

    /** 标准补助总金额。 */
    private BigDecimal standardTotalAmount;

    /** 申请金额。 */
    private BigDecimal applyAmount;

    /** 实际补助金额。 */
    private BigDecimal allowanceAmount;

    private Integer sortNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 每日补助日历明细。 */
    private List<AllowanceCalendarVO> calendarList;
}