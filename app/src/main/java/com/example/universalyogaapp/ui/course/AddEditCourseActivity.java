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
         .fallbackToDestructiveMigration() // Thêm dòng này!
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
                saveCourse();
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

    private void saveCourse() {
        String name = editTextName.getText().toString().trim();

        // Get selected days from ChipGroup
        List<String> selectedChips = new java.util.ArrayList<>();
        for (int id : chipGroupSchedule.getCheckedChipIds()) {
            Chip chip = chipGroupSchedule.findViewById(id);
            selectedChips.add(chip.getText().toString());
        }
        String schedule = String.join(",", selectedChips);

        // Calculate upcoming date
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

        int capacity = Integer.parseInt(capacityStr);
        double price = Double.parseDouble(priceStr);
        int duration = Integer.parseInt(durationStr);

        // Sau khi lấy dữ liệu từ form:
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
        entity.isSynced = false; // Đánh dấu là chưa sync

        // Lưu vào Room
        long localId = db.courseDao().insert(entity);

        if (courseId == null) {
            // Add new
            Course course = new Course(null, name, schedule, time, teacher, capacity, price, duration, description, note, upcomingDate);
            firebaseManager.addCourse(course, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        // Lấy firebaseId vừa tạo
                        String firebaseId = ref.getKey();
                        // Cập nhật lại bản ghi local với firebaseId và trạng thái đã sync
                        entity.firebaseId = firebaseId;
                        entity.isSynced = true;
                        entity.localId = (int)localId; // Gán lại localId để Room biết update đúng bản ghi
                        db.courseDao().update(entity); // Update lại bản ghi local

                        Toast.makeText(AddEditCourseActivity.this, "Course added", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditCourseActivity.this, "Error adding course", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Edit
            if (editingCourse == null) return;
            editingCourse.setName(name);
            editingCourse.setSchedule(schedule);
            editingCourse.setUpcomingDate(upcomingDate);
            editingCourse.setTime(time);
            editingCourse.setTeacher(teacher);
            editingCourse.setCapacity(capacity);
            editingCourse.setPrice(price);
            editingCourse.setDuration(duration);
            editingCourse.setDescription(description);
            editingCourse.setNote(note);
            firebaseManager.updateCourse(editingCourse, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        Toast.makeText(AddEditCourseActivity.this, "Course updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditCourseActivity.this, "Error updating course", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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