package com.example.universalyogaapp.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.model.Course;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courseList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCourseList(List<Course> courses) {
        this.courseList = courses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.textViewName.setText(course.getName());
        holder.textViewDescription.setText(course.getDescription());
        // Hiển thị viết tắt 3 chữ đầu cho mỗi ngày
        String shortSchedule = "";
        if (course.getSchedule() != null && !course.getSchedule().isEmpty()) {
            String[] days = course.getSchedule().split(",");
            StringBuilder sb = new StringBuilder();
            for (String day : days) {
                String trimmed = day.trim();
                if (trimmed.length() >= 3) {
                    sb.append(trimmed.substring(0, 3));
                } else {
                    sb.append(trimmed);
                }
                sb.append(", ");
            }
            if (sb.length() > 2) sb.setLength(sb.length() - 2); // Xóa dấu phẩy cuối
            shortSchedule = sb.toString();
        }
        holder.textViewSchedule.setText(shortSchedule);
        holder.textViewTime.setText(course.getTime());
        holder.textViewTeacher.setText(course.getTeacher());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDescription, textViewSchedule, textViewTime, textViewTeacher;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewCourseName);
            textViewDescription = itemView.findViewById(R.id.textViewCourseDescription);
            textViewSchedule = itemView.findViewById(R.id.textViewCourseSchedule);
            textViewTime = itemView.findViewById(R.id.textViewCourseTime);
            textViewTeacher = itemView.findViewById(R.id.textViewCourseTeacher);
        }
    }
} 