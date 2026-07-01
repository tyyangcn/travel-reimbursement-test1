package com.example.demo.reimburse.service;

import com.example.demo.reimburse.dto.AllowanceCalendarAdjustDTO;
import com.example.demo.reimburse.vo.AllowanceVO;

import java.util.List;

/**
 * 补助信息业务接口。
 */
public interface AllowanceService {


    void generateOrUpdateByTrip(
            String reimId,
            String tripId
    );

    /**
     * 查询指定报销单的全部补助信息。
     *
     * @param reimId 报销单ID
     * @return 补助汇总及每日明细
     */
    List<AllowanceVO> listByReimId(String reimId);

    /**
     * 调整指定行程的每日补助选择，并重新汇总金额。
     *
     * @param reimId 报销单ID
     * @param allowanceId 补助信息ID
     * @param adjustList 每日补助调整参数
     */
    void adjustCalendar(
            String reimId,
            String allowanceId,
            List<AllowanceCalendarAdjustDTO> adjustList
    );

    /**
     * 行程删除时同步删除对应补助数据。
     *
     * @param reimId 报销单ID
     * @param tripId 行程ID
     */
    void removeByTripId(
            String reimId,
            String tripId
    );
}
