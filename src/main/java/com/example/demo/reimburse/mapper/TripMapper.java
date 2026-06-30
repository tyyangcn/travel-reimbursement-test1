package com.example.demo.reimburse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.reimburse.entity.TripEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripMapper
        extends BaseMapper<TripEntity> {
}