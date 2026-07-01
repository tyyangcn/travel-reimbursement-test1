package com.example.demo.reimburse.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReimbursementDictionaryVO {
    private List<DictionaryOptionVO> companies;
    private List<DictionaryOptionVO> departments;
    private List<DictionaryOptionVO> employees;
    private List<DictionaryOptionVO> businessTypes;
    private List<DictionaryOptionVO> cities;
    private List<DictionaryOptionVO> projects;
}
