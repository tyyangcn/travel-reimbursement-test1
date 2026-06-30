package com.example.demo.reimburse.service;

import com.example.demo.reimburse.dto.TripCreateDTO;
import com.example.demo.reimburse.dto.TripUpdateDTO;
import com.example.demo.reimburse.vo.TripVO;

import java.util.List;

public interface TripService {
    String create(
            String reimId,
            TripCreateDTO createDTO
    );

    List<TripVO> listByReimId(String reimId);

    void update(
            String reimId,
            String tripId,
            TripUpdateDTO updateDTO
    );

    void delete(
            String reimId,
            String tripId
    );
}