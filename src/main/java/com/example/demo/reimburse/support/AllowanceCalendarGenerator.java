package com.example.demo.reimburse.support;

import com.example.demo.reimburse.entity.AllowanceCalendarEntity;
import com.example.demo.reimburse.entity.TripEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 补助日历生成器。
 */
public final class AllowanceCalendarGenerator {

    private static final int SELECTED = 1;

    private AllowanceCalendarGenerator() {
    }

    public static List<AllowanceCalendarEntity> generate(
            String reimId,
            String allowanceId,
            TripEntity trip) {

        List<AllowanceCalendarEntity> result =
                new ArrayList<>();

        BigDecimal mealAmount =
                AllowanceStandardCalculator.getMealAmount(
                        trip.getArrivalCityType()
                );

        BigDecimal trafficAmount =
                AllowanceStandardCalculator.getTrafficAmount();

        BigDecimal communicationAmount =
                AllowanceStandardCalculator
                        .getCommunicationAmount();

        BigDecimal dailyTotal = mealAmount
                .add(trafficAmount)
                .add(communicationAmount);

        LocalDate currentDate = trip.getDepartureDate();
        LocalDateTime now = LocalDateTime.now();

        while (!currentDate.isAfter(trip.getArrivalDate())) {
            AllowanceCalendarEntity calendar =
                    new AllowanceCalendarEntity();

            calendar.setId(createId());
            calendar.setReimId(reimId);
            calendar.setAllowanceId(allowanceId);
            calendar.setTripId(trip.getId());
            calendar.setAllowanceDate(currentDate);
            calendar.setWeekdayNo(
                    currentDate.getDayOfWeek().getValue()
            );

            calendar.setMealSelected(SELECTED);
            calendar.setMealStandardAmount(mealAmount);
            calendar.setMealActualAmount(mealAmount);

            calendar.setTrafficSelected(SELECTED);
            calendar.setTrafficStandardAmount(trafficAmount);
            calendar.setTrafficActualAmount(trafficAmount);

            calendar.setCommunicationSelected(SELECTED);
            calendar.setCommunicationStandardAmount(
                    communicationAmount
            );
            calendar.setCommunicationActualAmount(
                    communicationAmount
            );

            calendar.setDailyStandardAmount(dailyTotal);
            calendar.setDailyActualAmount(dailyTotal);
            calendar.setCreateTime(now);
            calendar.setUpdateTime(now);

            result.add(calendar);
            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    private static String createId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}