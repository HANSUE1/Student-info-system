package com.example.myapplication;

import java.util.regex.Pattern;

/**
 * Input rules for every form. Each method returns the error message to show, or null when the value is fine.
 * Plain Java (no Android classes) so the rules are unit-tested in ValidatorTest.
 */
public final class Validator {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._]+");
    private static final Pattern STUDENT_NO = Pattern.compile("\\d{4}-\\d{7}");
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
        if (!hasLetter(s) || !hasDigit(s)) return "Include at least one letter and one number";
        return null;
    }

    public static boolean hasLetter(String s) {
        return s.matches(".*[A-Za-z].*");
    }

    public static boolean hasDigit(String s) {
        return s.matches(".*\\d.*");
    }

    /** 0 to 4: one point per rule of newPassword that is met, and one more for 12+ characters. */
    public static int passwordStrength(String s) {
        int met = (s.length() >= 8 ? 1 : 0) + (hasLetter(s) ? 1 : 0) + (hasDigit(s) ? 1 : 0);
        return met == 3 && s.length() >= 12 ? 4 : met;
    }

    public static String confirmPassword(String password, String confirm) {
        if (confirm.isEmpty()) return "Re-enter your password";
        if (!confirm.equals(password)) return "Passwords do not match";
        return null;
    }

    public static String studentNumber(String s) {
        if (s.isEmpty()) return "Enter the student number";
        if (!STUDENT_NO.matcher(s).matches()) return "Use the format YYYY-NNNNNNN, e.g. 2025-0012345";
        return null;
    }

    /** Keeps digits only (11 at most) and puts the hyphen in: "20250012345" becomes "2025-0012345". */
    public static String formatStudentNumber(String s) {
        String digits = s.replaceAll("\\D", "");
        if (digits.length() > 11) digits = digits.substring(0, 11);
        return digits.length() <= 4 ? digits : digits.substring(0, 4) + "-" + digits.substring(4);
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
