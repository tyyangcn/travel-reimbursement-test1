package com.example.demo.reimburse.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.reimburse.dto.AllocationSaveDTO;
import com.example.demo.reimburse.service.AllocationService;
import com.example.demo.reimburse.vo.AllocationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reimburse/{reimId}/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @GetMapping
    public ApiResponse<List<AllocationVO>> list(
            @PathVariable String reimId) {
        return ApiResponse.success(
                allocationService.listByReimId(reimId)
        );
    }

    @PostMapping
    public ApiResponse<Void> save(
            @PathVariable String reimId,
            @RequestBody List<@Valid AllocationSaveDTO> allocationList) {
        allocationService.save(reimId, allocationList);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> delete(
            @PathVariable String reimId) {
        allocationService.deleteByReimId(reimId);
        return ApiResponse.success();
    }
}


