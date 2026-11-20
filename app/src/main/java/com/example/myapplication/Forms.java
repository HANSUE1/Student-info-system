package com.example.myapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

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

    /** Removes a field's error as soon as the user changes it. */
    static void clearErrorOnEdit(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            EditText edit = field.getEditText();
            if (edit == null) continue;
            edit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    field.setError(null);
                }
            });
        }
    }

    /** Moves focus to the first field with an error and shakes it so the user sees where to look. */
    static void focusFirstError(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            if (field.getError() != null) {
                if (field.getEditText() != null) field.getEditText().requestFocus();
                Motion.shake(field);
                return;
            }
        }
    }
}
