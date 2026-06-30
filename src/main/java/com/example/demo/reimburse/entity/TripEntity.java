package com.example.demo.reimburse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fk_reim_trip")
public class TripEntity {

    @TableId
    private String id;
    private String reimId;//所属报销单ID
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private String departureCityNo;
    private String departureCityName;//出发城市名称
    private String arrivalCityNo;//到达城市编码
    private String arrivalCityName;
    private String arrivalCityType;//到达城市类型，用于匹配补助标准
    private LocalDate departureDate;//出发日期
    private LocalDate arrivalDate;
    private String tripDescription;//行程说明
    private Integer sortNo;//行程排序号
    private Integer deleted;// 删除标识：0未删除，1已删除
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//修改时间
}