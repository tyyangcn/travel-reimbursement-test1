package com.example.demo.reimburse.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TripVO {
    private String id;
    private String reimId;

    private String travelerId;
    private String travelerNo;
    private String travelerName;

    private String departureCityNo;
    private String departureCityName;

    private String arrivalCityNo;
    private String arrivalCityName;
    private String arrivalCityType;

    private LocalDate departureDate;
    private LocalDate arrivalDate;

    private String tripDescription;
    private Integer sortNo;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}