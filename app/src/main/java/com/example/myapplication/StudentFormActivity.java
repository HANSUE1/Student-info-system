package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/** Add Student form (design only: Save just closes the screen, nothing is stored). */
public class StudentFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_student_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner yearLevel = findViewById(R.id.spinner_year_level);
        ArrayAdapter<CharSequence> years = ArrayAdapter.createFromResource(
                this, R.array.year_levels, android.R.layout.simple_spinner_item);
        years.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearLevel.setAdapter(years);

        findViewById(R.id.btn_save).setOnClickListener(v -> finish());
    }
}
