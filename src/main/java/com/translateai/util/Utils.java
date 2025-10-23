package com.translateai.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Utils {

    public static Date convertStringToDate(String dateString, String format) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertDateToString(Date date, String format) {
        if (date == null) {
            return null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateRandomPassword() {
        int length = 8;
        String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characterSet.length());
            randomString.append(characterSet.charAt(randomIndex));
        }

        return randomString.toString();
    }

}
