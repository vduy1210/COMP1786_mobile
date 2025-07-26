package com.example.universalyogaapp.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.model.ClassInstance;
import java.util.List;

public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.ViewHolder> {
    public interface OnInstanceActionListener {
        void onEdit(ClassInstance instance);
        void onDelete(ClassInstance instance);
    }

    private List<ClassInstance> instanceList;
    private final OnInstanceActionListener listener;

    public ClassInstanceAdapter(List<ClassInstance> instanceList, OnInstanceActionListener listener) {
        this.instanceList = instanceList;
        this.listener = listener;
    }

    public void setInstanceList(List<ClassInstance> list) {
        this.instanceList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_instance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassInstance instance = instanceList.get(position);
        holder.textViewDate.setText(instance.getDate());
        holder.textViewTeacher.setText("Teacher: " + (instance.getTeacher() != null ? instance.getTeacher() : ""));
        holder.textViewNote.setText("Note: " + (instance.getNote() != null ? instance.getNote() : ""));
        holder.buttonEdit.setOnClickListener(v -> listener.onEdit(instance));
        holder.buttonDelete.setOnClickListener(v -> listener.onDelete(instance));
    }

    @Override
    public int getItemCount() {
        return instanceList != null ? instanceList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewTeacher, textViewNote;
        Button buttonEdit, buttonDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTeacher = itemView.findViewById(R.id.textViewTeacher);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            buttonEdit = itemView.findViewById(R.id.buttonEditInstance);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteInstance);
        }
    }
} 