package com.example.finals_attendify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EmployeeClassAdapter extends RecyclerView.Adapter<EmployeeClassAdapter.ViewHolder> {

    private List<EmployeeClass> items;

    public EmployeeClassAdapter(List<EmployeeClass> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.employee_class_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeClass item = items.get(position);
        holder.tvSubjectName.setText(item.subjectName);
        holder.tvSection.setText(item.section);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvSection;
        public ViewHolder(View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSection = itemView.findViewById(R.id.tvSection);
        }
    }

    public void updateData(List<EmployeeClass> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
}
