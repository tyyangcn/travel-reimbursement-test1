package com.example.demo.reimburse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.reimburse.dto.ReimbursementCreateDTO;
import com.example.demo.reimburse.dto.ReimbursementQueryDTO;
import com.example.demo.reimburse.dto.ReimbursementUpdateDTO;
import com.example.demo.reimburse.vo.ReimbursementDetailVO;
import com.example.demo.reimburse.vo.ReimbursementPageVO;

public interface ReimbursementService {
    IPage<ReimbursementPageVO> page(ReimbursementQueryDTO queryDTO);
    String create(ReimbursementCreateDTO createDTO);
    ReimbursementDetailVO getById(String id);
    void update(
            String id,
            ReimbursementUpdateDTO updateDTO
    );
    void submit(String id);
}

