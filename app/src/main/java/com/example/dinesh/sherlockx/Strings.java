package com.example.dinesh.sherlockx;

/**
 * Created by vikrant on 5/9/16.
 */
public class Strings {
    /**
     * Checks if a string is null or empty
     *
     * @param text
     * @return
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null ||  text.trim().length() == 0;
    }
}
