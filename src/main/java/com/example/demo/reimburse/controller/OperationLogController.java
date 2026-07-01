package com.example.demo.reimburse.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.reimburse.service.OperationLogService;
import com.example.demo.reimburse.vo.OperationLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reimburse/{reimId}/logs")
public class OperationLogController {
    private final OperationLogService operationLogService;

    public OperationLogController(
            OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<List<OperationLogVO>> list(
            @PathVariable String reimId) {
        return ApiResponse.success(
                operationLogService.listByReimId(reimId)
        );
    }
}
