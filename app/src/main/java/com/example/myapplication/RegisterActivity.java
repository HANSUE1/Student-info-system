package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

/** Create-account screen. Design only: input is checked, but no account is stored. */
public class RegisterActivity extends AppCompatActivity {
    public static final String EXTRA_USERNAME = "username";

    private TextInputLayout username;
    private TextInputLayout password;
    private TextInputLayout confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btn_back_to_login).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        username = findViewById(R.id.field_username);
        password = findViewById(R.id.field_password);
        confirm = findViewById(R.id.field_confirm);
        Forms.clearErrorOnEdit(username, password, confirm);
        Forms.validateOnBlur(username, () -> Validator.username(Forms.text(username)));
        Forms.validateOnBlur(password, () -> Validator.newPassword(Forms.raw(password)));
        Forms.validateOnBlur(confirm, () -> Validator.confirmPassword(Forms.raw(password), Forms.raw(confirm)));
        if (password.getEditText() != null) Forms.onEdit(password.getEditText(), this::showStrength);
        showStrength();

        findViewById(R.id.btn_create).setOnClickListener(v -> register());
        if (confirm.getEditText() != null) {
            confirm.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                if (actionId != EditorInfo.IME_ACTION_DONE) return false;
                register();
                return true;
            });
        }
    }

    /** Updates the strength bar and ticks each password rule while the user types. */
    private void showStrength() {
        String pw = Forms.raw(password);
        int score = Validator.passwordStrength(pw);
        LinearProgressIndicator bar = findViewById(R.id.progress_strength);
        bar.setIndicatorColor(ContextCompat.getColor(this,
                score == 4 ? R.color.success : score == 3 ? R.color.md_primary : R.color.warning));
        bar.setProgressCompat(score, true);
        TextView label = findViewById(R.id.text_strength);
        label.setText(pw.isEmpty() ? "Password strength" : "Password strength: "
                + (score == 4 ? "strong" : score == 3 ? "good" : "weak"));
        showRule(R.id.rule_length, pw.length() >= 8);
        showRule(R.id.rule_letter, Validator.hasLetter(pw));
        showRule(R.id.rule_digit, Validator.hasDigit(pw));
    }

    private void showRule(int id, boolean met) {
        TextView rule = findViewById(id);
        rule.setCompoundDrawablesRelativeWithIntrinsicBounds(
                met ? R.drawable.ic_rule_met : R.drawable.ic_rule_pending, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(rule, ContextCompat.getColorStateList(this,
                met ? R.color.success : R.color.md_outline));
        ViewCompat.setStateDescription(rule, met ? "met" : "not met yet");
    }

    private void register() {
        String pw = Forms.raw(password);
        boolean valid = Forms.show(username, Validator.username(Forms.text(username)))
                & Forms.show(password, Validator.newPassword(pw))
                & Forms.show(confirm, Validator.confirmPassword(pw, Forms.raw(confirm))); // & so every error shows
        if (!valid) {
            Forms.focusFirstError(username, password, confirm);
            return;
        }
        // Nothing is stored. Hand the username back so the login screen can prefill it.
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_USERNAME, Forms.text(username)));
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        Motion.back(this);
    }
}
