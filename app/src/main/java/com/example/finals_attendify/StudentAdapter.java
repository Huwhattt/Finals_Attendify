package com.example.finals_attendify;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<String> items;

    public StudentAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        String subjectName = items.get(position);
        holder.tvSubjectName.setText(subjectName);

        holder.itemView.setOnClickListener(v -> {

            Dialog dialog = new Dialog(v.getContext());
            View view = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.attendclass, null);

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            dialog.setContentView(view);

            dialog.show();

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );


            Button btnSubmit = view.findViewById(R.id.btnSubmitAttend);
            Button btnCancel = view.findViewById(R.id.btnCancelAttend);

            btnSubmit.setOnClickListener(ok -> {
                dialog.dismiss();

                Intent intent = new Intent(v.getContext(), scanner_part.class);
                intent.putExtra("className", subjectName);
                v.getContext().startActivity(intent);
            });

            btnCancel.setOnClickListener(later -> {
                dialog.dismiss();
            });
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
        }
    }

    public void updateData(List<String> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }
}
