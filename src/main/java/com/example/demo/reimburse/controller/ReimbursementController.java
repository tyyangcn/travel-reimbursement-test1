package com.example.demo.reimburse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.reimburse.dto.ReimbursementCreateDTO;
import com.example.demo.reimburse.dto.ReimbursementQueryDTO;
import com.example.demo.reimburse.dto.ReimbursementUpdateDTO;
import com.example.demo.reimburse.service.ReimbursementService;
import com.example.demo.reimburse.vo.ReimbursementDetailVO;
import com.example.demo.reimburse.vo.ReimbursementPageVO;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reimburse")
public class ReimbursementController {
    private final ReimbursementService reimbursementService;

    public ReimbursementController(ReimbursementService reimbursementService){
        this.reimbursementService=reimbursementService;
    }
    @GetMapping("/page")
    public ApiResponse<IPage<ReimbursementPageVO>> page(
            ReimbursementQueryDTO queryDTO) {
        return ApiResponse.success(
                reimbursementService.page(queryDTO)
        );
    }
    @GetMapping("/{id}")
    public ApiResponse<ReimbursementDetailVO> getById(
            @PathVariable String id) {

        return ApiResponse.success(
                reimbursementService.getById(id)
        );
    }
    @PostMapping
    public ApiResponse<String> create(
            @Valid @RequestBody ReimbursementCreateDTO createDTO) {
        return ApiResponse.success(
                reimbursementService.create(createDTO)
        );
    }
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable String id,
            @Valid @RequestBody
            ReimbursementUpdateDTO updateDTO) {

        reimbursementService.update(id, updateDTO);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(
            @PathVariable String id) {
        reimbursementService.submit(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/void")
    public ApiResponse<Void> voidReimbursement(
            @PathVariable String id) {
        reimbursementService.voidReimbursement(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReimbursement(
            @PathVariable String id) {
        reimbursementService.deleteReimbursement(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<String> copyReimbursement(
            @PathVariable String id) {
        return ApiResponse.success(
                reimbursementService.copyReimbursement(id)
        );
    }
}
