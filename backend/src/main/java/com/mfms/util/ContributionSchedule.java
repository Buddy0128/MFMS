package com.mfms.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ContributionSchedule {
    public static final YearMonth START_MONTH = YearMonth.of(2024, 1);
    public static final BigDecimal MONTHLY_AMOUNT = new BigDecimal("1000");
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private ContributionSchedule() {
    }

    public static int expectedMonths() {
        return Math.max(0, (int) ChronoUnit.MONTHS.between(START_MONTH, YearMonth.now()) + 1);
    }

    public static int paidMonths(BigDecimal totalDeposit) {
        if (totalDeposit == null || totalDeposit.signum() <= 0) return 0;
        int paid = totalDeposit.divideToIntegralValue(MONTHLY_AMOUNT).intValue();
        return Math.min(paid, expectedMonths());
    }

    public static BigDecimal extraAmount(BigDecimal totalDeposit) {
        if (totalDeposit == null || totalDeposit.signum() <= 0) return BigDecimal.ZERO;
        return totalDeposit.remainder(MONTHLY_AMOUNT);
    }

    public static int pendingMonths(BigDecimal totalDeposit) {
        return Math.max(0, expectedMonths() - paidMonths(totalDeposit));
    }

    public static String lastPaidMonth(BigDecimal totalDeposit) {
        int paid = paidMonths(totalDeposit);
        return paid == 0 ? null : START_MONTH.plusMonths(paid - 1L).format(MONTH_FORMAT);
    }

    public static BigDecimal completionPercentage(BigDecimal totalDeposit) {
        int expected = expectedMonths();
        if (expected == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(paidMonths(totalDeposit))
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(expected), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal expectedAmountForOneMember() {
        return MONTHLY_AMOUNT.multiply(BigDecimal.valueOf(expectedMonths()));
    }

    public static String format(YearMonth month) {
        return month.format(MONTH_FORMAT);
    }

    public static List<YearMonth> expectedPeriods() {
        int expectedMonths = expectedMonths();
        List<YearMonth> periods = new ArrayList<>(expectedMonths);
        for (int index = 0; index < expectedMonths; index++) {
            periods.add(START_MONTH.plusMonths(index));
        }
        return periods;
    }
}
