package com.example.universalyogaapp.ui.course;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.FirebaseManager;
import com.example.universalyogaapp.model.Course;
import com.example.universalyogaapp.utils.DateUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import android.app.TimePickerDialog;
import java.util.Calendar;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import androidx.room.Room;
import com.example.universalyogaapp.db.AppDatabase;
import com.example.universalyogaapp.db.CourseEntity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AddEditCourseActivity extends AppCompatActivity {
    private TextInputEditText editTextName, editTextTime, editTextTeacher, editTextCapacity, editTextPrice, editTextDuration, editTextDescription, editTextNote;
    private ChipGroup chipGroupSchedule;
    private Button buttonSave;
    private FirebaseManager firebaseManager;
    private Course editingCourse;
    private String courseId;
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_course);

        db = Room.databaseBuilder(
            getApplicationContext(),
            AppDatabase.class,
            "yoga-db"
        ).allowMainThreadQueries()
                         .fallbackToDestructiveMigration() // Add this line!
        .build();

        editTextName = findViewById(R.id.editTextName);
        chipGroupSchedule = findViewById(R.id.chipGroupSchedule);
        editTextTime = findViewById(R.id.editTextTime);
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        editTextTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimePicker();
                }
            }
        });
        editTextTeacher = findViewById(R.id.editTextTeacher);
        editTextCapacity = findViewById(R.id.editTextCapacity);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextNote = findViewById(R.id.editTextNote);
        buttonSave = findViewById(R.id.buttonSave);

        firebaseManager = new FirebaseManager();

        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            setTitle("Edit Course");
            loadCourse(courseId);
        } else {
            setTitle("Add New Course");
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
    }

    private void loadCourse(String id) {
        firebaseManager.getCourseById(id, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editingCourse = snapshot.getValue(Course.class);
                if (editingCourse != null) {
                    editingCourse.setId(snapshot.getKey());
                    fillCourseData(editingCourse);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddEditCourseActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillCourseData(Course course) {
        editTextName.setText(course.getName());

        // Find and check the corresponding chips
        String schedule = course.getSchedule();
        if (schedule != null && !schedule.isEmpty()) {
            List<String> selectedDays = Arrays.asList(schedule.split(","));
            for (int i = 0; i < chipGroupSchedule.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSchedule.getChildAt(i);
                if (selectedDays.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }
        
        editTextTime.setText(course.getTime());
        editTextTeacher.setText(course.getTeacher());
        editTextCapacity.setText(String.valueOf(course.getCapacity()));
        editTextPrice.setText(String.valueOf(course.getPrice()));
        editTextDuration.setText(String.valueOf(course.getDuration()));
        editTextDescription.setText(course.getDescription());
        editTextNote.setText(course.getNote());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showConfirmDialog() {
        String name = editTextName.getText().toString().trim();
        List<String> selectedChips = new java.util.ArrayList<>();
        for (int id : chipGroupSchedule.getCheckedChipIds()) {
            Chip chip = chipGroupSchedule.findViewById(id);
            selectedChips.add(chip.getText().toString());
        }
        String schedule = String.join(",", selectedChips);
        String upcomingDate = DateUtils.getNextUpcomingDate(schedule);
        String time = editTextTime.getText().toString().trim();
        String teacher = editTextTeacher.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String durationStr = editTextDuration.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(schedule) || TextUtils.isEmpty(teacher) ||
                TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("Name: ").append(name).append("\n");
        message.append("Schedule: ").append(schedule).append("\n");
        message.append("Time: ").append(time).append("\n");
        message.append("Teacher: ").append(teacher).append("\n");
        message.append("Capacity: ").append(capacityStr).append("\n");
        message.append("Price: ").append(priceStr).append("\n");
        message.append("Duration: ").append(durationStr).append("\n");
        message.append("Description: ").append(description).append("\n");
        message.append("Note: ").append(note).append("\n");
        new AlertDialog.Builder(this)
            .setTitle("Confirm Course Details")
            .setMessage(message.toString())
            .setPositiveButton("Confirm", (dialog, which) -> saveCourse())
            .setNegativeButton("Edit", null)
            .show();
    }

    private void saveCourse() {
        String name = editTextName.getText().toString().trim();
        List<String> selectedChips = new java.util.ArrayList<>();
        for (int id : chipGroupSchedule.getCheckedChipIds()) {
            Chip chip = chipGroupSchedule.findViewById(id);
            selectedChips.add(chip.getText().toString());
        }
        String schedule = String.join(",", selectedChips);
        String upcomingDate = DateUtils.getNextUpcomingDate(schedule);
        String time = editTextTime.getText().toString().trim();
        String teacher = editTextTeacher.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String durationStr = editTextDuration.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();
        int capacity = Integer.parseInt(capacityStr);
        double price = Double.parseDouble(priceStr);
        int duration = Integer.parseInt(durationStr);

        if (editingCourse != null) {
            // EDITING EXISTING COURSE
            Course course = new Course(
                editingCourse.getId(), name, schedule, time, teacher,
                capacity, price, duration, description, note, upcomingDate, editingCourse.getLocalId()
            );
            
            if (isNetworkAvailable()) {
                // ONLINE: Update Firebase first, then local
                DatabaseReference.CompletionListener listener = (error, ref) -> {
                    if (error == null) {
                        // Update local database
                        CourseEntity entity = new CourseEntity();
                        entity.localId = editingCourse.getLocalId();
                        entity.firebaseId = editingCourse.getId();
                        entity.name = name;
                        entity.schedule = schedule;
                        entity.time = time;
                        entity.teacher = teacher;
                        entity.capacity = capacity;
                        entity.price = price;
                        entity.duration = duration;
                        entity.description = description;
                        entity.note = note;
                        entity.upcomingDate = upcomingDate;
                        entity.isSynced = true;
                        
                        db.courseDao().update(entity);
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditCourseActivity.this, "Course updated and synced!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditCourseActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                };
                firebaseManager.updateCourse(course, listener);
            } else {
                // OFFLINE: Update local only
                CourseEntity entity = new CourseEntity();
                entity.localId = editingCourse.getLocalId();
                entity.firebaseId = editingCourse.getId();
                entity.name = name;
                entity.schedule = schedule;
                entity.time = time;
                entity.teacher = teacher;
                entity.capacity = capacity;
                entity.price = price;
                entity.duration = duration;
                entity.description = description;
                entity.note = note;
                entity.upcomingDate = upcomingDate;
                entity.isSynced = false;
                
                db.courseDao().update(entity);
                Toast.makeText(AddEditCourseActivity.this, "Course updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // CREATING NEW COURSE
            CourseEntity entity = new CourseEntity();
            entity.name = name;
            entity.schedule = schedule;
            entity.time = time;
            entity.teacher = teacher;
            entity.capacity = capacity;
            entity.price = price;
            entity.duration = duration;
            entity.description = description;
            entity.note = note;
            entity.upcomingDate = upcomingDate;
            
            if (isNetworkAvailable()) {
                // ONLINE: Lưu local với isSynced=true, đẩy lên Firebase
                entity.isSynced = true;
                long localId = db.courseDao().insert(entity);
                Course course = new Course(
                    entity.firebaseId, entity.name, entity.schedule, entity.time, entity.teacher,
                    entity.capacity, entity.price, entity.duration, entity.description, entity.note, entity.upcomingDate, entity.localId
                );
                DatabaseReference.CompletionListener listener = (error, ref) -> {
                    if (error == null) {
                        db.courseDao().markCourseAsSynced((int) localId, ref.getKey());
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditCourseActivity.this, "Course saved and synced!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditCourseActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                };
                if (entity.firebaseId == null || entity.firebaseId.isEmpty()) {
                    firebaseManager.addCourse(course, listener);
                } else {
                    course.setId(entity.firebaseId);
                    firebaseManager.updateCourse(course, listener);
                }
            } else {
                // OFFLINE: Lưu local với isSynced=false
                entity.isSynced = false;
                db.courseDao().insert(entity);
                Toast.makeText(AddEditCourseActivity.this, "Course saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute1);
            editTextTime.setText(time);
        }, hour, minute, true);
        timePickerDialog.show();
    }
} 