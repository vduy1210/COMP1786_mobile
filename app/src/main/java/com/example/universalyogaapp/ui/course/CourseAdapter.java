
// Adapter for RecyclerView to display course list
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

// Adapter for RecyclerView to display course list
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courseList = new ArrayList<>(); // Course list
    private OnItemClickListener listener; // Listener for item click events

    // Interface for course item click events
    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    // Set listener for adapter
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Set course list for adapter
    public void setCourseList(List<Course> courses) {
        this.courseList = courses;
        notifyDataSetChanged(); // Update RecyclerView
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each course item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.textViewName.setText(course.getName()); // Display course name
        holder.textViewDescription.setText(course.getDescription()); // Display description
        // Display abbreviated first 3 letters for each day in schedule
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
            if (sb.length() > 2) sb.setLength(sb.length() - 2); // Remove trailing comma
            shortSchedule = sb.toString();
        }
        holder.textViewSchedule.setText(shortSchedule); // Display schedule
        holder.textViewTime.setText(course.getTime()); // Display class time

        // Click event for course item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size(); // Return number of courses
    }

    // ViewHolder for each course item
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