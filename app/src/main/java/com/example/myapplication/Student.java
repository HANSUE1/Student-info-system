package com.example.myapplication;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * One sample student, plus the search, filter and sort rules of the list.
 * Plain Java (no Android classes) so the rules are unit-tested in StudentTest.
 */
final class Student {
    /** Filter values besides a year level such as "1st Year". */
    static final String ALL = "";
    static final String NO_SPECIALIZATION = "No specialization";

    enum Sort {
        NAME_AZ("Name, A to Z"), NAME_ZA("Name, Z to A"), NUMBER("Student number"), YEAR("Year level");

        final String label;

        Sort(String label) {
            this.label = label;
        }
    }

    /** The line from strings.xml this student came from; also how a student travels between screens. */
    final String row;
    final String number, last, first, middle, year, specialization;

    private Student(String row, String[] part) {
        this.row = row;
        number = part[0];
        last = part[1];
        first = part[2];
        middle = part[3];
        year = part[4];
        specialization = part[5];
    }

    /** Reads "student no|last name|first name|middle name|year level|specialization". */
    static Student parse(String row) {
        return new Student(row, row.split("\\|", -1));
    }

    String fullName() {
        return last + ", " + first + (middle.isEmpty() ? "" : " " + middle);
    }

    String initials() {
        return initial(first) + initial(last);
    }

    private static String initial(String name) {
        return name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    boolean passes(String filter) {
        if (filter.equals(ALL)) return true;
        if (filter.equals(NO_SPECIALIZATION)) return specialization.isEmpty();
        return year.equals(filter);
    }

    /** Every typed word must appear somewhere, so "juan dela cruz" and "cruz 2023" both work. */
    private boolean matches(String search) {
        String text = String.join(" ", number, last, first, middle, year, specialization).toLowerCase(Locale.ROOT);
        for (String word : search.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!text.contains(word)) return false;
        }
        return true;
    }

    /** The students that match both the search and the filter, in the chosen order. */
    static List<Student> query(List<Student> all, String search, String filter, Sort sort) {
        List<Student> shown = new ArrayList<>();
        for (Student s : all) {
            if (s.passes(filter) && s.matches(search)) shown.add(s);
        }
        Collator collator = Collator.getInstance(); // puts Ñ and accents where readers expect them
        Comparator<Student> byName = (a, b) -> collator.compare(a.fullName(), b.fullName());
        switch (sort) {
            case NAME_ZA:
                shown.sort(byName.reversed());
                break;
            case NUMBER:
                shown.sort(Comparator.comparing(s -> s.number));
                break;
            case YEAR:
                shown.sort(Comparator.comparing((Student s) -> s.year).thenComparing(byName));
                break;
            default:
                shown.sort(byName);
        }
        return shown;
    }
}
