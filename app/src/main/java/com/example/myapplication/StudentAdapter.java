package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.BiConsumer;

/** Student rows. ListAdapter animates rows that enter, leave or move when the search, filter or sort changes. */
class StudentAdapter extends ListAdapter<Student, StudentAdapter.Row> {
    private final BiConsumer<Student, View> onOpen;

    StudentAdapter(BiConsumer<Student, View> onOpen) {
        super(new DiffUtil.ItemCallback<Student>() {
            @Override
            public boolean areItemsTheSame(@NonNull Student a, @NonNull Student b) {
                return a.number.equals(b.number);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Student a, @NonNull Student b) {
                return a.row.equals(b.row);
            }
        });
        this.onOpen = onOpen;
    }

    @NonNull
    @Override
    public Row onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Row(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Row row, int position) {
        Student s = getItem(position);
        String specialization = s.specialization.isEmpty() ? Student.NO_SPECIALIZATION : s.specialization;
        row.initials.setText(s.initials());
        row.name.setText(s.fullName());
        row.number.setText(s.number);
        row.year.setText(s.year);
        row.specialization.setText(specialization);
        row.itemView.setContentDescription(s.fullName() + ", " + s.number + ", " + s.year + ", " + specialization);
        row.itemView.setTransitionName(StudentDetailActivity.transitionName(s));
        row.itemView.setOnClickListener(v -> onOpen.accept(s, v));
    }

    static class Row extends RecyclerView.ViewHolder {
        final TextView initials, name, number, year, specialization;

        Row(View v) {
            super(v);
            initials = v.findViewById(R.id.text_initials);
            name = v.findViewById(R.id.text_name);
            number = v.findViewById(R.id.text_number);
            year = v.findViewById(R.id.text_year);
            specialization = v.findViewById(R.id.text_specialization);
        }
    }
}
