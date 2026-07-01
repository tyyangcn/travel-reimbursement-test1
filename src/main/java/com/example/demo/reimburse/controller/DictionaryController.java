package com.example.demo.reimburse.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.reimburse.service.DictionaryService;
import com.example.demo.reimburse.vo.ReimbursementDictionaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionaries")
public class DictionaryController {
    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/reimbursement")
    public ApiResponse<ReimbursementDictionaryVO>
            getReimbursementDictionaries() {
        return ApiResponse.success(
                dictionaryService.getReimbursementDictionaries()
        );
    }
}
