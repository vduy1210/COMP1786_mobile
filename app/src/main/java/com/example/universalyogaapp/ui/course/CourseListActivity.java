package com.example.universalyogaapp.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.FirebaseManager;
import com.example.universalyogaapp.model.Course;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import androidx.room.Room;
import com.example.universalyogaapp.db.AppDatabase;
import com.example.universalyogaapp.db.CourseEntity;
import com.example.universalyogaapp.db.ClassInstanceEntity;
import com.example.universalyogaapp.dao.ClassInstanceDao;
import com.example.universalyogaapp.model.ClassInstance;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CourseListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private List<Course> fullCourseList; // Store all for filtering
    private FirebaseManager firebaseManager;
    private TextView textViewStatsCourses, textViewStatsStudents, textViewStatsRevenue;
    private AppDatabase db;
    private Button buttonSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        db = Room.databaseBuilder(
            getApplicationContext(),
            AppDatabase.class,
            "yoga-db"
        ).allowMainThreadQueries()
                         .fallbackToDestructiveMigration() // Add this line!
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

        MaterialButton buttonAddCourse = findViewById(R.id.buttonAddCourse);
        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
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
        buttonSync = findViewById(R.id.buttonSync);
        buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncCoursesToFirebase();
                syncClassInstancesToFirebase();
                Toast.makeText(CourseListActivity.this, "Syncing data...", Toast.LENGTH_SHORT).show();
            }
        });
        syncCoursesToFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }

    private void loadCourses() {
        courseList.clear();
        fullCourseList.clear();
        List<CourseEntity> entities = db.courseDao().getAllCourses();
        int totalCourses = 0;
        int totalStudents = 0;
        double totalRevenueUSD = 0;
        for (CourseEntity entity : entities) {
            Course course = new Course(
                entity.firebaseId,
                entity.name,
                entity.schedule,
                entity.time,
                entity.teacher,
                entity.capacity,
                entity.price,
                entity.duration,
                entity.description,
                entity.note,
                entity.upcomingDate,
                entity.localId // truyền localId từ entity
            );
            courseList.add(course);
            fullCourseList.add(course);
            totalCourses++;
            totalStudents += entity.capacity;
            totalRevenueUSD += entity.price * entity.capacity;
        }
        textViewStatsCourses.setText(String.valueOf(totalCourses));
        textViewStatsStudents.setText(String.valueOf(totalStudents));
        textViewStatsRevenue.setText(formatCurrencyUSD(totalRevenueUSD) + " $");
        adapter.setCourseList(courseList);
    }

    private void filterCourses(String keyword) {
        List<Course> filtered = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd", Locale.US),
            new SimpleDateFormat("dd/MM/yyyy", Locale.US)
        };
        String dayOfWeek = null;
        // Thử parse ngày
        for (SimpleDateFormat sdf : dateFormats) {
            try {
                Date date = sdf.parse(keyword);
                if (date != null) {
                    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(date);
                    dayOfWeek = days[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1];
                }
            } catch (ParseException ignored) {}
        }
        for (Course course : fullCourseList) {
            boolean match = false;
                            // If input is a date, filter by day of week
            if (dayOfWeek != null) {
                match = course.getSchedule() != null && course.getSchedule().toLowerCase().contains(dayOfWeek.toLowerCase());
            } else {
                // If input is day name
                String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "mon", "tue", "wed", "thu", "fri", "sat", "sun"};
                for (String wd : weekDays) {
                    if (lowerKeyword.equals(wd) && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(wd)) {
                        match = true;
                        break;
                    }
                }
                // If input is teacher name
                if (!match && course.getTeacher() != null && course.getTeacher().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                // If input is course name or schedule
                if (!match && course.getName() != null && course.getName().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                if (!match && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
            }
            if (match) {
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
                entity.capacity, entity.price, entity.duration, entity.description, entity.note, entity.upcomingDate, entity.localId
            );
            firebaseManager.addCourse(course, (error, ref) -> {
                if (error == null) {
                    entity.isSynced = true;
                    entity.firebaseId = ref.getKey();
                    db.courseDao().update(entity);
                }
            });
        }
        // Removed section to reload all data from Firebase to Room
    }

    public void syncClassInstancesToFirebase() {
        List<ClassInstanceEntity> unsynced = db.classInstanceDao().getUnsyncedInstances();
        FirebaseManager firebaseManager = new FirebaseManager();
        for (ClassInstanceEntity entity : unsynced) {
            // Note: entity.courseId is localId, need to map to firebaseId if want to link correctly on cloud
            ClassInstance instance = new ClassInstance(
                null, entity.firebaseId != null ? entity.firebaseId : String.valueOf(entity.courseId),
                entity.date, entity.teacher, entity.note, entity.id
            );
            firebaseManager.addClassInstance(instance, (error, ref) -> {
                if (error == null) {
                    entity.isSynced = true;
                    entity.firebaseId = ref.getKey();
                    db.classInstanceDao().update(entity);
                }
            });
        }
    }
} 