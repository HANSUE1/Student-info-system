package com.example.myapplication;

import java.util.regex.Pattern;

/**
 * Input rules for every form. Each method returns the error message to show, or null when the value is fine.
 * Plain Java (no Android classes) so the rules are unit-tested in ValidatorTest.
 */
public final class Validator {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._]+");
    private static final Pattern STUDENT_NO = Pattern.compile("\\d{4}-\\d{4,6}");
    // At least one letter; letters (including ñ and accents), spaces, hyphens, apostrophes and periods allowed:
    // "Dela Cruz", "O'Neil", "Ma. Lourdes", "Peña".
    private static final Pattern NAME = Pattern.compile("(?=.*\\p{L})[\\p{L} .'-]+");

    private Validator() {
    }

    public static String required(String value, String what) {
        return value.isEmpty() ? "Enter " + what : null;
    }

    public static String username(String s) {
        if (s.isEmpty()) return "Enter a username";
        if (s.length() < 4 || s.length() > 20) return "Use 4 to 20 characters";
        if (!USERNAME.matcher(s).matches()) return "Use letters, numbers, . or _ only (no spaces)";
        return null;
    }

    public static String newPassword(String s) {
        if (s.isEmpty()) return "Enter a password";
        if (s.length() < 8) return "Use at least 8 characters";
        if (!s.matches(".*[A-Za-z].*") || !s.matches(".*\\d.*")) return "Include at least one letter and one number";
        return null;
    }

    public static String confirmPassword(String password, String confirm) {
        if (confirm.isEmpty()) return "Re-enter your password";
        if (!confirm.equals(password)) return "Passwords do not match";
        return null;
    }

    public static String studentNumber(String s) {
        if (s.isEmpty()) return "Enter the student number";
        if (!STUDENT_NO.matcher(s).matches()) return "Use the format YYYY-NNNNN, e.g. 2025-00123";
        return null;
    }

    public static String name(String s, String what, boolean required) {
        if (s.isEmpty()) return required ? "Enter the " + what : null;
        if (s.length() > 50) return "Keep it under 50 characters";
        if (!NAME.matcher(s).matches()) return "Use letters only (spaces, - ' and . are allowed)";
        return null;
    }

    public static String yearLevel(String s) {
        return s.isEmpty() ? "Select a year level" : null;
    }

    public static String specialization(String s) {
        return s.length() > 60 ? "Keep it under 60 characters" : null;
    }
}
