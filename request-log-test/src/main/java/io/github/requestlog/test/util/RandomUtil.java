package io.github.requestlog.test.util;

import java.security.SecureRandom;


/**
 * Utility class for generating random values.
 */
public class RandomUtil {


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    /**
     * Generate a random string of the specified length.
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }


    /**
     * Generate a random integer within the specified range.
     */
    public static int randomInt(int min, int max) {
        return SECURE_RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * Generate a random boolean value.
     */
    public static boolean randomBoolean() {
        return SECURE_RANDOM.nextBoolean();
    }

}
