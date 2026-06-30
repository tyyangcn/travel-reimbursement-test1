package com.example.demo.reimburse.dto;

import lombok.Data;

@Data
public class ReimbursementQueryDTO {
    private String reimNo;
    private String reimTitle;
    private String travelReason;
    private String companyId;
    private String reimDeptId;
    private String reimUserId;
    private String businessTypeId;
    private Integer status;
    private long pageNum = 1;
    private long pageSize = 10;
}
