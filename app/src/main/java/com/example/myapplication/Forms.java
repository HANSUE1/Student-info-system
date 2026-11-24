package com.example.myapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.core.view.HapticFeedbackConstantsCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Supplier;

/** Small helpers to read Material text fields and show their errors. */
final class Forms {
    private Forms() {
    }

    /** Field text with surrounding spaces removed. */
    static String text(TextInputLayout field) {
        return raw(field).trim();
    }

    /** Field text exactly as typed (for passwords). */
    static String raw(TextInputLayout field) {
        EditText edit = field.getEditText();
        return edit == null || edit.getText() == null ? "" : edit.getText().toString();
    }

    /** Shows the error under the field, or clears it when error is null. Returns true when the field is valid. */
    static boolean show(TextInputLayout field, String error) {
        field.setError(error);
        return error == null;
    }

    /** Runs the action after every change to the field's text. */
    static void onEdit(EditText edit, Runnable action) {
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                action.run();
            }
        });
    }

    /** Removes a field's error as soon as the user changes it. */
    static void clearErrorOnEdit(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            if (field.getEditText() != null) onEdit(field.getEditText(), () -> field.setError(null));
        }
    }

    /** Checks a filled-in field when the user leaves it. Empty fields wait for submit. */
    static void validateOnBlur(TextInputLayout field, Supplier<String> rule) {
        EditText edit = field.getEditText();
        if (edit == null) return;
        View.OnFocusChangeListener previous = edit.getOnFocusChangeListener();
        edit.setOnFocusChangeListener((v, hasFocus) -> {
            if (previous != null) previous.onFocusChange(v, hasFocus);
            if (!hasFocus && !raw(field).isEmpty()) show(field, rule.get());
        });
    }

    /** Moves focus to the first field with an error and shakes it so the user sees where to look. */
    static void focusFirstError(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            if (field.getError() != null) {
                if (field.getEditText() != null) field.getEditText().requestFocus();
                Motion.shake(field);
                ViewCompat.performHapticFeedback(field, HapticFeedbackConstantsCompat.REJECT);
                return;
            }
        }
    }
}
