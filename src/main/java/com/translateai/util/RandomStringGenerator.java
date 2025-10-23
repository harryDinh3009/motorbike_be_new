package com.translateai.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomStringGenerator {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String DIGITS = "0123456789";

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";

    private static final int OTP_LENGTH = 6;

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomString() {
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

    public static String generateRandomNumericString() {
        int length = 6;
        String numericSet = "0123456789";
        Random random = new Random();
        StringBuilder numericString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(numericSet.length());
            numericString.append(numericSet.charAt(randomIndex));
        }

        return numericString.toString();
    }

    public static String generateRandomPassword() {
        int passwordLength = 12;
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder(passwordLength);

        password.append(getRandomCharacter(LOWERCASE, random));
        password.append(getRandomCharacter(UPPERCASE, random));
        password.append(getRandomCharacter(DIGITS, random));
        password.append(getRandomCharacter(SPECIAL_CHARACTERS, random));

        String allCharacters = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARACTERS;
        for (int i = password.length(); i < passwordLength; i++) {
            password.append(getRandomCharacter(allCharacters, random));
        }

        String finalPassword = shuffleString(password.toString(), random);

        return finalPassword;
    }

    private static char getRandomCharacter(String characters, SecureRandom random) {
        int index = random.nextInt(characters.length());
        return characters.charAt(index);
    }

    private static String shuffleString(String string, SecureRandom random) {
        char[] array = string.toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = array[ i ];
            array[ i ] = array[ j ];
            array[ j ] = temp;
        }
        return new String(array);
    }

    public static String generateOTPForUser() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = random.nextInt(DIGITS.length());
            otp.append(DIGITS.charAt(index));
        }
        return otp.toString();
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+<>?";

    private static final int PASSWORD_LENGTH = 20;

    public static String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);


        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(randomIndex));
        }

        return password.toString();
    }

    public static void main(String[] args) {
        String password = generatePassword();
        System.out.println("Generated Password: " + password);
    }

}
