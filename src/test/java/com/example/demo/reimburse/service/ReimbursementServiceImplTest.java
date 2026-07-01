package com.example.demo.reimburse.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.service.impl.ReimbursementServiceImpl;
import com.example.demo.reimburse.vo.AllocationVO;
import com.example.demo.reimburse.vo.AllowanceVO;
import com.example.demo.reimburse.vo.TripVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ReimbursementServiceImplTest {

    private static final String REIM_ID = "TEST_REIM_ID";

    @Mock
    private ReimbursementMapper reimbursementMapper;
    @Mock
    private TripService tripService;
    @Mock
    private AllocationService allocationService;
    @Mock
    private AllowanceService allowanceService;
    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private ReimbursementServiceImpl reimbursementService;

    private ReimbursementEntity reimbursement;

    @BeforeEach
    void setUp() {
        reimbursement = new ReimbursementEntity();
        reimbursement.setId(REIM_ID);
        reimbursement.setReimNo("RCBX_TEST");
        reimbursement.setStatus(0);
        reimbursement.setDeleted(0);
        reimbursement.setTotalAllowanceAmount(
                new BigDecimal("540.00")
        );

        when(reimbursementMapper.selectById(REIM_ID))
                .thenReturn(reimbursement);
    }

    @Test
    void submitShouldRejectNonDraftReimbursement() {
        reimbursement.setStatus(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reimbursementService.submit(REIM_ID)
        );

        assertEquals(
                "只有草稿状态的报销单允许提交",
                exception.getMessage()
        );
        verify(reimbursementMapper, never()).updateById(
                any(ReimbursementEntity.class)
        );
    }

    @Test
    void submitShouldRequireAtLeastOneTrip() {
        when(tripService.listByReimId(REIM_ID))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reimbursementService.submit(REIM_ID)
        );

        assertEquals(
                "请至少补录一条行程后再提交",
                exception.getMessage()
        );
        verify(reimbursementMapper, never()).updateById(
                any(ReimbursementEntity.class)
        );
    }

    @Test
    void submitShouldRequireAllowanceForEveryTrip() {
        when(tripService.listByReimId(REIM_ID))
                .thenReturn(List.of(createTrip()));
        when(allowanceService.listByReimId(REIM_ID))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reimbursementService.submit(REIM_ID)
        );

        assertEquals(
                "部分行程尚未生成补助，请检查后再提交",
                exception.getMessage()
        );
        verify(reimbursementMapper, never()).updateById(
                any(ReimbursementEntity.class)
        );
    }

    @Test
    void submitShouldRequireAllocation() {
        prepareTripAndAllowance();
        when(allocationService.listByReimId(REIM_ID))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reimbursementService.submit(REIM_ID)
        );

        assertEquals(
                "请完成费用分摊后再提交",
                exception.getMessage()
        );
        verify(reimbursementMapper, never()).updateById(
                any(ReimbursementEntity.class)
        );
    }

    @Test
    void submitShouldRequireAllocationRatioTotalOfOne() {
        prepareTripAndAllowance();
        when(allocationService.listByReimId(REIM_ID))
                .thenReturn(List.of(
                        createAllocation("0.80", "432.00")
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reimbursementService.submit(REIM_ID)
        );

        assertEquals(
                "费用分摊比例合计必须等于100%",
                exception.getMessage()
        );
        verify(reimbursementMapper, never()).updateById(
                any(ReimbursementEntity.class)
        );
    }

    @Test
    void submitShouldRequireAllocationAmountToMatchTotal() {
        prepareTripAndAllowance();
        when(allocationService.listByReimId(REIM_ID))
                .thenReturn(List.of(
                        createAllocation("1.00", "500.00")
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reimbursementService.submit(REIM_ID)
        );

        assertEquals(
                "费用分摊金额合计必须等于补助总金额",
                exception.getMessage()
        );
        verify(reimbursementMapper, never()).updateById(
                any(ReimbursementEntity.class)
        );
    }

    @Test
    void submitShouldUpdateStatusAndRecordLogWhenDataIsComplete() {
        prepareTripAndAllowance();
        when(allocationService.listByReimId(REIM_ID))
                .thenReturn(List.of(
                        createAllocation("0.60", "324.00"),
                        createAllocation("0.40", "216.00")
                ));
        when(reimbursementMapper.updateById(
                any(ReimbursementEntity.class)
        ))
                .thenReturn(1);

        reimbursementService.submit(REIM_ID);

        verify(reimbursementMapper).updateById(
                any(ReimbursementEntity.class)
        );
        verify(operationLogService).record(
                reimbursement,
                "SUBMIT",
                "提交报销单"
        );
    }

    private void prepareTripAndAllowance() {
        when(tripService.listByReimId(REIM_ID))
                .thenReturn(List.of(createTrip()));
        when(allowanceService.listByReimId(REIM_ID))
                .thenReturn(List.of(createAllowance()));
    }

    private TripVO createTrip() {
        TripVO trip = new TripVO();
        trip.setId("TEST_TRIP_ID");
        trip.setReimId(REIM_ID);
        return trip;
    }

    private AllowanceVO createAllowance() {
        AllowanceVO allowance = new AllowanceVO();
        allowance.setId("TEST_ALLOWANCE_ID");
        allowance.setReimId(REIM_ID);
        allowance.setTripId("TEST_TRIP_ID");
        return allowance;
    }

    private AllocationVO createAllocation(
            String ratio,
            String amount) {
        AllocationVO allocation = new AllocationVO();
        allocation.setAllocationRatio(new BigDecimal(ratio));
        allocation.setAllocationAmount(new BigDecimal(amount));
        return allocation;
    }
}
