package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

/** One student's details. Design only: shows the sample row it was opened with. */
public class StudentDetailActivity extends AppCompatActivity {
    static final String EXTRA_ROW = "row";

    /** Name shared by a list row and this screen, so the row grows into the screen. */
    static String transitionName(Student student) {
        return "student_" + student.number;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Student student = Student.parse(getIntent().getStringExtra(EXTRA_ROW));
        Motion.growFrom(this, transitionName(student));
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_student_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ((TextView) findViewById(R.id.text_initials)).setText(student.initials());
        TextView name = findViewById(R.id.text_name);
        name.setText(student.fullName());
        ViewCompat.setAccessibilityHeading(name, true);
        ((TextView) findViewById(R.id.text_number)).setText(student.number);
        ((TextView) findViewById(R.id.text_year)).setText(student.year);
        findViewById(R.id.btn_copy).setOnClickListener(v -> copy(student.number));

        showRow(R.id.row_last, "Last name", student.last);
        showRow(R.id.row_first, "First name", student.first);
        showRow(R.id.row_middle, "Middle name", student.middle);
        showRow(R.id.row_year, "Year level", student.year);
        showRow(R.id.row_specialization, "Specialization", student.specialization);
    }

    private void showRow(int id, String label, String value) {
        View row = findViewById(id);
        ((TextView) row.findViewById(R.id.text_label)).setText(label);
        TextView text = row.findViewById(R.id.text_value);
        text.setText(value.isEmpty() ? "Not provided" : value);
        text.setEnabled(!value.isEmpty()); // missing values show in the quieter text color
    }

    private void copy(String number) {
        getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Student number", number));
        // Android 13 and newer shows its own "copied" message.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Snackbar.make(findViewById(R.id.main), "Student number copied", Snackbar.LENGTH_SHORT).show();
        }
    }
}
