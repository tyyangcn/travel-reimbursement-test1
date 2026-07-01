package com.example.demo.reimburse.service.impl;

import com.example.demo.reimburse.mapper.DictionaryMapper;
import com.example.demo.reimburse.service.DictionaryService;
import com.example.demo.reimburse.vo.ReimbursementDictionaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DictionaryServiceImpl implements DictionaryService {
    private final DictionaryMapper dictionaryMapper;

    public DictionaryServiceImpl(DictionaryMapper dictionaryMapper) {
        this.dictionaryMapper = dictionaryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ReimbursementDictionaryVO getReimbursementDictionaries() {
        ReimbursementDictionaryVO result =
                new ReimbursementDictionaryVO();
        result.setCompanies(dictionaryMapper.selectCompanies());
        result.setDepartments(dictionaryMapper.selectDepartments());
        result.setEmployees(dictionaryMapper.selectEmployees());
        result.setBusinessTypes(dictionaryMapper.selectBusinessTypes());
        result.setCities(dictionaryMapper.selectCities());
        result.setProjects(dictionaryMapper.selectProjects());
        return result;
    }
}
