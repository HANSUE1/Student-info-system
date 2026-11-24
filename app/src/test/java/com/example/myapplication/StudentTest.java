package com.example.myapplication;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StudentTest {
    private final List<Student> all = Arrays.asList(
            Student.parse("2023-0000123|Dela Cruz|Juan|Santos|3rd Year|Software Engineering"),
            Student.parse("2025-0000789|Reyes|Jose||1st Year|"),
            Student.parse("2022-0000321|Santos|Ana|Cruz|4th Year|Network Security"),
            Student.parse("2025-0000402|Aquino|Bea|Ramos|1st Year|Data Science"));

    private String found(String search, String filter, Student.Sort sort) {
        StringBuilder names = new StringBuilder();
        for (Student s : Student.query(all, search, filter, sort)) names.append(s.last).append(';');
        return names.toString();
    }

    @Test
    public void parse() {
        assertEquals("Dela Cruz, Juan Santos", all.get(0).fullName());
        assertEquals("Reyes, Jose", all.get(1).fullName()); // no middle name
        assertEquals("JD", all.get(0).initials());
        assertEquals("", all.get(1).specialization);
    }

    @Test
    public void searchMatchesEveryWordInAnyOrder() {
        assertEquals("Dela Cruz;", found("  juan DELA cruz ", Student.ALL, Student.Sort.NAME_AZ));
        assertEquals("Dela Cruz;Santos;", found("santos", Student.ALL, Student.Sort.NAME_AZ));
        assertEquals("Santos;", found("0000321", Student.ALL, Student.Sort.NAME_AZ));
        assertEquals("", found("juan reyes", Student.ALL, Student.Sort.NAME_AZ));
    }

    @Test
    public void filterAndSearchCombine() {
        assertEquals("Aquino;Reyes;", found("", "1st Year", Student.Sort.NAME_AZ));
        assertEquals("Reyes;", found("jose", "1st Year", Student.Sort.NAME_AZ));
        assertEquals("", found("jose", "3rd Year", Student.Sort.NAME_AZ));
        assertEquals("Reyes;", found("", Student.NO_SPECIALIZATION, Student.Sort.NAME_AZ));
    }

    @Test
    public void sorts() {
        assertEquals("Aquino;Dela Cruz;Reyes;Santos;", found("", Student.ALL, Student.Sort.NAME_AZ));
        assertEquals("Santos;Reyes;Dela Cruz;Aquino;", found("", Student.ALL, Student.Sort.NAME_ZA));
        assertEquals("Santos;Dela Cruz;Aquino;Reyes;", found("", Student.ALL, Student.Sort.NUMBER));
        assertEquals("Aquino;Reyes;Dela Cruz;Santos;", found("", Student.ALL, Student.Sort.YEAR));
    }
}
