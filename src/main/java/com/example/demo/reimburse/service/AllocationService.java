package com.example.demo.reimburse.service;

import com.example.demo.reimburse.dto.AllocationSaveDTO;
import com.example.demo.reimburse.vo.AllocationVO;

import java.util.List;

public interface AllocationService {

    void save(String reimId, List<AllocationSaveDTO> allocationList);

    List<AllocationVO> listByReimId(String reimId);

    void deleteByReimId(String reimId);
}
