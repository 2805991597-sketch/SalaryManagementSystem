package com.example.service;

import com.example.mapper.TaxDetailMapper;
import com.example.pojo.TaxDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxDetailService {
    private final TaxDetailMapper taxDetailMapper;

    public Map<String, Object> list(String taxType) {
        List<TaxDetail> list = taxDetailMapper.list(taxType, true);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("summary", getTaxSummary());
        return result;
    }

    private Map<String, Object> getTaxSummary() {

        Map<String, String> typeNames = new HashMap<>();
        typeNames.put("个人所得税", "个人所得税税率标准（中国2019年后）");
        typeNames.put("年终奖单独计税税率", "年终奖单独计税税率标准");
        typeNames.put("社保公积金", "社保公积金缴费比例");
        typeNames.put("专项附加扣除", "个人所得税专项附加扣除标准");

        return new HashMap<>(typeNames);
    }

    public Map<String, Object> calculatePersonalTax(BigDecimal monthlyIncome, BigDecimal annualBonus) {
        Map<String, Object> result = new HashMap<>();

        List<TaxDetail> taxRates = taxDetailMapper.list("个人所得税", true);
        List<TaxDetail> bonusRates = taxDetailMapper.list("年终奖单独计税税率", true);

        BigDecimal monthlyTax = calculateMonthlyTax(monthlyIncome, taxRates);
        BigDecimal annualMonthlyTax = monthlyTax.multiply(new BigDecimal("12"));
        BigDecimal bonusTax = calculateBonusTax(annualBonus, bonusRates);

        BigDecimal totalTax = annualMonthlyTax.add(bonusTax);
        BigDecimal totalIncome = monthlyIncome.multiply(new BigDecimal("12")).add(annualBonus);
        BigDecimal netIncome = totalIncome.subtract(totalTax);

        result.put("monthlyIncome", monthlyIncome);
        result.put("annualBonus", annualBonus);
        result.put("totalIncome", totalIncome);
        result.put("monthlyTax", monthlyTax);
        result.put("annualMonthlyTax", annualMonthlyTax);
        result.put("bonusTax", bonusTax);
        result.put("totalTax", totalTax);
        result.put("netIncome", netIncome);

        return result;
    }

    private BigDecimal calculateMonthlyTax(BigDecimal income, List<TaxDetail> taxRates) {
        BigDecimal taxableIncome = income.subtract(new BigDecimal("5000"));
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        for (TaxDetail rate : taxRates) {
            BigDecimal min = rate.getMinThreshold() != null ? rate.getMinThreshold() : BigDecimal.ZERO;
            BigDecimal max = rate.getMaxLimit();
            BigDecimal taxRate = rate.getRate();
            BigDecimal quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction() : BigDecimal.ZERO;

            if (max != null) {
                if (taxableIncome.compareTo(min) > 0 && taxableIncome.compareTo(max) <= 0) {
                    return taxableIncome.multiply(taxRate).subtract(quickDeduction).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            } else {
                if (taxableIncome.compareTo(min) > 0) {
                    return taxableIncome.multiply(taxRate).subtract(quickDeduction).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateBonusTax(BigDecimal bonus, List<TaxDetail> bonusRates) {
        if (bonus == null || bonus.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyBonus = bonus.divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);

        for (TaxDetail rate : bonusRates) {
            BigDecimal min = rate.getMinThreshold() != null ? rate.getMinThreshold() : BigDecimal.ZERO;
            BigDecimal max = rate.getMaxLimit();
            BigDecimal taxRate = rate.getRate();
            BigDecimal quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction() : BigDecimal.ZERO;

            if (max != null) {
                if (monthlyBonus.compareTo(min) > 0 && monthlyBonus.compareTo(max) <= 0) {
                    return bonus.multiply(taxRate).subtract(quickDeduction).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            } else {
                if (monthlyBonus.compareTo(min) > 0) {
                    return bonus.multiply(taxRate).subtract(quickDeduction).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    @Transactional
    public void save(TaxDetail taxDetail) {
        if (taxDetail.getId() != null) {
            taxDetailMapper.update(taxDetail);
            log.info("税务明细更新成功 - id:{}", taxDetail.getId());
        } else {
            taxDetailMapper.insert(taxDetail);
            log.info("税务明细新增成功");
        }
    }

    @Transactional
    public void delete(Integer id) {
        taxDetailMapper.delete(id);
        log.info("税务明细删除成功 - id:{}", id);
    }
}