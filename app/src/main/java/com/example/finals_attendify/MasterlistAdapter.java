package com.example.finals_attendify;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;


import java.util.List;


public class MasterlistAdapter extends RecyclerView.Adapter<MasterlistAdapter.VH> {


    private final List<StudentStatus> list;


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


    static class VH extends RecyclerView.ViewHolder {
        TextView studentNumber, scannedAt, status;


        VH(View v) {
            super(v);
            studentNumber = v.findViewById(R.id.txtName); // reusing txtName for student number
            scannedAt = v.findViewById(R.id.txtDate);
            status = v.findViewById(R.id.txtStatus);
        }
    }
}
