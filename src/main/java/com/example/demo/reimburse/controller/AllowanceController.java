package com.example.demo.reimburse.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.reimburse.service.AllowanceService;
import com.example.demo.reimburse.vo.AllowanceVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reimburse/{reimId}/allowances")
public class AllowanceController {

    private final AllowanceService allowanceService;

    public AllowanceController(AllowanceService allowanceService) {
        this.allowanceService = allowanceService;
    }

    @GetMapping
    public ApiResponse<List<AllowanceVO>> list(
            @PathVariable String reimId) {

        return ApiResponse.success(
                allowanceService.listByReimId(reimId)
        );
    }
}