package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ValidatorTest {
    @Test
    public void username() {
        assertNull(Validator.username("juan_dc"));
        assertEquals("Enter a username", Validator.username(""));
        assertNotNull(Validator.username("abc"));                    // too short
        assertNotNull(Validator.username("abcdefghijklmnopqrstu"));  // 21 chars
        assertNotNull(Validator.username("juan dc"));                // space
    }

    @Test
    public void newPassword() {
        assertNull(Validator.newPassword("secret123"));
        assertNotNull(Validator.newPassword(""));
        assertNotNull(Validator.newPassword("abc1"));      // too short
        assertNotNull(Validator.newPassword("abcdefgh"));  // no number
        assertNotNull(Validator.newPassword("12345678"));  // no letter
    }

    @Test
    public void confirmPassword() {
        assertNull(Validator.confirmPassword("secret123", "secret123"));
        assertEquals("Re-enter your password", Validator.confirmPassword("secret123", ""));
        assertEquals("Passwords do not match", Validator.confirmPassword("secret123", "secret124"));
    }

    @Test
    public void studentNumber() {
        assertNull(Validator.studentNumber("2025-00123"));
        assertNull(Validator.studentNumber("2025-1234"));
        assertNotNull(Validator.studentNumber(""));
        assertNotNull(Validator.studentNumber("202500123"));
        assertNotNull(Validator.studentNumber("25-00123"));
        assertNotNull(Validator.studentNumber("2025-ABCDE"));
    }

    @Test
    public void name() {
        assertNull(Validator.name("Dela Cruz", "last name", true));
        assertNull(Validator.name("O'Neil-Peña", "last name", true));
        assertNull(Validator.name("Ma. Lourdes", "first name", true));
        assertNull(Validator.name("", "middle name", false));              // optional
        assertEquals("Enter the last name", Validator.name("", "last name", true));
        assertNotNull(Validator.name("Juan2", "first name", true));        // digit
        assertNotNull(Validator.name("--", "first name", true));           // no letter
        assertNotNull(Validator.name("A".repeat(51), "first name", true)); // too long
    }

    @Test
    public void optionalAndRequiredFields() {
        assertNull(Validator.yearLevel("3rd Year"));
        assertNotNull(Validator.yearLevel(""));
        assertNull(Validator.specialization(""));
        assertNotNull(Validator.specialization("A".repeat(61)));
        assertNull(Validator.required("x", "your username"));
        assertEquals("Enter your username", Validator.required("", "your username"));
    }
}
