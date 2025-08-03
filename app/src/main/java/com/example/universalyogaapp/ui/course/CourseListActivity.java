// Activity to display course list, statistics, search, and data synchronization
package com.example.universalyogaapp.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.app.AlertDialog;

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

// Activity to display course list, statistics, search, and data synchronization
public class CourseListActivity extends AppCompatActivity {
    private RecyclerView recyclerView; // RecyclerView to display course list
    private CourseAdapter adapter; // Adapter for RecyclerView
    private List<Course> courseList; // Currently displayed course list
    private List<Course> fullCourseList; // Full course list for filtering
    private FirebaseManager firebaseManager; // Firebase synchronization manager
    private TextView textViewStatsCourses, textViewStatsStudents, textViewStatsRevenue; // Statistics TextViews
    private AppDatabase db; // Room database instance
    private Button buttonSync; // Data synchronization button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // Initialize database
        db = Room.databaseBuilder(
            getApplicationContext(),
            AppDatabase.class,
            "yoga-db"
        ).allowMainThreadQueries()
                         .addMigrations(AppDatabase.MIGRATION_5_6)
        .build();

        // Initialize views and adapter
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
        loadCourses(); // Load course data

        // Click event on course item to view details
        adapter.setOnItemClickListener(new CourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Course course) {
                Intent intent = new Intent(CourseListActivity.this, CourseDetailActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivity(intent);
            }
        });

        // Event for adding new course
        MaterialButton buttonAddCourse = findViewById(R.id.buttonAddCourse);
        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseListActivity.this, AddEditCourseActivity.class);
                startActivity(intent);
            }
        });

        // Event for course search
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
        // Event for data synchronization
        buttonSync = findViewById(R.id.buttonSync);
        buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prevent multiple consecutive syncs
                if (!buttonSync.isEnabled()) {
                    return;
                }
                // Display syncing status
                buttonSync.setEnabled(false);
                buttonSync.setText("Syncing...");
                // Perform complete synchronization
                performCompleteSync();
            }
        });

        // Event for viewing all class instances
        MaterialButton buttonAllClassInstances = findViewById(R.id.buttonAllClassInstances);
        buttonAllClassInstances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseListActivity.this, ClassInstanceListActivity.class);
                startActivity(intent);
            }
        });

        // Auto sync courses to Firebase when opening activity
        syncCoursesToFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }

    // Load course list from local database, update statistics
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
                entity.capacity,
                entity.price,
                entity.duration,
                entity.description,
                entity.note,
                entity.upcomingDate,
                entity.localId // pass localId from entity
            );
            courseList.add(course);
            fullCourseList.add(course);
            totalCourses++;
            totalStudents += entity.capacity;
            totalRevenueUSD += entity.price * entity.capacity;
        }
        // Display statistics
        textViewStatsCourses.setText(String.valueOf(totalCourses));
        textViewStatsStudents.setText(String.valueOf(totalStudents));
        textViewStatsRevenue.setText(formatCurrencyUSD(totalRevenueUSD) + " $");
        adapter.setCourseList(courseList);
    }

    // Filter courses by keyword (name, schedule, date, etc.)
    private void filterCourses(String keyword) {
        List<Course> filtered = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd", Locale.US),
            new SimpleDateFormat("dd/MM/yyyy", Locale.US)
        };
        String dayOfWeek = null;
        // Try parsing date to filter by day of week
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
                // If input is a day name
                String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "mon", "tue", "wed", "thu", "fri", "sat", "sun"};
                for (String wd : weekDays) {
                    if (lowerKeyword.equals(wd) && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(wd)) {
                        match = true;
                        break;
                    }
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

    // Format USD currency
    private String formatCurrencyUSD(double value) {
        return String.format("% ,.2f", value);
    }

    // Push unsynced courses to Firebase
    public void syncCoursesToFirebase() {
        List<CourseEntity> unsynced = db.courseDao().getUnsyncedCourses();
        FirebaseManager firebaseManager = new FirebaseManager();

        for (CourseEntity entity : unsynced) {
            Course course = new Course(
                null, entity.name, entity.schedule, entity.time,
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
        // Removed full reload from Firebase to Room
    }

    // Push unsynced class instances to Firebase
    public void syncClassInstancesToFirebase() {
        List<ClassInstanceEntity> unsynced = db.classInstanceDao().getUnsyncedInstances();
        FirebaseManager firebaseManager = new FirebaseManager();

        if (unsynced.isEmpty()) {
            Toast.makeText(CourseListActivity.this, "All class instances are already synced", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] syncedCount = {0};
        final int totalCount = unsynced.size();

        for (ClassInstanceEntity entity : unsynced) {
            // Skip if already has Firebase ID
            if (entity.firebaseId != null) {
                continue;
            }

            // Create ClassInstance with courseId as firebaseId
            ClassInstance instance = new ClassInstance(
                entity.firebaseId, 
                entity.courseId, // This is the firebaseId of the course
                entity.date, 
                entity.teacher, 
                entity.note, 
                entity.id
            );

            firebaseManager.addClassInstance(instance, (error, ref) -> {
                if (error == null) {
                    // Update local entity with Firebase ID and mark as synced
                    entity.isSynced = true;
                    entity.firebaseId = ref.getKey();
                    db.classInstanceDao().update(entity);
                    syncedCount[0]++;
                } else {
                    // If sync fails, keep for retry
                    Toast.makeText(CourseListActivity.this, "Failed to sync class instance", Toast.LENGTH_SHORT).show();
                }

                // Check if all processed
                if (syncedCount[0] + (totalCount - unsynced.size()) == totalCount) {
                    runOnUiThread(() -> {
                        Toast.makeText(CourseListActivity.this, 
                            "Synced " + syncedCount[0] + " of " + totalCount + " class instances", 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    // Get class instance data from Firebase to local
    public void pullClassInstancesFromFirebase() {
        FirebaseManager firebaseManager = new FirebaseManager();

        // Get all courses
        List<CourseEntity> courses = db.courseDao().getAllCourses();

        for (CourseEntity course : courses) {
            if (course.firebaseId != null) {
                firebaseManager.getClassInstancesByCourseId(course.firebaseId, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ClassInstance firebaseInstance = child.getValue(ClassInstance.class);
                            if (firebaseInstance != null) {
                                firebaseInstance.setId(child.getKey());

                                // Check if instance already exists locally (by firebaseId)
                                ClassInstanceEntity existingEntity = db.classInstanceDao().getInstanceByFirebaseId(child.getKey());

                                if (existingEntity == null) {
                                    // Check for duplicate dates to avoid duplicates
                                    List<ClassInstanceEntity> similarInstances = db.classInstanceDao().getInstancesForCourse(course.firebaseId);
                                    boolean isDuplicate = false;

                                    for (ClassInstanceEntity entity : similarInstances) {
                                        if (entity.date.equals(firebaseInstance.getDate())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }

                                    if (!isDuplicate) {
                                        // Create new local entity
                                        ClassInstanceEntity newEntity = new ClassInstanceEntity();
                                        newEntity.firebaseId = child.getKey();
                                        newEntity.courseId = course.firebaseId;
                                        newEntity.courseLocalId = course.localId;
                                        newEntity.date = firebaseInstance.getDate();
                                        newEntity.teacher = ""; // No teacher information yet
                                        newEntity.note = firebaseInstance.getNote();
                                        newEntity.isSynced = true;

                                        db.classInstanceDao().insert(newEntity);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CourseListActivity.this, "Failed to pull class instances", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    // Perform complete sync: remove duplicates, push to Firebase, pull to local
    private void performCompleteSync() {
        // Remove duplicates first
        cleanupDuplicateInstances();

        // Push local data to Firebase (only unsynced data)
        syncCoursesToFirebase();
        syncClassInstancesToFirebase();

        // Pull data from Firebase to local (delay to avoid conflicts)
        buttonSync.postDelayed(() -> {
            pullClassInstancesFromFirebase();
        }, 2000); // 2 second delay

        // Re-enable button after delay
        buttonSync.postDelayed(() -> {
            buttonSync.setEnabled(true);
            buttonSync.setText("Sync Data");
            Toast.makeText(CourseListActivity.this, "Sync completed", Toast.LENGTH_SHORT).show();
        }, 5000); // 5 second delay
    }

    // Remove duplicate class instances (keep the one with smallest ID)
    private void cleanupDuplicateInstances() {
        List<ClassInstanceDao.DuplicateInfo> duplicates = db.classInstanceDao().getDuplicateInfo();

        int cleanedCount = 0;
        for (ClassInstanceDao.DuplicateInfo duplicate : duplicates) {
            // Remove duplicates, keep the one with smallest ID
            db.classInstanceDao().deleteDuplicateInstances(duplicate.courseId, duplicate.date);
            cleanedCount++;
        }

        if (cleanedCount > 0) {
            Toast.makeText(this, "Cleaned up " + cleanedCount + " duplicate entries", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to call duplicate cleanup from UI
    public void manualCleanupDuplicates() {
        new AlertDialog.Builder(this)
                .setTitle("Clean Up Duplicates")
                .setMessage("This will remove duplicate class instances. Continue?")
                .setPositiveButton("Clean Up", (dialog, which) -> {
                    cleanupDuplicateInstances();
                    // Refresh course list
                    loadCourses();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
