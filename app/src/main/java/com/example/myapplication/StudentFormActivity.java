package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.inputmethod.EditorInfo;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.HapticFeedbackConstantsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

/** Add Student form. Design only: input is checked, then the screen closes without storing anything. */
public class StudentFormActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextInputLayout studentNo;
    private TextInputLayout lastName;
    private TextInputLayout firstName;
    private TextInputLayout middleName;
    private TextInputLayout yearLevel;
    private TextInputLayout specialization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This whole screen grows out of the list's Add button, and shrinks back into it on close.
        Motion.growFrom(this, "add_student");
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

        Forms.validateOnBlur(studentNo, () -> Validator.studentNumber(Forms.text(studentNo)));
        Forms.validateOnBlur(lastName, () -> Validator.name(Forms.text(lastName), "last name", true));
        Forms.validateOnBlur(firstName, () -> Validator.name(Forms.text(firstName), "first name", true));
        Forms.validateOnBlur(middleName, () -> Validator.name(Forms.text(middleName), "middle name", false));
        Forms.validateOnBlur(specialization, () -> Validator.specialization(Forms.text(specialization)));

        toolbar = findViewById(R.id.toolbar);
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
        // Off while the form is empty, so the system back preview still works.
        OnBackPressedCallback confirmDiscard = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                new MaterialAlertDialogBuilder(StudentFormActivity.this)
                        .setTitle("Discard this student?")
                        .setMessage("The details you typed will be lost.")
                        .setNegativeButton("Keep editing", null)
                        .setPositiveButton("Discard", (dialog, which) -> leave(this))
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, confirmDiscard);

        if (studentNo.getEditText() != null) Forms.onEdit(studentNo.getEditText(), this::formatStudentNumber);
        for (TextInputLayout field : allFields()) {
            if (field.getEditText() == null) continue;
            Forms.onEdit(field.getEditText(), () -> {
                confirmDiscard.setEnabled(hasInput());
                showProgress();
            });
        }
        showProgress();
    }

    /** Adds the hyphen while the user types, and keeps the cursor next to the same digit. */
    private void formatStudentNumber() {
        Editable s = studentNo.getEditText().getText();
        String formatted = Validator.formatStudentNumber(s.toString());
        if (formatted.contentEquals(s)) return;
        int cursor = Math.max(0, Selection.getSelectionStart(s));
        int digitsBefore = s.subSequence(0, cursor).toString().replaceAll("\\D", "").length();
        s.replace(0, s.length(), formatted);
        Selection.setSelection(s, Math.min(s.length(), digitsBefore + (digitsBefore > 4 ? 1 : 0)));
    }

    /** Ticks each valid required field and moves the progress line under the toolbar. */
    private void showProgress() {
        boolean number = Validator.studentNumber(Forms.text(studentNo)) == null;
        boolean last = Validator.name(Forms.text(lastName), "last name", true) == null;
        boolean first = Validator.name(Forms.text(firstName), "first name", true) == null;
        boolean year = Validator.yearLevel(Forms.text(yearLevel)) == null;
        studentNo.setEndIconVisible(number);
        lastName.setEndIconVisible(last);
        firstName.setEndIconVisible(first);
        int done = (number ? 1 : 0) + (last ? 1 : 0) + (first ? 1 : 0) + (year ? 1 : 0);
        toolbar.setSubtitle(done + " of 4 required fields");
        LinearProgressIndicator progress = findViewById(R.id.progress_required);
        progress.setProgressCompat(done, true);
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
        ViewCompat.performHapticFeedback(findViewById(R.id.btn_save), HapticFeedbackConstantsCompat.CONFIRM);
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
}
