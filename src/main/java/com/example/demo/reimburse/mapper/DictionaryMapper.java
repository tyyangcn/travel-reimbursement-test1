package com.example.demo.reimburse.mapper;

import com.example.demo.reimburse.vo.DictionaryOptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictionaryMapper {

    @Select("""
            SELECT id, company_no AS no, company_name AS name
            FROM fk_reim_company
            WHERE deleted = 0
            ORDER BY company_no
            """)
    List<DictionaryOptionVO> selectCompanies();

    @Select("""
            SELECT id, department_no AS no, department_name AS name
            FROM fk_reim_department
            WHERE deleted = 0
            ORDER BY department_no
            """)
    List<DictionaryOptionVO> selectDepartments();

    @Select("""
            SELECT id, employee_no AS no, employee_name AS name,
                   department_id AS departmentId,
                   department_no AS departmentNo,
                   department_name AS departmentName
            FROM fk_reim_employee
            WHERE deleted = 0
            ORDER BY employee_no
            """)
    List<DictionaryOptionVO> selectEmployees();

    @Select("""
            SELECT id, business_type_no AS no,
                   business_type_name AS name
            FROM fk_reim_business_type
            WHERE deleted = 0
            ORDER BY business_type_no
            """)
    List<DictionaryOptionVO> selectBusinessTypes();

    @Select("""
            SELECT city_no AS id, city_no AS no,
                   city_name AS name, city_type AS type
            FROM fk_reim_city
            WHERE deleted = 0
            ORDER BY city_no
            """)
    List<DictionaryOptionVO> selectCities();

    @Select("""
            SELECT id, project_no AS no, project_name AS name
            FROM fk_reim_project
            WHERE deleted = 0
            ORDER BY project_no
            """)
    List<DictionaryOptionVO> selectProjects();
}
