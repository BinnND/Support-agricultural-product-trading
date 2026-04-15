package com.example.htgdnss.common;

import java.text.DecimalFormat;

public class Utils {

    private static final DecimalFormat VND = new DecimalFormat("#,###");

    public static String formatVnd(double amount) {
        return VND.format(amount) + " đ";
    }
}
