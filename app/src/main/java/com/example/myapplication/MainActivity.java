package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Student list screen. Design only: rows come from the sample_students array in strings.xml. */
public class MainActivity extends AppCompatActivity {
    // One student = {student no, last name, first name, middle name, year level, specialization}
    private final List<String[]> allStudents = new ArrayList<>();
    private final List<String[]> shownStudents = new ArrayList<>();
    private ArrayAdapter<String[]> adapter;
    private TextView emptyView;
    private View addButton;

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK) return;
                Snackbar.make(findViewById(R.id.main), "Student saved (preview only, nothing is stored)",
                        Snackbar.LENGTH_LONG).setAnchorView(addButton).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // The Add button grows into the form screen (Material container transform).
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        for (String row : getResources().getStringArray(R.array.sample_students)) {
            allStudents.add(row.split("\\|", -1));
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle(allStudents.size() + " students enrolled");
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != R.id.action_logout) return false;
            confirmLogout();
            return true;
        });

        emptyView = findViewById(R.id.text_empty);
        ListView list = findViewById(R.id.list_students);
        adapter = new StudentAdapter();
        list.setAdapter(adapter);
        list.setEmptyView(emptyView);
        if (savedInstanceState == null && Motion.enabled(this)) {
            list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.list_layout_enter));
        }

        EditText search = findViewById(R.id.edit_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        addButton = findViewById(R.id.fab_add);
        addButton.setOnClickListener(v -> formLauncher.launch(new Intent(this, StudentFormActivity.class),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, addButton, "add_student")));

        filter(search.getText() == null ? "" : search.getText().toString());
    }

    private void filter(String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        shownStudents.clear();
        for (String[] s : allStudents) {
            if (TextUtils.join(" ", s).toLowerCase(Locale.ROOT).contains(needle)) shownStudents.add(s);
        }
        emptyView.setText(needle.isEmpty()
                ? "No students yet.\nTap Add student to create one."
                : "No students match “" + query.trim() + "”.\nCheck the spelling or search by student number.");
        adapter.notifyDataSetChanged();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log out?")
                .setMessage("You will need to sign in again to manage student records.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    Motion.back(this);
                    finish();
                })
                .show();
    }

    private class StudentAdapter extends ArrayAdapter<String[]> {
        StudentAdapter() {
            super(MainActivity.this, 0, shownStudents);
        }

        @NonNull
        @Override
        public View getView(int position, View row, @NonNull ViewGroup parent) {
            if (row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.item_student, parent, false);
            }
            String[] s = shownStudents.get(position);
            String no = s[0], last = s[1], first = s[2], middle = s[3], year = s[4], specialization = s[5];

            ((TextView) row.findViewById(R.id.text_initials)).setText(initial(first) + initial(last));
            ((TextView) row.findViewById(R.id.text_name)).setText(
                    last + ", " + first + (middle.isEmpty() ? "" : " " + middle));
            ((TextView) row.findViewById(R.id.text_number)).setText(no + "  ·  " + year);
            TextView spec = row.findViewById(R.id.text_specialization);
            spec.setText(specialization);
            spec.setVisibility(specialization.isEmpty() ? View.GONE : View.VISIBLE);
            return row;
        }

        private String initial(String name) {
            return name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        }
    }
}
