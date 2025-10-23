package com.translateai.util;

import java.security.SecureRandom;

public class GenerateSecretKey {

    public static String generateHS512Key() {
        String allCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";

        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder(64);

        for (int i = 0; i < 128; i++) {
            int randomIndex = secureRandom.nextInt(allCharacters.length());
            char randomChar = allCharacters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String hs512Key = generateHS512Key();
        System.out.println("Generated HS512 Key: " + hs512Key);
    }
}
