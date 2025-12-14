package com.ucop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtil {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat VND_FORMAT = NumberFormat.getCurrencyInstance(VI_LOCALE);

    private CurrencyUtil() {
    }

    public static String format(BigDecimal value) {
        if (value == null) return "";
        return VND_FORMAT.format(value);
    }

    public static String formatNumber(Number value) {
        if (value == null) return "";
        return VND_FORMAT.format(value);
    }
}
