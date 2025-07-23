package com.example.universalyogaapp.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.FirebaseManager;
import com.example.universalyogaapp.model.Course;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import androidx.room.Room;
import com.example.universalyogaapp.db.AppDatabase;
import com.example.universalyogaapp.db.CourseEntity;

public class CourseListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private List<Course> fullCourseList; // Lưu toàn bộ để filter
    private FirebaseManager firebaseManager;
    private TextView textViewStatsCourses, textViewStatsStudents, textViewStatsRevenue;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        db = Room.databaseBuilder(
            getApplicationContext(),
            AppDatabase.class,
            "yoga-db"
        ).allowMainThreadQueries()
         .fallbackToDestructiveMigration() // Thêm dòng này!
        .build();

        recyclerView = findViewById(R.id.recyclerViewCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();
        fullCourseList = new ArrayList<>();
        adapter = new CourseAdapter();
        recyclerView.setAdapter(adapter);
        firebaseManager = new FirebaseManager();
        textViewStatsCourses = findViewById(R.id.textViewStatsCourses);
        textViewStatsStudents = findViewById(R.id.textViewStatsStudents);
        textViewStatsRevenue = findViewById(R.id.textViewStatsRevenue);
        loadCourses();

        adapter.setOnItemClickListener(new CourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Course course) {
                Intent intent = new Intent(CourseListActivity.this, CourseDetailActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivity(intent);
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAddCourse);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseListActivity.this, AddEditCourseActivity.class);
                startActivity(intent);
            }
        });

        TextInputEditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        syncCoursesToFirebase();
    }

    private void loadCourses() {
        firebaseManager.getCourses(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                fullCourseList.clear();
                int totalCourses = 0;
                int totalStudents = 0;
                double totalRevenueUSD = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Course course = data.getValue(Course.class);
                    if (course != null) {
                        courseList.add(course);
                        fullCourseList.add(course);
                        totalCourses++;
                        totalStudents += course.getCapacity();
                        totalRevenueUSD += course.getPrice() * course.getCapacity();
                    }
                }
                textViewStatsCourses.setText(String.valueOf(totalCourses));
                textViewStatsStudents.setText(String.valueOf(totalStudents));
                textViewStatsRevenue.setText(formatCurrencyUSD(totalRevenueUSD) + " $");
                adapter.setCourseList(courseList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CourseListActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterCourses(String keyword) {
        List<Course> filtered = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Course course : fullCourseList) {
            boolean matchName = course.getName() != null && course.getName().toLowerCase().contains(lowerKeyword);
            boolean matchSchedule = course.getSchedule() != null && course.getSchedule().toLowerCase().contains(lowerKeyword);
            if (matchName || matchSchedule) {
                filtered.add(course);
            }
        }
        adapter.setCourseList(filtered);
    }

    private String formatCurrencyUSD(double value) {
        return String.format("%,.2f", value);
    }

    public void syncCoursesToFirebase() {
        List<CourseEntity> unsynced = db.courseDao().getUnsyncedCourses();
        FirebaseManager firebaseManager = new FirebaseManager();

        for (CourseEntity entity : unsynced) {
            Course course = new Course(
                null, entity.name, entity.schedule, entity.time, entity.teacher,
                entity.capacity, entity.price, entity.duration, entity.description, entity.note, entity.upcomingDate
            );
            firebaseManager.addCourse(course, (error, ref) -> {
                if (error == null) {
                    // Đánh dấu đã sync
                    entity.isSynced = true;
                    entity.firebaseId = ref.getKey();
                    db.courseDao().update(entity);
                }
            });
        }
    }
} 