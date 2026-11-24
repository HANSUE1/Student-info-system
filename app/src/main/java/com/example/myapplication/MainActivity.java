package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/** Student list screen. Design only: rows come from the sample_students array in strings.xml. */
public class MainActivity extends AppCompatActivity {
    private static final String STATE_FILTER = "filter";
    private static final String STATE_SORT = "sort";
    private static final int[] YEAR_COLORS =
            {R.color.year_1, R.color.year_2, R.color.year_3, R.color.year_4, R.color.year_5};

    private final List<Student> students = new ArrayList<>();
    private String filter = Student.ALL;
    private Student.Sort sort = Student.Sort.NAME_AZ;

    private StudentAdapter adapter;
    private AppBarLayout appBar;
    private RecyclerView list;
    private EditText search;
    private ChipGroup chips;
    private LinearLayout yearBar;
    private MaterialButton sortButton;
    private TextView resultCount;
    private View emptyView;
    private TextView emptyText;
    private ExtendedFloatingActionButton addButton;

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK) return;
                Snackbar.make(findViewById(R.id.main), "Student saved (preview only, nothing is stored)",
                        Snackbar.LENGTH_LONG).setAnchorView(addButton).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Motion.growOut(this); // the Add button and the rows grow into the screens they open
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        for (String row : getResources().getStringArray(R.array.sample_students)) {
            students.add(Student.parse(row));
        }
        if (savedInstanceState != null) {
            filter = savedInstanceState.getString(STATE_FILTER, Student.ALL);
            sort = Student.Sort.values()[savedInstanceState.getInt(STATE_SORT)];
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != R.id.action_logout) return false;
            confirmLogout();
            return true;
        });

        appBar = findViewById(R.id.app_bar);
        emptyView = findViewById(R.id.view_empty);
        emptyText = findViewById(R.id.text_empty);
        resultCount = findViewById(R.id.text_result_count);
        findViewById(R.id.btn_clear_filters).setOnClickListener(v -> clearFilters());

        sortButton = findViewById(R.id.btn_sort);
        sortButton.setOnClickListener(v -> showSortMenu());

        addButton = findViewById(R.id.fab_add);
        addButton.setOnClickListener(v -> formLauncher.launch(new Intent(this, StudentFormActivity.class),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, addButton, "add_student")));

        setUpList();
        setUpChips();

        search = findViewById(R.id.edit_search);
        Forms.onEdit(search, this::refresh);
        search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            WindowCompat.getInsetsController(getWindow(), v).hide(WindowInsetsCompat.Type.ime());
            return true;
        });

        refresh();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER, filter);
        outState.putInt(STATE_SORT, sort.ordinal());
    }

    private void setUpList() {
        adapter = new StudentAdapter((student, row) -> startActivity(
                new Intent(this, StudentDetailActivity.class).putExtra(StudentDetailActivity.EXTRA_ROW, student.row),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, row, row.getTransitionName()).toBundle()));
        list = findViewById(R.id.list_students);
        list.setAdapter(adapter);

        View divider = findViewById(R.id.app_bar_divider);
        divider.setAlpha(0f);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
                if (dy > 0) addButton.shrink();
                if (dy < 0) addButton.extend();
                divider.setAlpha(view.canScrollVertically(-1) ? 1f : 0f);
            }
        });
    }

    /** One bar segment and one chip per year level. The chips double as the bar's legend and totals. */
    private void setUpChips() {
        chips = findViewById(R.id.chips_filter);
        yearBar = findViewById(R.id.bar_years);
        addChip(Student.ALL, "All", 0);

        String[] years = getResources().getStringArray(R.array.year_levels);
        StringBuilder summary = new StringBuilder("Students by year level.");
        for (int i = 0; i < years.length; i++) {
            int color = ContextCompat.getColor(this, YEAR_COLORS[i]);
            int count = addChip(years[i], years[i], color);
            summary.append(' ').append(years[i]).append(": ").append(count).append('.');
            if (count == 0) continue;
            View segment = new View(this);
            segment.setTag(years[i]);
            segment.setBackgroundResource(R.drawable.bg_year_segment);
            segment.setBackgroundTintList(ColorStateList.valueOf(color));
            yearBar.addView(segment, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, count));
        }
        yearBar.setContentDescription(summary);
        addChip(Student.NO_SPECIALIZATION, Student.NO_SPECIALIZATION, 0);

        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            filter = (String) group.findViewById(ids.get(0)).getTag();
            refresh();
        });
    }

    /** Adds a filter chip showing how many students it holds. Color 0 means no year dot. Returns that count. */
    private int addChip(String value, String label, int color) {
        int count = Student.query(students, "", value, sort).size();
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, chips, false);
        chip.setId(View.generateViewId());
        chip.setTag(value);
        chip.setText(getString(R.string.filter_with_count, label, count));
        chip.setChipIconVisible(color != 0);
        if (color != 0) {
            chip.setChipIconTint(ColorStateList.valueOf(color));
            // The check mark replaces the dot instead of drawing over it.
            chip.setOnCheckedChangeListener((c, checked) -> chip.setChipIconVisible(!checked));
        }
        chip.setChecked(value.equals(filter));
        chips.addView(chip);
        return count;
    }

    private void clearFilters() {
        search.setText("");
        chips.check(chips.getChildAt(0).getId());
    }

    private void showSortMenu() {
        PopupMenu menu = new PopupMenu(this, sortButton);
        for (Student.Sort option : Student.Sort.values()) {
            menu.getMenu().add(0, option.ordinal(), option.ordinal(), option.label);
        }
        menu.getMenu().setGroupCheckable(0, true, true);
        menu.getMenu().findItem(sort.ordinal()).setChecked(true);
        menu.setOnMenuItemClickListener(item -> {
            sort = Student.Sort.values()[item.getItemId()];
            refresh();
            return true;
        });
        menu.show();
    }

    /** Re-runs search, filter and sort together and updates everything that depends on them. */
    private void refresh() {
        String query = search.getText().toString().trim();
        List<Student> shown = Student.query(students, query, filter, sort);

        // Keep the scroll position on first load (rotation); jump to the top when the result changes.
        boolean changed = !adapter.getCurrentList().isEmpty() && !adapter.getCurrentList().equals(shown);
        adapter.submitList(shown, changed ? () -> list.scrollToPosition(0) : null);
        if (changed) {
            appBar.setExpanded(true);
            addButton.extend();
        }

        sortButton.setText(sort.label);
        resultCount.setText(shown.size() == students.size()
                ? students.size() + " students"
                : shown.size() + " of " + students.size() + " students");

        for (int i = 0; i < yearBar.getChildCount(); i++) {
            View segment = yearBar.getChildAt(i);
            int inResult = Student.query(shown, "", (String) segment.getTag(), sort).size();
            segment.animate().alpha(inResult > 0 ? 1f : 0.2f).setDuration(200);
        }

        emptyView.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        if (shown.isEmpty()) {
            String where = filter.equals(Student.ALL) ? "" : " in “" + filter + "”";
            emptyText.setText(query.isEmpty() ? "No students" + where + "."
                    : "No students match “" + query + "”" + where + ".");
        }
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
}
