package com.example.universalyogaapp.ui.course;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.FirebaseManager;
import com.example.universalyogaapp.model.Course;
import com.example.universalyogaapp.utils.DateUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import androidx.room.Room;
import com.example.universalyogaapp.db.AppDatabase;
import com.example.universalyogaapp.dao.CourseDao;
import com.example.universalyogaapp.db.CourseEntity;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView textViewName, textViewSchedule, textViewTime, textViewTeacher, textViewCapacity, textViewPrice, textViewDuration, textViewDescription, textViewNote;
    private Button buttonEdit, buttonDelete;
    private Course course;
    private FirebaseManager firebaseManager;
    private String courseId;
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "yoga-db"
            ).allowMainThreadQueries()
            .build();

        textViewName = findViewById(R.id.textViewName);
        textViewSchedule = findViewById(R.id.textViewSchedule);
        textViewTime = findViewById(R.id.textViewTime);
        textViewTeacher = findViewById(R.id.textViewTeacher);
        textViewCapacity = findViewById(R.id.textViewCapacity);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewDuration = findViewById(R.id.textViewDuration);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewNote = findViewById(R.id.textViewNote);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);

        firebaseManager = new FirebaseManager();

        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            loadCourse(courseId);
        }

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, AddEditCourseActivity.class);
                intent.putExtra("course_id", courseId);
                startActivity(intent);
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    private void loadCourse(String id) {
        firebaseManager.getCourseById(id, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                course = snapshot.getValue(Course.class);
                if (course != null) {
                    course.setId(snapshot.getKey());
                    showCourseInfo(course);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CourseDetailActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCourseInfo(Course course) {
        textViewName.setText(course.getName());
        textViewDescription.setText(course.getDescription());
        textViewSchedule.setText(DateUtils.getNextUpcomingDate(course.getSchedule()));
        textViewTime.setText(course.getTime() != null ? course.getTime() : "Not set");
        textViewTeacher.setText(course.getTeacher());
        textViewCapacity.setText(String.format(Locale.getDefault(), "%d Students", course.getCapacity()));
        textViewPrice.setText(String.format(Locale.US, "$%.2f", course.getPrice()));
        textViewDuration.setText(String.format(Locale.getDefault(), "%d min", course.getDuration()));
        
        if (course.getNote() != null && !course.getNote().isEmpty()) {
            textViewNote.setText(course.getNote());
            textViewNote.setVisibility(View.VISIBLE);
        } else {
            textViewNote.setVisibility(View.GONE);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCourse();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCourse() {
        if (courseId == null) return;
        firebaseManager.deleteCourse(courseId, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    // Xóa local trong Room sau khi xóa cloud thành công
                    db.courseDao().deleteByFirebaseId(courseId);

                    Toast.makeText(CourseDetailActivity.this, "Đã xóa lớp học", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CourseDetailActivity.this, "Lỗi khi xóa lớp học", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 