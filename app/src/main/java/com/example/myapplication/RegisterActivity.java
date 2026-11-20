package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
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

        findViewById(R.id.btn_create).setOnClickListener(v -> register());
        if (confirm.getEditText() != null) {
            confirm.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                if (actionId != EditorInfo.IME_ACTION_DONE) return false;
                register();
                return true;
            });
        }
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
