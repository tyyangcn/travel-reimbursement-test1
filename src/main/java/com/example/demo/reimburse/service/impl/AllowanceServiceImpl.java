package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.entity.AllowanceCalendarEntity;
import com.example.demo.reimburse.entity.AllowanceEntity;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.entity.TripEntity;
import com.example.demo.reimburse.mapper.AllowanceCalendarMapper;
import com.example.demo.reimburse.mapper.AllowanceMapper;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.mapper.TripMapper;
import com.example.demo.reimburse.service.AllowanceService;
import com.example.demo.reimburse.support.AllowanceCalendarGenerator;
import com.example.demo.reimburse.vo.AllowanceCalendarVO;
import com.example.demo.reimburse.vo.AllowanceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.reimburse.support.AllowanceStandardCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 补助信息业务实现。
 */
@Service
public class AllowanceServiceImpl
        implements AllowanceService {
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private final AllowanceMapper allowanceMapper;
    private final AllowanceCalendarMapper calendarMapper;
    private final TripMapper tripMapper;
    private final ReimbursementMapper reimbursementMapper;

    public AllowanceServiceImpl(
            AllowanceMapper allowanceMapper,
            AllowanceCalendarMapper calendarMapper,
            TripMapper tripMapper,
            ReimbursementMapper reimbursementMapper) {

        this.allowanceMapper = allowanceMapper;
        this.calendarMapper = calendarMapper;
        this.tripMapper = tripMapper;
        this.reimbursementMapper = reimbursementMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateOrUpdateByTrip(
            String reimId,
            String tripId) {

        TripEntity trip = tripMapper.selectById(tripId);

        if (trip == null
                || Integer.valueOf(DELETED)
                .equals(trip.getDeleted())
                || !reimId.equals(trip.getReimId())) {
            throw new BusinessException(404, "行程不存在");
        }

        AllowanceEntity existingAllowance =
                allowanceMapper.selectOne(
                        new LambdaQueryWrapper<AllowanceEntity>()
                                .eq(AllowanceEntity::getTripId, tripId)
                                .last("LIMIT 1")
                );

        String allowanceId;
        Integer sortNo;

        if (existingAllowance == null) {
            allowanceId = createId();
            sortNo = getNextAllowanceSortNo(reimId);
        } else {
            allowanceId = existingAllowance.getId();
            sortNo = existingAllowance.getSortNo();

            calendarMapper.delete(
                    new LambdaQueryWrapper<AllowanceCalendarEntity>()
                            .eq(
                                    AllowanceCalendarEntity::getAllowanceId,
                                    allowanceId
                            )
            );
        }

        List<AllowanceCalendarEntity> calendarList =
                AllowanceCalendarGenerator.generate(
                        reimId,
                        allowanceId,
                        trip
                );

        int allowanceDays = calendarList.size();

        BigDecimal dailyTotal =
                AllowanceStandardCalculator.getDailyTotal(
                        trip.getArrivalCityType()
                );

        BigDecimal totalAmount = dailyTotal.multiply(
                BigDecimal.valueOf(allowanceDays)
        );

        LocalDateTime now = LocalDateTime.now();

        AllowanceEntity allowance = new AllowanceEntity();
        allowance.setId(allowanceId);
        allowance.setReimId(reimId);
        allowance.setTripId(tripId);
        allowance.setTravelerId(trip.getTravelerId());
        allowance.setTravelerName(trip.getTravelerName());
        allowance.setStartDate(trip.getDepartureDate());
        allowance.setEndDate(trip.getArrivalDate());
        allowance.setAllowanceDays(allowanceDays);

        allowance.setDepartureCityName(
                trip.getDepartureCityName()
        );
        allowance.setAllowanceCityNo(
                trip.getArrivalCityNo()
        );
        allowance.setAllowanceCityName(
                trip.getArrivalCityName()
        );
        allowance.setCityType(
                trip.getArrivalCityType()
        );

        allowance.setStandardTotalAmount(totalAmount);
        allowance.setApplyAmount(totalAmount);
        allowance.setAllowanceAmount(totalAmount);
        allowance.setSortNo(sortNo);
        allowance.setDeleted(NOT_DELETED);
        allowance.setUpdateTime(now);

        if (existingAllowance == null) {
            allowance.setCreateTime(now);

            if (allowanceMapper.insert(allowance) != 1) {
                throw new IllegalStateException("新增补助信息失败");
            }
        } else if (allowanceMapper.updateById(allowance) != 1) {
            throw new IllegalStateException("更新补助信息失败");
        }

        for (AllowanceCalendarEntity calendar : calendarList) {
            if (calendarMapper.insert(calendar) != 1) {
                throw new IllegalStateException("生成补助日历失败");
            }
        }
        updateReimbursementAmounts(reimId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllowanceVO> listByReimId(String reimId) {
        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(
                    400, "报销单ID不能为空"
            );
        }

        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);

        if (reimbursement == null
                || Integer.valueOf(DELETED)
                .equals(reimbursement.getDeleted())) {
            throw new BusinessException(
                    404, "报销单不存在"
            );
        }

        List<AllowanceEntity> allowanceList =
                allowanceMapper.selectList(
                        new LambdaQueryWrapper<AllowanceEntity>()
                                .eq(
                                        AllowanceEntity::getReimId,
                                        reimId
                                )
                                .eq(
                                        AllowanceEntity::getDeleted,
                                        NOT_DELETED
                                )
                                .orderByAsc(
                                        AllowanceEntity::getSortNo
                                )
                );

        List<AllowanceVO> result = new ArrayList<>();

        for (AllowanceEntity allowance : allowanceList) {
            AllowanceVO allowanceVO = new AllowanceVO();
            BeanUtils.copyProperties(allowance, allowanceVO);

            List<AllowanceCalendarEntity> calendarList =
                    calendarMapper.selectList(
                            new LambdaQueryWrapper
                                    <AllowanceCalendarEntity>()
                                    .eq(
                                            AllowanceCalendarEntity
                                                    ::getAllowanceId,
                                            allowance.getId()
                                    )
                                    .orderByAsc(
                                            AllowanceCalendarEntity
                                                    ::getAllowanceDate
                                    )
                    );

            List<AllowanceCalendarVO> calendarVOList =
                    new ArrayList<>();

            for (AllowanceCalendarEntity calendar : calendarList) {
                AllowanceCalendarVO calendarVO =
                        new AllowanceCalendarVO();

                BeanUtils.copyProperties(calendar, calendarVO);
                calendarVOList.add(calendarVO);
            }

            allowanceVO.setCalendarList(calendarVOList);
            result.add(allowanceVO);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByTripId(
            String reimId,
            String tripId) {

        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        if (tripId == null || tripId.isBlank()) {
            throw new BusinessException(400, "行程ID不能为空");
        }

        AllowanceEntity allowance =
                allowanceMapper.selectOne(
                        new LambdaQueryWrapper<AllowanceEntity>()
                                .eq(AllowanceEntity::getReimId, reimId)
                                .eq(AllowanceEntity::getTripId, tripId)
                                .last("LIMIT 1")
                );

        if (allowance == null
                || Integer.valueOf(DELETED).equals(allowance.getDeleted())) {
            updateReimbursementAmounts(reimId);
            return;
        }

        calendarMapper.delete(
                new LambdaQueryWrapper<AllowanceCalendarEntity>()
                        .eq(AllowanceCalendarEntity::getAllowanceId, allowance.getId())
        );

        AllowanceEntity deleteEntity = new AllowanceEntity();
        deleteEntity.setId(allowance.getId());
        deleteEntity.setDeleted(DELETED);
        deleteEntity.setUpdateTime(LocalDateTime.now());

        if (allowanceMapper.updateById(deleteEntity) != 1) {
            throw new IllegalStateException("删除补助信息失败");
        }

        updateReimbursementAmounts(reimId);
    }

    private int getNextAllowanceSortNo(String reimId) {
        AllowanceEntity lastAllowance =
                allowanceMapper.selectOne(
                        new LambdaQueryWrapper<AllowanceEntity>()
                                .select(AllowanceEntity::getSortNo)
                                .eq(AllowanceEntity::getReimId, reimId)
                                .orderByDesc(AllowanceEntity::getSortNo)
                                .last("LIMIT 1")
                );

        return lastAllowance == null
                ? 1
                : lastAllowance.getSortNo() + 1;
    }

    private String createId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    /**
     * 汇总报销单的餐费、交通、通讯及补助总金额。
     */
    private void updateReimbursementAmounts(String reimId) {
        List<AllowanceCalendarEntity> calendarList =
                calendarMapper.selectList(
                        new LambdaQueryWrapper<AllowanceCalendarEntity>()
                                .eq(
                                        AllowanceCalendarEntity::getReimId,
                                        reimId
                                )
                );

        BigDecimal mealTotal = BigDecimal.ZERO;
        BigDecimal trafficTotal = BigDecimal.ZERO;
        BigDecimal communicationTotal = BigDecimal.ZERO;

        for (AllowanceCalendarEntity calendar : calendarList) {
            mealTotal = mealTotal.add(
                    calendar.getMealActualAmount()
            );

            trafficTotal = trafficTotal.add(
                    calendar.getTrafficActualAmount()
            );

            communicationTotal = communicationTotal.add(
                    calendar.getCommunicationActualAmount()
            );
        }

        BigDecimal totalAmount = mealTotal
                .add(trafficTotal)
                .add(communicationTotal);

        ReimbursementEntity updateEntity =
                new ReimbursementEntity();

        updateEntity.setId(reimId);
        updateEntity.setMealAllowanceAmount(mealTotal);
        updateEntity.setTrafficAllowanceAmount(trafficTotal);
        updateEntity.setCommunicationAllowanceAmount(
                communicationTotal
        );
        updateEntity.setTotalAllowanceAmount(totalAmount);
        updateEntity.setUpdateTime(LocalDateTime.now());

        if (reimbursementMapper.updateById(updateEntity) != 1) {
            throw new IllegalStateException(
                    "更新报销单补助汇总金额失败"
            );
        }
    }
}