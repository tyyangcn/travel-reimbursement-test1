package com.example.demo.reimburse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.reimburse.dto.TripCreateDTO;
import com.example.demo.reimburse.dto.TripUpdateDTO;
import com.example.demo.reimburse.entity.ReimbursementEntity;
import com.example.demo.reimburse.entity.TripEntity;
import com.example.demo.reimburse.mapper.ReimbursementMapper;
import com.example.demo.reimburse.mapper.TripMapper;
import com.example.demo.reimburse.service.TripService;
import com.example.demo.reimburse.vo.TripVO;
import com.example.demo.reimburse.service.AllowanceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TripServiceImpl implements TripService {

    private static final int STATUS_DRAFT = 0;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;

    private final TripMapper tripMapper;
    private final ReimbursementMapper reimbursementMapper;
    private final AllowanceService allowanceService;
    public TripServiceImpl(
            TripMapper tripMapper,
            ReimbursementMapper reimbursementMapper,
            AllowanceService allowanceService) {
        this.tripMapper = tripMapper;
        this.reimbursementMapper = reimbursementMapper;
        this.allowanceService = allowanceService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(
            String reimId,
            TripCreateDTO createDTO) {
        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }
        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);

        if (reimbursement == null
                || Integer.valueOf(DELETED)
                .equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT)
                .equals(reimbursement.getStatus())) {
            throw new BusinessException(
                    400, "只有草稿报销单可以新增行程"
            );
        }

        validateTrip(
                reimId,
                null,
                createDTO.getTravelerId(),
                createDTO.getDepartureDate(),
                createDTO.getArrivalDate()
        );

        TripEntity entity = new TripEntity();
        BeanUtils.copyProperties(createDTO, entity);

        String id = UUID.randomUUID()
                .toString()
                .replace("-", "");

        LocalDateTime now = LocalDateTime.now();
        entity.setId(id);
        entity.setReimId(reimId);
        entity.setSortNo(getNextSortNo(reimId));
        entity.setDeleted(NOT_DELETED);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        if (tripMapper.insert(entity) != 1) {
            throw new IllegalStateException("新增行程失败");
        }
        allowanceService.generateOrUpdateByTrip(reimId, id);
        return id;
    }

    @Override
    public List<TripVO> listByReimId(String reimId) {
        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);

        if (reimbursement == null
                || Integer.valueOf(DELETED)
                .equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        List<TripEntity> entities = tripMapper.selectList(
                new LambdaQueryWrapper<TripEntity>()
                        .eq(TripEntity::getReimId, reimId)
                        .eq(TripEntity::getDeleted, NOT_DELETED)
                        .orderByAsc(TripEntity::getSortNo)
        );

        List<TripVO> result = new ArrayList<>();

        for (TripEntity entity : entities) {
            TripVO tripVO = new TripVO();
            BeanUtils.copyProperties(entity, tripVO);
            result.add(tripVO);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(
            String reimId,
            String tripId,
            TripUpdateDTO updateDTO) {

        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        if (tripId == null || tripId.isBlank()) {
            throw new BusinessException(400, "行程ID不能为空");
        }

        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);

        if (reimbursement == null
                || Integer.valueOf(DELETED)
                .equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT)
                .equals(reimbursement.getStatus())) {
            throw new BusinessException(
                    400,
                    "只有草稿报销单可以修改行程"
            );
        }

        TripEntity existingTrip =
                tripMapper.selectById(tripId);

        if (existingTrip == null
                || Integer.valueOf(DELETED)
                .equals(existingTrip.getDeleted())
                || !reimId.equals(existingTrip.getReimId())) {
            throw new BusinessException(404, "行程不存在");
        }

        validateTrip(
                reimId,
                tripId,
                updateDTO.getTravelerId(),
                updateDTO.getDepartureDate(),
                updateDTO.getArrivalDate()
        );

        TripEntity updateEntity = new TripEntity();
        BeanUtils.copyProperties(updateDTO, updateEntity);

        updateEntity.setId(tripId);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                tripMapper.updateById(updateEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("修改行程失败");
        }
        allowanceService.generateOrUpdateByTrip(reimId, tripId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(
            String reimId,
            String tripId) {

        if (reimId == null || reimId.isBlank()) {
            throw new BusinessException(400, "报销单ID不能为空");
        }

        if (tripId == null || tripId.isBlank()) {
            throw new BusinessException(400, "行程ID不能为空");
        }

        ReimbursementEntity reimbursement =
                reimbursementMapper.selectById(reimId);

        if (reimbursement == null
                || Integer.valueOf(DELETED)
                .equals(reimbursement.getDeleted())) {
            throw new BusinessException(404, "报销单不存在");
        }

        if (!Integer.valueOf(STATUS_DRAFT)
                .equals(reimbursement.getStatus())) {
            throw new BusinessException(
                    400,
                    "只有草稿报销单可以删除行程"
            );
        }

        TripEntity existingTrip =
                tripMapper.selectById(tripId);

        if (existingTrip == null
                || Integer.valueOf(DELETED)
                .equals(existingTrip.getDeleted())
                || !reimId.equals(existingTrip.getReimId())) {
            throw new BusinessException(404, "行程不存在");
        }

        TripEntity deleteEntity = new TripEntity();
        deleteEntity.setId(tripId);
        deleteEntity.setDeleted(DELETED);
        deleteEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows =
                tripMapper.updateById(deleteEntity);

        if (affectedRows != 1) {
            throw new IllegalStateException("删除行程失败");
        }
        allowanceService.removeByTripId(reimId, tripId);
    }

    /**
     * 获取下一行程排序号。
     */
    private int getNextSortNo(String reimId) {
        TripEntity lastTrip = tripMapper.selectOne(
                new LambdaQueryWrapper<TripEntity>()
                        .select(TripEntity::getSortNo)
                        .eq(TripEntity::getReimId, reimId)
                        .orderByDesc(TripEntity::getSortNo)
                        .last("LIMIT 1")
        );
        if (lastTrip == null) {
            return 1;
        }
        return lastTrip.getSortNo() + 1;
    }
    /**
     * 校验行程日期及同一出行人的日期重叠。
     */
    private void validateTrip(
            String reimId,
            String excludeTripId,
            String travelerId,
            LocalDate departureDate,
            LocalDate arrivalDate) {

        if (arrivalDate.isBefore(departureDate)) {
            throw new BusinessException(
                    400, "到达日期不能早于出发日期"
            );
        }

        if (arrivalDate.isAfter(LocalDate.now())) {
            throw new BusinessException(
                    400, "到达日期不能晚于当前日期"
            );
        }

        LambdaQueryWrapper<TripEntity> queryWrapper =
                new LambdaQueryWrapper<TripEntity>()
                        .eq(TripEntity::getReimId, reimId)
                        .eq(TripEntity::getTravelerId, travelerId)
                        .eq(TripEntity::getDeleted, NOT_DELETED)
                        .le(TripEntity::getDepartureDate, arrivalDate)
                        .ge(TripEntity::getArrivalDate, departureDate);

        if (excludeTripId != null) {
            queryWrapper.ne(TripEntity::getId, excludeTripId);
        }

        if (tripMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(
                    400, "该出行人的行程日期存在重叠"
            );
        }
    }
}