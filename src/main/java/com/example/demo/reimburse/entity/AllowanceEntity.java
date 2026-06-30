package com.example.demo.reimburse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fk_reim_allowance")
public class AllowanceEntity {

    @TableId
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
    private BigDecimal standardTotalAmount;
    private BigDecimal applyAmount;
    private BigDecimal allowanceAmount;
    private Integer sortNo;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}