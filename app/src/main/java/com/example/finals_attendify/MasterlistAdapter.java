package com.example.finals_attendify;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MasterlistAdapter extends RecyclerView.Adapter<MasterlistAdapter.VH> {

    private final List<StudentStatus> list;
    // Track the currently selected position
    private int selectedPosition = RecyclerView.NO_POSITION;

    public MasterlistAdapter(List<StudentStatus> list) {
        this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_masterlist, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        StudentStatus s = list.get(position);
        holder.studentNumber.setText(s.studentNumber);
        holder.scannedAt.setText(s.scannedAt);
        holder.status.setText(s.status);

        // Visual feedback for selection
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Highlight selected item
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Reset other items
        }

        // Handle Click Event to select an item
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Notify changes to update the background colors
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });

        // Color code for status
        switch (s.status) {
            case "PRESENT":
                holder.status.setTextColor(Color.GREEN);
                break;
            case "LATE":
                holder.status.setTextColor(Color.parseColor("#FFA500"));
                break;
            default:
                holder.status.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // METHOD TO GET THE SELECTED STUDENT
    public StudentStatus getSelectedStudent() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < list.size()) {
            return list.get(selectedPosition);
        }
        return null;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView studentNumber, scannedAt, status;

        VH(View v) {
            super(v);
            studentNumber = v.findViewById(R.id.txtName);
            scannedAt = v.findViewById(R.id.txtDate);
            status = v.findViewById(R.id.txtStatus);
        }
    }
}