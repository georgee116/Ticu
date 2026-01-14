package com.example.transactions.utils;

import com.example.transactions.entity.Transaction;
import com.example.transactions.enums.Currency;

import java.math.BigDecimal;

public class TransactionHelper {
    public static BigDecimal checkAmountAntiFraud(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return BigDecimal.ONE;
        } else if (amount.compareTo(new BigDecimal("5000")) > 0) {
            return new BigDecimal("0.7");
        } else if (amount.compareTo(new BigDecimal("2000")) > 0) {
            return new BigDecimal("0.4");
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal ConvertCurrency(Currency previousCurrency, Currency newCurrency, BigDecimal amount) {
        if (previousCurrency == newCurrency) {
            return amount;
        }

        BigDecimal prevVal = BigDecimal.valueOf(previousCurrency.getValue());
        BigDecimal newVal = BigDecimal.valueOf(newCurrency.getValue());

        return amount.multiply(prevVal)
                .divide(newVal, 8, java.math.RoundingMode.HALF_UP);
    }
}
