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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universalyogaapp.model.ClassInstance;
import java.util.ArrayList;
import java.util.List;
import com.example.universalyogaapp.ui.course.ClassInstanceAdapter;
import com.example.universalyogaapp.dao.ClassInstanceDao;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView textViewName, textViewSchedule, textViewTime, textViewTeacher, textViewCapacity, textViewPrice, textViewDuration, textViewDescription, textViewNote;
    private Button buttonEdit, buttonDelete;
    private Course course;
    private FirebaseManager firebaseManager;
    private String courseId;
    private AppDatabase db;
    private RecyclerView recyclerViewClassInstances;
    private ClassInstanceAdapter classInstanceAdapter;
    private Button buttonAddClassInstance;
    private List<ClassInstance> classInstanceList = new ArrayList<>();
    private String courseSchedule; // Store course schedule to pass to add/edit screen

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
        recyclerViewClassInstances = findViewById(R.id.recyclerViewClassInstances);
        buttonAddClassInstance = findViewById(R.id.buttonAddClassInstance);

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
        classInstanceAdapter = new ClassInstanceAdapter(classInstanceList, new ClassInstanceAdapter.OnInstanceActionListener() {
            @Override
            public void onEdit(ClassInstance instance) {
                Intent intent = new Intent(CourseDetailActivity.this, AddEditClassInstanceActivity.class);
                intent.putExtra("course_id", courseId);
                intent.putExtra("course_schedule", courseSchedule);
                intent.putExtra("class_instance", instance);
                startActivity(intent);
            }
            @Override
            public void onDelete(ClassInstance instance) {
                confirmDeleteInstance(instance);
            }
        });
        recyclerViewClassInstances.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewClassInstances.setAdapter(classInstanceAdapter);
        buttonAddClassInstance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, AddEditClassInstanceActivity.class);
                intent.putExtra("course_id", courseId);
                intent.putExtra("course_schedule", courseSchedule);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (course != null) {
            loadClassInstances(course.getId());
        } else if (courseId != null) {
            loadClassInstances(courseId);
        }
    }

    private void loadCourse(String id) {
        firebaseManager.getCourseById(id, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer capacityObj = snapshot.child("capacity").getValue(Integer.class);
                int capacity = capacityObj != null ? capacityObj : 0;
                Double priceObj = snapshot.child("price").getValue(Double.class);
                double price = priceObj != null ? priceObj : 0.0;
                Integer durationObj = snapshot.child("duration").getValue(Integer.class);
                int duration = durationObj != null ? durationObj : 0;
                com.example.universalyogaapp.model.Course course = new com.example.universalyogaapp.model.Course(
                    snapshot.getKey(),
                    snapshot.child("name").getValue(String.class),
                    snapshot.child("schedule").getValue(String.class),
                    snapshot.child("time").getValue(String.class),
                    snapshot.child("teacher").getValue(String.class),
                    capacity,
                    price,
                    duration,
                    snapshot.child("description").getValue(String.class),
                    snapshot.child("note").getValue(String.class),
                    snapshot.child("upcomingDate").getValue(String.class),
                    0 // hoặc 0 nếu không có
                );
                if (course != null) {
                    course.setId(snapshot.getKey());
                    course.setLocalId(0); // When getting from Firebase, localId doesn't exist, set 0
                    showCourseInfo(course);
                    loadClassInstances(course.getId());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CourseDetailActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClassInstances(String courseId) {
        classInstanceList.clear();
        List<com.example.universalyogaapp.db.ClassInstanceEntity> entities = db.classInstanceDao().getInstancesForCourse(courseId);
        for (com.example.universalyogaapp.db.ClassInstanceEntity entity : entities) {
            com.example.universalyogaapp.model.ClassInstance instance = new com.example.universalyogaapp.model.ClassInstance(
                entity.firebaseId,
                entity.courseId,
                entity.date,
                entity.teacher,
                entity.note,
                entity.id // truyền localId từ entity
            );
            classInstanceList.add(instance);
        }
        classInstanceAdapter.setInstanceList(new ArrayList<>(classInstanceList));
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
                    // Store schedule to pass
        courseSchedule = course.getSchedule();
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
                    // Delete ClassInstances on Firebase first
        firebaseManager.deleteClassInstancesByCourseId(courseId, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                // After deleting instances, delete Course
                firebaseManager.deleteCourse(courseId, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            // Delete local in Room after successful cloud deletion
                            db.courseDao().deleteByFirebaseId(courseId);
                            // Delete local instances
                            db.classInstanceDao().getInstancesForCourse(courseId).forEach(entity -> {
                                db.classInstanceDao().delete(entity);
                            });
                            Toast.makeText(CourseDetailActivity.this, "Course and related class instances deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CourseDetailActivity.this, "Error deleting course", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void confirmDeleteInstance(ClassInstance instance) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class Session")
                .setMessage("Are you sure you want to delete this class session?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteClassInstance(instance);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClassInstance(ClassInstance instance) {
        firebaseManager.deleteClassInstance(instance.getId(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(CourseDetailActivity.this, "Class instance deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                                          Toast.makeText(CourseDetailActivity.this, "Error deleting class instance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 