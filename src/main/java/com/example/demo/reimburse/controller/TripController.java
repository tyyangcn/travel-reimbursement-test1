package com.example.demo.reimburse.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.reimburse.dto.TripCreateDTO;
import com.example.demo.reimburse.dto.TripUpdateDTO;
import com.example.demo.reimburse.service.TripService;
import com.example.demo.reimburse.vo.TripVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reimburse/{reimId}/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ApiResponse<String> create(
            @PathVariable String reimId,
            @Valid @RequestBody TripCreateDTO createDTO) {
        return ApiResponse.success(
                tripService.create(reimId, createDTO)
        );
    }

    @GetMapping
    public ApiResponse<List<TripVO>> list(
            @PathVariable String reimId) {
        return ApiResponse.success(
                tripService.listByReimId(reimId)
        );
    }

    @PutMapping("/{tripId}")
    public ApiResponse<Void> update(
            @PathVariable String reimId,
            @PathVariable String tripId,
            @Valid @RequestBody TripUpdateDTO updateDTO) {

        tripService.update(
                reimId,
                tripId,
                updateDTO
        );
        return ApiResponse.success();
    }

    @DeleteMapping("/{tripId}")
    public ApiResponse<Void> delete(
            @PathVariable String reimId,
            @PathVariable String tripId) {

        tripService.delete(reimId, tripId);
        return ApiResponse.success();
    }
}