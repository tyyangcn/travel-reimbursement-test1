package com.example.demo.reimburse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TripUpdateDTO {

    @NotBlank(message = "出行人ID不能为空")
    @Size(max = 32, message = "出行人ID不能超过32个字符")
    private String travelerId;

    @NotBlank(message = "出行人工号不能为空")
    @Size(max = 50, message = "出行人工号不能超过50个字符")
    private String travelerNo;

    @NotBlank(message = "出行人姓名不能为空")
    @Size(max = 100, message = "出行人姓名不能超过100个字符")
    private String travelerName;

    @NotBlank(message = "出发城市编码不能为空")
    @Size(max = 50, message = "出发城市编码不能超过50个字符")
    private String departureCityNo;

    @NotBlank(message = "出发城市名称不能为空")
    @Size(max = 100, message = "出发城市名称不能超过100个字符")
    private String departureCityName;

    @NotBlank(message = "到达城市编码不能为空")
    @Size(max = 50, message = "到达城市编码不能超过50个字符")
    private String arrivalCityNo;

    @NotBlank(message = "到达城市名称不能为空")
    @Size(max = 100, message = "到达城市名称不能超过100个字符")
    private String arrivalCityName;

    @NotBlank(message = "到达城市类型不能为空")
    @Size(max = 10, message = "到达城市类型不能超过10个字符")
    private String arrivalCityType;

    @NotNull(message = "出发日期不能为空")
    private LocalDate departureDate;

    @NotNull(message = "到达日期不能为空")
    private LocalDate arrivalDate;

    @NotBlank(message = "行程说明不能为空")
    @Size(max = 500, message = "行程说明不能超过500个字符")
    private String tripDescription;
}

