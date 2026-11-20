package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

/** Login screen. Design only: any filled-in username and password is accepted. */
public class LoginActivity extends AppCompatActivity {
    private TextInputLayout username;
    private TextInputLayout password;

    // Coming back from a successful registration: prefill the username and say so.
    private final ActivityResultLauncher<Intent> registerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                if (username.getEditText() != null) {
                    username.getEditText().setText(result.getData().getStringExtra(RegisterActivity.EXTRA_USERNAME));
                }
                if (password.getEditText() != null) password.getEditText().requestFocus();
                Snackbar.make(findViewById(R.id.main), "Account created. Log in to continue.", Snackbar.LENGTH_LONG)
                        .show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.field_username);
        password = findViewById(R.id.field_password);
        Forms.clearErrorOnEdit(username, password);

        View loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(v -> login());
        if (password.getEditText() != null) {
            password.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                if (actionId != EditorInfo.IME_ACTION_DONE) return false;
                login();
                return true;
            });
        }
        findViewById(R.id.btn_register).setOnClickListener(v -> {
            registerLauncher.launch(new Intent(this, RegisterActivity.class));
            Motion.forward(this);
        });

        if (savedInstanceState == null) { // play the entrance once, not again after rotating the phone
            View logo = findViewById(R.id.logo);
            Motion.riseIn(logo, findViewById(R.id.text_title), findViewById(R.id.text_subtitle),
                    username, password, loginButton, findViewById(R.id.row_register));
            logo.setScaleX(0.6f);
            logo.setScaleY(0.6f);
            logo.animate().scaleX(1f).scaleY(1f);
        }
    }

    private void login() {
        boolean valid = Forms.show(username, Validator.required(Forms.text(username), "your username"))
                & Forms.show(password, Validator.required(Forms.raw(password), "your password")); // & so both errors show
        if (!valid) {
            Forms.focusFirstError(username, password);
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        Motion.forward(this);
        finish();
    }
}
