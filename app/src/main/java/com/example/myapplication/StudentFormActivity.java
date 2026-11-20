package com.example.myapplication;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

/** Add Student form. Design only: input is checked, then the screen closes without storing anything. */
public class StudentFormActivity extends AppCompatActivity {
    private TextInputLayout studentNo;
    private TextInputLayout lastName;
    private TextInputLayout firstName;
    private TextInputLayout middleName;
    private TextInputLayout yearLevel;
    private TextInputLayout specialization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This whole screen grows out of the list's Add button, and shrinks back into it on close.
        findViewById(android.R.id.content).setTransitionName("add_student");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementEnterTransition(containerTransform(450));
        getWindow().setSharedElementReturnTransition(containerTransform(350));
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_student_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studentNo = findViewById(R.id.field_student_no);
        lastName = findViewById(R.id.field_last_name);
        firstName = findViewById(R.id.field_first_name);
        middleName = findViewById(R.id.field_middle_name);
        yearLevel = findViewById(R.id.field_year_level);
        specialization = findViewById(R.id.field_specialization);
        Forms.clearErrorOnEdit(allFields());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        if (specialization.getEditText() != null) {
            specialization.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                if (actionId != EditorInfo.IME_ACTION_DONE) return false;
                save();
                return true;
            });
        }

        // Back with unsaved input asks first, so typed details are not lost by accident.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!hasInput()) {
                    leave(this);
                    return;
                }
                new MaterialAlertDialogBuilder(StudentFormActivity.this)
                        .setTitle("Discard this student?")
                        .setMessage("The details you typed will be lost.")
                        .setNegativeButton("Keep editing", null)
                        .setPositiveButton("Discard", (dialog, which) -> leave(this))
                        .show();
            }
        });
    }

    private void leave(OnBackPressedCallback callback) {
        callback.setEnabled(false);
        getOnBackPressedDispatcher().onBackPressed(); // default back: plays the shrink-back animation
    }

    private void save() {
        boolean valid = Forms.show(studentNo, Validator.studentNumber(Forms.text(studentNo)))
                & Forms.show(lastName, Validator.name(Forms.text(lastName), "last name", true))
                & Forms.show(firstName, Validator.name(Forms.text(firstName), "first name", true))
                & Forms.show(middleName, Validator.name(Forms.text(middleName), "middle name", false))
                & Forms.show(yearLevel, Validator.yearLevel(Forms.text(yearLevel)))
                & Forms.show(specialization, Validator.specialization(Forms.text(specialization))); // & so every error shows
        if (!valid) {
            Forms.focusFirstError(allFields());
            return;
        }
        setResult(RESULT_OK); // the list screen shows the "saved" message
        finishAfterTransition();
    }

    private boolean hasInput() {
        for (TextInputLayout field : allFields()) {
            if (!Forms.text(field).isEmpty()) return true;
        }
        return false;
    }

    private TextInputLayout[] allFields() {
        return new TextInputLayout[]{studentNo, lastName, firstName, middleName, yearLevel, specialization};
    }

    private MaterialContainerTransform containerTransform(long durationMs) {
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(android.R.id.content);
        transform.setDuration(durationMs);
        // Solid surface color while morphing, so nothing shows through the growing shape.
        transform.setAllContainerColors(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorSurface, 0));
        return transform;
    }
}
