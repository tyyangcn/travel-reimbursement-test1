package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.dto.AllowanceCalendarAdjustDTO;
import com.example.demo.reimburse.entity.AllowanceCalendarEntity;
import com.example.demo.reimburse.entity.AllowanceEntity;
import com.example.demo.reimburse.entity.AllocationEntity;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.entity.TripEntity;
import com.example.demo.reimburse.mapper.AllowanceCalendarMapper;
import com.example.demo.reimburse.mapper.AllowanceMapper;
import com.example.demo.reimburse.mapper.AllocationMapper;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 补助信息业务实现。
 */
@Service
public class AllowanceServiceImpl
        implements AllowanceService {
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private static final int STATUS_DRAFT = 0;
    private static final int SELECTED = 1;
    private final AllowanceMapper allowanceMapper;
    private final AllowanceCalendarMapper calendarMapper;
    private final AllocationMapper allocationMapper;
    private final TripMapper tripMapper;
    private final ReimbursementMapper reimbursementMapper;

    public AllowanceServiceImpl(
            AllowanceMapper allowanceMapper,
            AllowanceCalendarMapper calendarMapper,
            AllocationMapper allocationMapper,
            TripMapper tripMapper,
            ReimbursementMapper reimbursementMapper) {

        this.allowanceMapper = allowanceMapper;
        this.calendarMapper = calendarMapper;
        this.allocationMapper = allocationMapper;
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
    public void adjustCalendar(
            String reimId,
            String allowanceId,
            List<AllowanceCalendarAdjustDTO> adjustList) {

        if (adjustList == null || adjustList.isEmpty()) {
            throw new BusinessException(400, "补助明细不能为空");
        }

        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);
        if (reimbursement == null
                || Integer.valueOf(DELETED).equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }
        if (!Integer.valueOf(STATUS_DRAFT).equals(reimbursement.getStatus())) {
            throw new BusinessException(400, "只有草稿状态可以调整补助明细");
        }

        AllowanceEntity allowance = allowanceMapper.selectById(allowanceId);
        if (allowance == null
                || Integer.valueOf(DELETED).equals(allowance.getDeleted())
                || !reimId.equals(allowance.getReimId())) {
            throw new BusinessException(404, "补助信息不存在");
        }

        List<AllowanceCalendarEntity> calendarList =
                calendarMapper.selectList(
                        new LambdaQueryWrapper<AllowanceCalendarEntity>()
                                .eq(
                                        AllowanceCalendarEntity::getAllowanceId,
                                        allowanceId
                                )
                );
        if (calendarList.size() != adjustList.size()) {
            throw new BusinessException(400, "补助明细数量与系统记录不一致");
        }

        Map<String, AllowanceCalendarAdjustDTO> adjustMap = new HashMap<>();
        for (AllowanceCalendarAdjustDTO item : adjustList) {
            if (adjustMap.put(item.getCalendarId(), item) != null) {
                throw new BusinessException(400, "补助日历ID不能重复");
            }
        }

        BigDecimal selectedStandardAmount = BigDecimal.ZERO;
        BigDecimal allowanceAmount = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        for (AllowanceCalendarEntity calendar : calendarList) {
            AllowanceCalendarAdjustDTO adjust =
                    adjustMap.get(calendar.getId());
            if (adjust == null) {
                throw new BusinessException(400, "存在不属于当前补助的明细");
            }

            BigDecimal mealAmount = validateActualAmount(
                    "餐费补助",
                    adjust.getMealSelected(),
                    adjust.getMealActualAmount(),
                    calendar.getMealStandardAmount()
            );
            BigDecimal trafficAmount = validateActualAmount(
                    "交通补助",
                    adjust.getTrafficSelected(),
                    adjust.getTrafficActualAmount(),
                    calendar.getTrafficStandardAmount()
            );
            BigDecimal communicationAmount = validateActualAmount(
                    "通讯补助",
                    adjust.getCommunicationSelected(),
                    adjust.getCommunicationActualAmount(),
                    calendar.getCommunicationStandardAmount()
            );
            BigDecimal dailyAmount = mealAmount
                    .add(trafficAmount)
                    .add(communicationAmount);

            AllowanceCalendarEntity updateCalendar =
                    new AllowanceCalendarEntity();
            updateCalendar.setId(calendar.getId());
            updateCalendar.setMealSelected(adjust.getMealSelected());
            updateCalendar.setMealActualAmount(mealAmount);
            updateCalendar.setTrafficSelected(adjust.getTrafficSelected());
            updateCalendar.setTrafficActualAmount(trafficAmount);
            updateCalendar.setCommunicationSelected(
                    adjust.getCommunicationSelected()
            );
            updateCalendar.setCommunicationActualAmount(
                    communicationAmount
            );
            updateCalendar.setDailyActualAmount(dailyAmount);
            updateCalendar.setUpdateTime(now);

            if (calendarMapper.updateById(updateCalendar) != 1) {
                throw new IllegalStateException("更新补助日历失败");
            }
            selectedStandardAmount = selectedStandardAmount
                    .add(selectedStandardAmount(
                            adjust.getMealSelected(),
                            calendar.getMealStandardAmount()
                    ))
                    .add(selectedStandardAmount(
                            adjust.getTrafficSelected(),
                            calendar.getTrafficStandardAmount()
                    ))
                    .add(selectedStandardAmount(
                            adjust.getCommunicationSelected(),
                            calendar.getCommunicationStandardAmount()
                    ));
            allowanceAmount = allowanceAmount.add(dailyAmount);
        }

        AllowanceEntity updateAllowance = new AllowanceEntity();
        updateAllowance.setId(allowanceId);
        updateAllowance.setStandardTotalAmount(selectedStandardAmount);
        updateAllowance.setApplyAmount(selectedStandardAmount);
        updateAllowance.setAllowanceAmount(allowanceAmount);
        updateAllowance.setUpdateTime(now);
        if (allowanceMapper.updateById(updateAllowance) != 1) {
            throw new IllegalStateException("更新补助信息失败");
        }

        updateReimbursementAmounts(reimId);
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

    private BigDecimal selectedStandardAmount(
            Integer selected,
            BigDecimal standardAmount) {

        return Integer.valueOf(SELECTED).equals(selected)
                ? standardAmount
                : BigDecimal.ZERO;
    }

    private BigDecimal validateActualAmount(
            String allowanceType,
            Integer selected,
            BigDecimal actualAmount,
            BigDecimal standardAmount) {

        if (!Integer.valueOf(SELECTED).equals(selected)) {
            return BigDecimal.ZERO;
        }
        if (actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    400,
                    allowanceType + "金额必须大于0"
            );
        }
        if (actualAmount.compareTo(standardAmount) > 0) {
            throw new BusinessException(
                    400,
                    allowanceType + "金额不能超过标准金额"
            );
        }
        return actualAmount;
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

        updateAllocationAmounts(reimId, totalAmount);
    }

    private void updateAllocationAmounts(
            String reimId,
            BigDecimal totalAmount) {

        List<AllocationEntity> allocationList =
                allocationMapper.selectList(
                        new LambdaQueryWrapper<AllocationEntity>()
                                .eq(AllocationEntity::getReimId, reimId)
                                .eq(AllocationEntity::getDeleted, NOT_DELETED)
                                .orderByAsc(AllocationEntity::getSortNo)
                );
        BigDecimal remainingAmount = totalAmount;
        for (int index = 1; index < allocationList.size(); index++) {
            AllocationEntity allocation = allocationList.get(index);
            BigDecimal amount = totalAmount
                    .multiply(allocation.getAllocationRatio())
                    .setScale(2, RoundingMode.HALF_UP);
            remainingAmount = remainingAmount.subtract(amount);
            updateAllocationAmount(allocation.getId(), amount);
        }

        if (!allocationList.isEmpty()) {
            updateAllocationAmount(
                    allocationList.get(0).getId(),
                    remainingAmount
            );
        }
    }

    private void updateAllocationAmount(
            String allocationId,
            BigDecimal amount) {

        AllocationEntity updateAllocation = new AllocationEntity();
        updateAllocation.setId(allocationId);
        updateAllocation.setAllocationAmount(amount);
        updateAllocation.setUpdateTime(LocalDateTime.now());
        if (allocationMapper.updateById(updateAllocation) != 1) {
            throw new IllegalStateException("更新费用分摊金额失败");
        }
    }
}
