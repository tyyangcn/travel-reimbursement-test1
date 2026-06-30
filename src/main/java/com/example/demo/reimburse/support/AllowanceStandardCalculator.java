package com.example.demo.reimburse.support;

import com.example.demo.common.exception.BusinessException;

import java.math.BigDecimal;

/**
 * 补助标准计算器。
 */
public final class AllowanceStandardCalculator {

    private static final String FIRST_TIER_CITY = "1";
    private static final String SECOND_TIER_CITY = "2";
    private static final String THIRD_TIER_CITY = "3";

    private static final BigDecimal FIRST_TIER_MEAL =
            new BigDecimal("100.00");

    private static final BigDecimal SECOND_TIER_MEAL =
            new BigDecimal("80.00");

    private static final BigDecimal THIRD_TIER_MEAL =
            new BigDecimal("50.00");

    private static final BigDecimal TRAFFIC_AMOUNT =
            new BigDecimal("40.00");

    private static final BigDecimal COMMUNICATION_AMOUNT =
            new BigDecimal("40.00");

    private AllowanceStandardCalculator() {
    }

    /**
     * 根据城市类型取得每日餐费标准。
     */
    public static BigDecimal getMealAmount(String cityType) {
        if (cityType == null) {
            throw new BusinessException(
                    400, "城市类型不能为空"
            );
        }

        return switch (cityType) {
            case FIRST_TIER_CITY -> FIRST_TIER_MEAL;
            case SECOND_TIER_CITY -> SECOND_TIER_MEAL;
            case THIRD_TIER_CITY -> THIRD_TIER_MEAL;
            default -> throw new BusinessException(
                    400, "无法识别城市类型"
            );
        };
    }

    public static BigDecimal getTrafficAmount() {
        return TRAFFIC_AMOUNT;
    }

    public static BigDecimal getCommunicationAmount() {
        return COMMUNICATION_AMOUNT;
    }

    /**
     * 计算每日全部补助标准金额。
     */
    public static BigDecimal getDailyTotal(String cityType) {
        return getMealAmount(cityType)
                .add(TRAFFIC_AMOUNT)
                .add(COMMUNICATION_AMOUNT);
    }
}