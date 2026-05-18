package com.example.pantallas.utils;

import java.time.LocalDate;

public class ValidationUtil {

    private ValidationUtil() {}

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidNumber(String str) {
        if (isNullOrEmpty(str)) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveNumber(String str) {
        if (!isValidNumber(str)) return false;
        return Double.parseDouble(str) > 0;
    }
    
    public static boolean isValidInteger(String str) {
        if (isNullOrEmpty(str)) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String str) {
        if (!isValidInteger(str)) return false;
        return Integer.parseInt(str) > 0;
    }

    public static boolean isDateValid(LocalDate date) {
        return date != null;
    }
    
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
}
