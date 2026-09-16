package com.example.service;

import com.example.mapper.SalaryMapper;
import com.example.mapper.YearEndBonusMapper;
import com.example.mapper.TaxDetailMapper;
import com.example.pojo.Salary;
import com.example.pojo.SalaryVO;
import com.example.pojo.YearEndBonus;
import com.example.pojo.TaxDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class YearEndBonusService {
    private final YearEndBonusMapper yearEndBonusMapper;
    private final SalaryMapper salaryMapper;
    private final TaxDetailMapper taxDetailMapper;

    public Map<String, Object> list(Integer year, Integer empId, String name) {
        List<YearEndBonus> bonusList = yearEndBonusMapper.list(year, empId, name);

        for (YearEndBonus bonus : bonusList) {
            calculateAnnualSalary(bonus);
        }

        return buildYearEndReport(bonusList, year);
    }

    private void calculateAnnualSalary(YearEndBonus bonus) {
        if (bonus.getEmpId() == null || bonus.getYear() == null) {
            return;
        }

        List<SalaryVO> salaryList = salaryMapper.list(null, null, null, null, bonus.getEmpId(), "DESC");
        BigDecimal yearSalary = BigDecimal.ZERO;
        BigDecimal yearActual = BigDecimal.ZERO;
        BigDecimal yearTax = BigDecimal.ZERO;

        for (Salary salary : salaryList) {
            if (salary.getMonth() != null && salary.getMonth().getYear() == bonus.getYear()) {
                BigDecimal monthlySalary = (salary.getBasicSalary() != null ? salary.getBasicSalary() : BigDecimal.ZERO)
                        .add(salary.getFoodSubsidy() != null ? salary.getFoodSubsidy() : BigDecimal.ZERO)
                        .add(salary.getTrafficSubsidy() != null ? salary.getTrafficSubsidy() : BigDecimal.ZERO)
                        .add(salary.getHousingSubsidy() != null ? salary.getHousingSubsidy() : BigDecimal.ZERO)
                        .add(salary.getLevelSubsidy() != null ? salary.getLevelSubsidy() : BigDecimal.ZERO)
                        .subtract(salary.getLateFine() != null ? salary.getLateFine() : BigDecimal.ZERO);
                yearSalary = yearSalary.add(monthlySalary);
                yearActual = yearActual.add(salary.getActualSalary() != null ? salary.getActualSalary() : BigDecimal.ZERO);
                yearTax = yearTax.add(salary.getTax() != null ? salary.getTax() : BigDecimal.ZERO);
            }
        }

        bonus.setYearSalary(yearSalary);
        bonus.setYearBonus(bonus.getBonusAmount() != null ? bonus.getBonusAmount() : BigDecimal.ZERO);
        bonus.setYearTax(yearTax.add(bonus.getTaxAmount() != null ? bonus.getTaxAmount() : BigDecimal.ZERO));
        bonus.setYearActual(yearActual.add(bonus.getNetAmount() != null ? bonus.getNetAmount() : BigDecimal.ZERO));
    }

    public Map<String, Object> buildYearEndReport(List<YearEndBonus> bonusList, Integer year) {
        Map<String, Object> result = new HashMap<>();
        result.put("bonusList", bonusList);
        result.put("year", year);
        return result;
    }

    @Transactional
    public void save(YearEndBonus bonus) {
        if (bonus.getEmpId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        if (bonus.getYear() == null) {
            throw new IllegalArgumentException("年份不能为空");
        }
        if (bonus.getBonusAmount() == null) {
            throw new IllegalArgumentException("年终奖金额不能为空");
        }

        calculateTax(bonus);

        YearEndBonus existing = yearEndBonusMapper.findByEmpAndYear(bonus.getEmpId(), bonus.getYear());
        if (existing != null) {
            bonus.setId(existing.getId());
            yearEndBonusMapper.update(bonus);
            log.info("年终奖更新成功 - empId:{}, year:{}", bonus.getEmpId(), bonus.getYear());
        } else {
            yearEndBonusMapper.insert(bonus);
            log.info("年终奖新增成功 - empId:{}, year:{}", bonus.getEmpId(), bonus.getYear());
        }
    }

    public void calculateTax(YearEndBonus bonus) {
        BigDecimal bonusAmount = bonus.getBonusAmount();
        if (bonusAmount == null || bonusAmount.compareTo(BigDecimal.ZERO) <= 0) {
            bonus.setTaxAmount(BigDecimal.ZERO);
            bonus.setNetAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal monthlyBonus = bonusAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        List<TaxDetail> bonusRates = taxDetailMapper.list("年终奖单独计税税率", true);
        
        BigDecimal taxRate = BigDecimal.ZERO;
        BigDecimal quickDeduction = BigDecimal.ZERO;
        
        for (TaxDetail rate : bonusRates) {
            BigDecimal min = rate.getMinThreshold() != null ? rate.getMinThreshold() : BigDecimal.ZERO;
            BigDecimal max = rate.getMaxLimit();
            
            if (max != null) {
                if (monthlyBonus.compareTo(min) > 0 && monthlyBonus.compareTo(max) <= 0) {
                    taxRate = rate.getRate();
                    quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction() : BigDecimal.ZERO;
                    break;
                }
            } else {
                if (monthlyBonus.compareTo(min) > 0) {
                    taxRate = rate.getRate();
                    quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction() : BigDecimal.ZERO;
                    break;
                }
            }
        }

        BigDecimal taxAmount = bonusAmount.multiply(taxRate).subtract(quickDeduction).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = bonusAmount.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);

        bonus.setTaxAmount(taxAmount);
        bonus.setNetAmount(netAmount);
    }

    @Transactional
    public void delete(Integer id) {
        yearEndBonusMapper.delete(id);
        log.info("年终奖删除成功 - id:{}", id);
    }

    public Map<String, Object> getAnnualSalarySummary(Integer empId, Integer year) {
        Map<String, Object> result = new HashMap<>();

        List<SalaryVO> salaryList = salaryMapper.list(null, null, null, null, empId, "DESC");
        BigDecimal yearSalary = BigDecimal.ZERO;
        BigDecimal yearTax = BigDecimal.ZERO;
        BigDecimal yearActual = BigDecimal.ZERO;

        for (Salary salary : salaryList) {
            if (salary.getMonth() != null && salary.getMonth().getYear() == year) {
                BigDecimal actualSalary = salary.getActualSalary() != null ? salary.getActualSalary() : BigDecimal.ZERO;
                BigDecimal tax = salary.getTax() != null ? salary.getTax() : BigDecimal.ZERO;
                BigDecimal monthlySalary = (salary.getBasicSalary() != null ? salary.getBasicSalary() : BigDecimal.ZERO)
                        .add(salary.getFoodSubsidy() != null ? salary.getFoodSubsidy() : BigDecimal.ZERO)
                        .add(salary.getTrafficSubsidy() != null ? salary.getTrafficSubsidy() : BigDecimal.ZERO)
                        .add(salary.getHousingSubsidy() != null ? salary.getHousingSubsidy() : BigDecimal.ZERO)
                        .add(salary.getLevelSubsidy() != null ? salary.getLevelSubsidy() : BigDecimal.ZERO)
                        .subtract(salary.getLateFine() != null ? salary.getLateFine() : BigDecimal.ZERO);
                yearSalary = yearSalary.add(monthlySalary);
                yearTax = yearTax.add(tax);
                yearActual = yearActual.add(actualSalary);
            }
        }

        YearEndBonus bonus = yearEndBonusMapper.findByEmpAndYear(empId, year);
        if (bonus != null) {
            yearSalary = yearSalary.add(bonus.getBonusAmount() != null ? bonus.getBonusAmount() : BigDecimal.ZERO);
            yearTax = yearTax.add(bonus.getTaxAmount() != null ? bonus.getTaxAmount() : BigDecimal.ZERO);
            yearActual = yearActual.add(bonus.getNetAmount() != null ? bonus.getNetAmount() : BigDecimal.ZERO);
        }

        result.put("year", year);
        result.put("empId", empId);
        result.put("yearSalary", yearSalary);
        result.put("yearTax", yearTax);
        result.put("yearActual", yearActual);
        result.put("bonus", bonus);

        return result;
    }
}