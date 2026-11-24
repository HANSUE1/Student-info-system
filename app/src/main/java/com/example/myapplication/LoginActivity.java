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
        // The brand panel is dark in both themes, so status bar icons stay light here.
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        View sheet = findViewById(R.id.sheet);
        int sheetPadding = sheet.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(bars.left, bars.top, bars.right, keyboard);
            // Keyboard closed: the sheet itself runs under the navigation bar.
            sheet.setPadding(sheet.getPaddingLeft(), sheet.getPaddingTop(), sheet.getPaddingRight(),
                    sheetPadding + (keyboard > 0 ? 0 : bars.bottom));
            return insets;
        });

        username = findViewById(R.id.field_username);
        password = findViewById(R.id.field_password);
        Forms.clearErrorOnEdit(username, password);

        findViewById(R.id.btn_login).setOnClickListener(v -> login());
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
            Motion.riseIn(sheet);
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
