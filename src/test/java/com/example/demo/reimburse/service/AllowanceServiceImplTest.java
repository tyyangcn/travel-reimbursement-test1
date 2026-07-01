package com.example.demo.reimburse.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.dto.AllowanceCalendarAdjustDTO;
import com.example.demo.reimburse.entity.AllowanceCalendarEntity;
import com.example.demo.reimburse.entity.AllowanceEntity;
import com.example.demo.reimburse.entity.AllocationEntity;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.mapper.AllowanceCalendarMapper;
import com.example.demo.reimburse.mapper.AllowanceMapper;
import com.example.demo.reimburse.mapper.AllocationMapper;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.mapper.TripMapper;
import com.example.demo.reimburse.service.impl.AllowanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceServiceImplTest {

    private static final String REIM_ID = "TEST_REIM_ID";
    private static final String ALLOWANCE_ID = "TEST_ALLOWANCE_ID";
    private static final String CALENDAR_ID = "TEST_CALENDAR_ID";

    @Mock
    private AllowanceMapper allowanceMapper;
    @Mock
    private AllowanceCalendarMapper calendarMapper;
    @Mock
    private AllocationMapper allocationMapper;
    @Mock
    private TripMapper tripMapper;
    @Mock
    private ReimbursementMapper reimbursementMapper;

    @InjectMocks
    private AllowanceServiceImpl allowanceService;

    private AllowanceCalendarEntity calendar;

    @BeforeEach
    void setUp() {
        ReimbursementEntity reimbursement = new ReimbursementEntity();
        reimbursement.setId(REIM_ID);
        reimbursement.setStatus(0);
        reimbursement.setDeleted(0);
        when(reimbursementMapper.selectById(REIM_ID))
                .thenReturn(reimbursement);

        AllowanceEntity allowance = new AllowanceEntity();
        allowance.setId(ALLOWANCE_ID);
        allowance.setReimId(REIM_ID);
        allowance.setDeleted(0);
        when(allowanceMapper.selectById(ALLOWANCE_ID))
                .thenReturn(allowance);

        calendar = new AllowanceCalendarEntity();
        calendar.setId(CALENDAR_ID);
        calendar.setReimId(REIM_ID);
        calendar.setAllowanceId(ALLOWANCE_ID);
        calendar.setMealStandardAmount(new BigDecimal("100.00"));
        calendar.setTrafficStandardAmount(new BigDecimal("40.00"));
        calendar.setCommunicationStandardAmount(new BigDecimal("40.00"));
    }

    @Test
    void adjustCalendarShouldRejectAmountAboveStandard() {
        when(calendarMapper.selectList(any()))
                .thenReturn(List.of(calendar));
        AllowanceCalendarAdjustDTO adjust = createAdjust(
                new BigDecimal("101.00"),
                new BigDecimal("40.00"),
                new BigDecimal("40.00")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceService.adjustCalendar(
                        REIM_ID,
                        ALLOWANCE_ID,
                        List.of(adjust)
                )
        );

        assertEquals("餐费补助金额不能超过标准金额", exception.getMessage());
        verify(calendarMapper, never()).updateById(
                any(AllowanceCalendarEntity.class)
        );
    }

    @Test
    void adjustCalendarShouldRejectZeroAmountWhenSelected() {
        when(calendarMapper.selectList(any()))
                .thenReturn(List.of(calendar));
        AllowanceCalendarAdjustDTO adjust = createAdjust(
                BigDecimal.ZERO,
                new BigDecimal("40.00"),
                new BigDecimal("40.00")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceService.adjustCalendar(
                        REIM_ID,
                        ALLOWANCE_ID,
                        List.of(adjust)
                )
        );

        assertEquals("餐费补助金额必须大于0", exception.getMessage());
        verify(calendarMapper, never()).updateById(
                any(AllowanceCalendarEntity.class)
        );
    }

    @Test
    void adjustCalendarShouldSeparateStandardAndActualTotals() {
        AllowanceCalendarEntity summaryCalendar =
                new AllowanceCalendarEntity();
        summaryCalendar.setMealActualAmount(new BigDecimal("80.00"));
        summaryCalendar.setTrafficActualAmount(BigDecimal.ZERO);
        summaryCalendar.setCommunicationActualAmount(
                new BigDecimal("30.00")
        );
        when(calendarMapper.selectList(any()))
                .thenReturn(
                        List.of(calendar),
                        List.of(summaryCalendar)
                );
        when(calendarMapper.updateById(
                any(AllowanceCalendarEntity.class)
        )).thenReturn(1);
        when(allowanceMapper.updateById(
                any(AllowanceEntity.class)
        )).thenReturn(1);
        when(reimbursementMapper.updateById(
                any(ReimbursementEntity.class)
        )).thenReturn(1);
        AllocationEntity firstAllocation = new AllocationEntity();
        firstAllocation.setId("ALLOCATION_1");
        firstAllocation.setAllocationRatio(new BigDecimal("0.6000"));
        AllocationEntity secondAllocation = new AllocationEntity();
        secondAllocation.setId("ALLOCATION_2");
        secondAllocation.setAllocationRatio(new BigDecimal("0.4000"));
        when(allocationMapper.selectList(any()))
                .thenReturn(List.of(firstAllocation, secondAllocation));
        when(allocationMapper.updateById(
                any(AllocationEntity.class)
        )).thenReturn(1);

        AllowanceCalendarAdjustDTO adjust = createAdjust(
                new BigDecimal("80.00"),
                BigDecimal.ZERO,
                new BigDecimal("30.00")
        );
        adjust.setTrafficSelected(0);

        allowanceService.adjustCalendar(
                REIM_ID,
                ALLOWANCE_ID,
                List.of(adjust)
        );

        ArgumentCaptor<AllowanceEntity> allowanceCaptor =
                ArgumentCaptor.forClass(AllowanceEntity.class);
        verify(allowanceMapper).updateById(allowanceCaptor.capture());
        assertEquals(
                new BigDecimal("140.00"),
                allowanceCaptor.getValue().getApplyAmount()
        );
        assertEquals(
                new BigDecimal("110.00"),
                allowanceCaptor.getValue().getAllowanceAmount()
        );

        ArgumentCaptor<ReimbursementEntity> reimbursementCaptor =
                ArgumentCaptor.forClass(ReimbursementEntity.class);
        verify(reimbursementMapper).updateById(
                reimbursementCaptor.capture()
        );
        assertEquals(
                new BigDecimal("110.00"),
                reimbursementCaptor.getValue().getTotalAllowanceAmount()
        );

        ArgumentCaptor<AllocationEntity> allocationCaptor =
                ArgumentCaptor.forClass(AllocationEntity.class);
        verify(allocationMapper, org.mockito.Mockito.times(2))
                .updateById(allocationCaptor.capture());
        assertEquals(
                new BigDecimal("44.00"),
                allocationCaptor.getAllValues().get(0)
                        .getAllocationAmount()
        );
        assertEquals(
                new BigDecimal("66.00"),
                allocationCaptor.getAllValues().get(1)
                        .getAllocationAmount()
        );
    }

    private AllowanceCalendarAdjustDTO createAdjust(
            BigDecimal mealAmount,
            BigDecimal trafficAmount,
            BigDecimal communicationAmount) {

        AllowanceCalendarAdjustDTO adjust =
                new AllowanceCalendarAdjustDTO();
        adjust.setCalendarId(CALENDAR_ID);
        adjust.setMealSelected(1);
        adjust.setMealActualAmount(mealAmount);
        adjust.setTrafficSelected(1);
        adjust.setTrafficActualAmount(trafficAmount);
        adjust.setCommunicationSelected(1);
        adjust.setCommunicationActualAmount(communicationAmount);
        return adjust;
    }
}
