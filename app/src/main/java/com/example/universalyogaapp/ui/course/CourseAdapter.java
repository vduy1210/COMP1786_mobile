
// Adapter cho RecyclerView hiển thị danh sách khoá học
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

// Adapter cho RecyclerView hiển thị danh sách khoá học
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courseList = new ArrayList<>(); // Danh sách khoá học
    private OnItemClickListener listener; // Listener cho sự kiện click item

    // Interface cho sự kiện click vào item khoá học
    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    // Gán listener cho adapter
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Gán danh sách khoá học cho adapter
    public void setCourseList(List<Course> courses) {
        this.courseList = courses;
        notifyDataSetChanged(); // Cập nhật lại RecyclerView
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho từng item khoá học
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.textViewName.setText(course.getName()); // Hiển thị tên khoá học
        holder.textViewDescription.setText(course.getDescription()); // Hiển thị mô tả
        // Hiển thị viết tắt 3 chữ đầu cho mỗi ngày trong lịch học
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
            if (sb.length() > 2) sb.setLength(sb.length() - 2); // Xoá dấu phẩy cuối
            shortSchedule = sb.toString();
        }
        holder.textViewSchedule.setText(shortSchedule); // Hiển thị lịch học
        holder.textViewTime.setText(course.getTime()); // Hiển thị giờ học

        // Sự kiện click vào item khoá học
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size(); // Trả về số lượng khoá học
    }

    // ViewHolder cho từng item khoá học
    class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDescription, textViewSchedule, textViewTime;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewCourseName);
            textViewDescription = itemView.findViewById(R.id.textViewCourseDescription);
            textViewSchedule = itemView.findViewById(R.id.textViewCourseSchedule);
            textViewTime = itemView.findViewById(R.id.textViewCourseTime);
        }
    }
}