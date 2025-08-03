// Activity to display course details, class instance list, allowing edit/delete course and class instances
package com.example.universalyogaapp.ui.course;

// Import necessary libraries for this Activity
import android.content.DialogInterface; // Interface for dialog interactions
import android.content.Intent; // Intent for Activity navigation
import android.os.Bundle; // Bundle for passing data
import android.view.View; // Basic view
import android.widget.Button; // Button widget
import android.widget.TextView; // Text display widget
import android.widget.Toast; // Display short messages

import androidx.annotation.NonNull; // NonNull annotation
import androidx.annotation.Nullable; // Nullable annotation
import androidx.appcompat.app.AlertDialog; // Alert dialog
import androidx.appcompat.app.AppCompatActivity; // Base AppCompat activity

import com.example.universalyogaapp.R; // Layout resources
import com.example.universalyogaapp.firebase.FirebaseManager; // Firebase sync manager
import com.example.universalyogaapp.model.Course; // Course model
import com.example.universalyogaapp.utils.DateUtils; // Date utility functions
import com.google.firebase.database.DataSnapshot; // Firebase data snapshot
import com.google.firebase.database.DatabaseError; // Firebase database error
import com.google.firebase.database.DatabaseReference; // Firebase database reference
import com.google.firebase.database.ValueEventListener; // Value change event listener

import java.util.Locale; // Localization
import androidx.room.Room; // Room database builder
import com.example.universalyogaapp.db.AppDatabase; // Application database
import com.example.universalyogaapp.dao.CourseDao; // DAO for courses
import com.example.universalyogaapp.db.CourseEntity; // Course entity
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for lists
import com.example.universalyogaapp.model.ClassInstance; // Class instance model
import java.util.ArrayList; // ArrayList collection
import java.util.List; // List interface
import com.example.universalyogaapp.ui.course.ClassInstanceAdapter; // Adapter for class instances
import com.example.universalyogaapp.dao.ClassInstanceDao; // DAO for class instances
import com.example.universalyogaapp.db.ClassInstanceEntity; // Class instance entity
import java.util.Map; // Map interface
import java.util.HashMap; // HashMap implementation
import android.net.ConnectivityManager; // Network connectivity manager
import android.net.NetworkInfo; // Network information
import android.net.NetworkCapabilities; // Network capabilities
import android.os.Build; // Android build information

// Activity to display course details, class instance list, allowing edit/delete course and class instances
public class CourseDetailActivity extends AppCompatActivity {
    // Declare UI component variables for course information
    private TextView textViewName, textViewSchedule, textViewTime, textViewCapacity, textViewPrice, textViewDuration, textViewDescription, textViewNote; // TextViews to display course information
    private Button buttonEdit, buttonDelete; // Buttons for edit/delete course
    
    // Declare data and processing variables
    private Course course; // Course object being viewed
    private FirebaseManager firebaseManager; // Firebase sync manager
    private String courseId; // Course ID on Firebase
    private AppDatabase db; // Room database instance
    
    // Declare variables for class instance list
    private RecyclerView recyclerViewClassInstances; // RecyclerView to display class instance list
    private ClassInstanceAdapter classInstanceAdapter; // Adapter for class instance list
    private Button buttonAddClassInstance; // Button to add class instance
    private List<ClassInstance> classInstanceList = new ArrayList<>(); // List of class instances
    private String courseSchedule; // Store course schedule to pass to add/edit class instance screen

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call parent method
        setContentView(R.layout.activity_course_detail); // Set layout for Activity

        // Initialize Room database with migration
        db = Room.databaseBuilder(
                getApplicationContext(), // Application context
                AppDatabase.class, // Database class
                "yoga-db" // Database name
            ).allowMainThreadQueries() // Allow main thread queries
            .addMigrations(AppDatabase.MIGRATION_5_6) // Add migration from version 5 to 6
            .build(); // Build database

        // Initialize views from layout
        textViewName = findViewById(R.id.textViewName); // Find and assign course name TextView
        textViewSchedule = findViewById(R.id.textViewSchedule); // Find and assign schedule TextView
        textViewTime = findViewById(R.id.textViewTime); // Find and assign time TextView
        textViewCapacity = findViewById(R.id.textViewCapacity); // Find and assign capacity TextView
        textViewPrice = findViewById(R.id.textViewPrice); // Find and assign price TextView
        textViewDuration = findViewById(R.id.textViewDuration); // Find and assign duration TextView
        textViewDescription = findViewById(R.id.textViewDescription); // Find and assign description TextView
        textViewNote = findViewById(R.id.textViewNote); // Find and assign note TextView
        buttonEdit = findViewById(R.id.buttonEdit); // Find and assign edit button
        buttonDelete = findViewById(R.id.buttonDelete); // Find and assign delete button
        recyclerViewClassInstances = findViewById(R.id.recyclerViewClassInstances); // Find and assign class instances RecyclerView
        buttonAddClassInstance = findViewById(R.id.buttonAddClassInstance); // Find and assign add class instance button

        firebaseManager = new FirebaseManager(); // Initialize Firebase manager

        // Initialize adapter for class instance list
        classInstanceAdapter = new ClassInstanceAdapter(classInstanceList, new ClassInstanceAdapter.OnInstanceActionListener() { // Create adapter with listener
            @Override
            public void onEdit(ClassInstance instance) { // When edit instance is pressed
                // Open edit instance screen
                Intent intent = new Intent(CourseDetailActivity.this, AddEditClassInstanceActivity.class); // Create Intent
                intent.putExtra("course_id", courseId); // Pass course ID
                intent.putExtra("course_schedule", courseSchedule); // Pass course schedule
                intent.putExtra("class_instance", instance); // Pass instance to edit
                startActivity(intent); // Start Activity
            }
            @Override
            public void onDelete(ClassInstance instance) { // When delete instance is pressed
                confirmDeleteInstance(instance); // Show delete instance confirmation dialog
            }
        });
        recyclerViewClassInstances.setLayoutManager(new LinearLayoutManager(this)); // Set linear layout manager
        recyclerViewClassInstances.setAdapter(classInstanceAdapter); // Set adapter for RecyclerView

        // Get courseId from Intent to load data
        courseId = getIntent().getStringExtra("course_id"); // Read courseId from extra
        if (courseId != null) { // If courseId exists
            loadCourse(courseId); // Load course information
        }

        // Edit course event
        buttonEdit.setOnClickListener(new View.OnClickListener() { // Listener for edit button
            @Override
            public void onClick(View v) { // When edit button is pressed
                // Open edit course screen
                Intent intent = new Intent(CourseDetailActivity.this, AddEditCourseActivity.class); // Create Intent
                intent.putExtra("course_id", courseId); // Pass course ID
                startActivity(intent); // Start Activity
            }
        });

        // Delete course event
        buttonDelete.setOnClickListener(new View.OnClickListener() { // Listener for delete button
            @Override
            public void onClick(View v) { // When delete button is pressed
                confirmDelete(); // Show delete confirmation dialog
            }
        });

        // Add class instance event
        buttonAddClassInstance.setOnClickListener(new View.OnClickListener() { // Listener for add instance button
            @Override
            public void onClick(View v) { // When add button is pressed
                // Open add new instance screen
                Intent intent = new Intent(CourseDetailActivity.this, AddEditClassInstanceActivity.class); // Create Intent
                intent.putExtra("course_id", courseId); // Pass course ID
                intent.putExtra("course_schedule", courseSchedule); // Pass course schedule
                startActivity(intent); // Start Activity
            }
        });
    }

    @Override
    protected void onResume() { // Method called when Activity returns to foreground
        super.onResume(); // Call parent method
        // When returning to screen, reload class instance list
        if (classInstanceAdapter != null) { // If adapter is initialized
            if (course != null) { // If course object exists
                loadClassInstances(course.getId()); // Reload class instances by course ID
            } else if (courseId != null) { // If courseId exists
                loadClassInstances(courseId); // Reload class instances by courseId
            }
        }
    }

    // Load course data from local database, if not available then get from Firebase
    private void loadCourse(String id) { // Method to load course information
        // Try to get from local database first
        CourseEntity localCourse = db.courseDao().getCourseByFirebaseId(id); // Query course from Room database

        if (localCourse != null) { // If local data exists
            // If local exists, display information
            Course course = new Course( // Create Course object from entity
                localCourse.firebaseId, // Firebase ID
                localCourse.name, // Course name
                localCourse.schedule, // Schedule
                localCourse.time, // Time
                localCourse.capacity, // Capacity
                localCourse.price, // Price
                localCourse.duration, // Duration
                localCourse.description, // Description
                localCourse.note, // Note
                localCourse.upcomingDate, // Upcoming date
                localCourse.localId // Local ID
            );
            course.setId(localCourse.firebaseId); // Set Firebase ID
            course.setLocalId(localCourse.localId); // Set local ID
            showCourseInfo(course); // Display course information
            loadClassInstances(course.getId()); // Load class instances
        } else {
            // If no local data, get from Firebase
            firebaseManager.getCourseById(id, new ValueEventListener() { // Listen for data from Firebase
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { // When data changes
                    Integer capacityObj = snapshot.child("capacity").getValue(Integer.class); // Get capacity value
                    int capacity = capacityObj != null ? capacityObj : 0; // Handle null safety
                    Double priceObj = snapshot.child("price").getValue(Double.class); // Get price value
                    double price = priceObj != null ? priceObj : 0.0; // Handle null safety for price
                    Integer durationObj = snapshot.child("duration").getValue(Integer.class); // Get duration value
                    int duration = durationObj != null ? durationObj : 0; // Handle null safety for duration
                    com.example.universalyogaapp.model.Course course = new com.example.universalyogaapp.model.Course( // Create Course object from Firebase
                        snapshot.getKey(), // ID from snapshot key
                        snapshot.child("name").getValue(String.class), // Course name
                        snapshot.child("schedule").getValue(String.class), // Schedule
                        snapshot.child("time").getValue(String.class), // Time
                        capacity, // Capacity
                        price, // Price
                        duration, // Duration
                        snapshot.child("description").getValue(String.class), // Description
                        snapshot.child("note").getValue(String.class), // Note
                        snapshot.child("upcomingDate").getValue(String.class), // Upcoming date
                        0 // When getting from Firebase, localId doesn't exist, assign 0
                    );
                    if (course != null) { // If course is not null
                        course.setId(snapshot.getKey()); // Set ID from key
                        course.setLocalId(0); // When getting from Firebase, localId doesn't exist, assign 0
                        showCourseInfo(course); // Display course information
                        loadClassInstances(course.getId()); // Load class instances
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { // When Firebase connection error occurs
                    Toast.makeText(CourseDetailActivity.this, "Error loading data", Toast.LENGTH_SHORT).show(); // Show error message
                }
            });
        }
    }

    // Load class instances of course from local, if network available then merge additional from Firebase (display only, don't save locally)
    private void loadClassInstances(String courseId) { // Method to load class instances
        if (classInstanceAdapter == null) { // If adapter not initialized
            // Adapter not initialized, skip
            return; // Exit method
        }

        classInstanceList.clear(); // Clear old list

        // Get from local database first
        List<com.example.universalyogaapp.db.ClassInstanceEntity> localEntities = db.classInstanceDao().getInstancesForCourse(courseId); // Query class instances from Room

        // Convert local entity to ClassInstance
        for (ClassInstanceEntity entity : localEntities) { // Iterate through each entity
            ClassInstance instance = new ClassInstance( // Create ClassInstance object
                entity.firebaseId, // Firebase ID
                entity.courseId, // Course ID
                entity.date, // Class date
                entity.teacher, // Teacher
                entity.note, // Comment
                entity.id // Local ID
            );
            classInstanceList.add(instance); // Add to list
        }

        // Display local data first
        classInstanceAdapter.setInstanceList(new ArrayList<>(classInstanceList)); // Update adapter with local data

        // If network available, get additional from Firebase for display (don't save locally)
        if (isNetworkAvailable()) { // Check network connection
            firebaseManager.getClassInstancesByCourseId(courseId, new ValueEventListener() { // Listen for data from Firebase
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { // When data changes
                    // Check if adapter still exists
                    if (classInstanceAdapter == null) { // If adapter is null
                        return; // Exit method
                    }

                    // Create map of existing instances by firebaseId
                    Map<String, ClassInstance> existingInstanceMap = new HashMap<>(); // Map to store existing instances
                    for (ClassInstance instance : classInstanceList) { // Iterate through current list
                        if (instance.getId() != null) { // If has ID
                            existingInstanceMap.put(instance.getId(), instance); // Add to map
                        }
                    }

                    // Process instances from Firebase for display only
                    for (DataSnapshot child : snapshot.getChildren()) { // Iterate through each child snapshot
                        ClassInstance firebaseInstance = child.getValue(ClassInstance.class); // Convert to ClassInstance object
                        if (firebaseInstance != null) { // If instance is not null
                            firebaseInstance.setId(child.getKey()); // Set ID from key

                            // If not yet in display list
                            if (!existingInstanceMap.containsKey(child.getKey())) { // Check for duplicate ID
                                // Check for duplicate date in display list
                                boolean isDuplicate = false; // Duplicate check flag
                                for (ClassInstance existingInstance : classInstanceList) { // Iterate through existing list
                                    if (existingInstance.getDate().equals(firebaseInstance.getDate())) { // Compare dates
                                        isDuplicate = true; // Mark as duplicate
                                        break; // Break loop
                                    }
                                }

                                if (!isDuplicate) { // If not duplicate
                                    // New instance from Firebase, only add to display list
                                    classInstanceList.add(firebaseInstance); // Add to list
                                }
                            }
                        }
                    }

                    // Display merged data
                    if (classInstanceAdapter != null) { // If adapter still exists
                        classInstanceAdapter.setInstanceList(new ArrayList<>(classInstanceList)); // Update adapter with merged data
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { // When Firebase error occurs
                    // If Firebase error, still display local data
                    Toast.makeText(CourseDetailActivity.this, "Failed to load from cloud", Toast.LENGTH_SHORT).show(); // Show error message
                }
            });
        }
    }

    // Check network connection
    private boolean isNetworkAvailable() { // Method to check network
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Get connectivity manager service
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Get current network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Return true if network available
    }

    // Display course information on UI
    private void showCourseInfo(Course course) { // Method to display course information
        this.course = course; // Store course object
        textViewName.setText(course.getName()); // Display course name
        textViewDescription.setText(course.getDescription()); // Display description
        textViewSchedule.setText(DateUtils.getNextUpcomingDate(course.getSchedule())); // Display next upcoming date
        textViewTime.setText(course.getTime() != null ? course.getTime() : "Not set"); // Display time or "Not set"
        textViewCapacity.setText(String.format(Locale.getDefault(), "%d Students", course.getCapacity())); // Display capacity
        textViewPrice.setText(String.format(Locale.US, "$%.2f", course.getPrice())); // Display price with currency format
        textViewDuration.setText(String.format(Locale.getDefault(), "%d min", course.getDuration())); // Display duration

        if (course.getNote() != null && !course.getNote().isEmpty()) { // If note exists
            textViewNote.setText(course.getNote()); // Display note
            textViewNote.setVisibility(View.VISIBLE); // Show note TextView
        } else {
            textViewNote.setVisibility(View.GONE); // Hide note TextView
        }
        // Save schedule to pass to add/edit class instance screen
        courseSchedule = course.getSchedule(); // Store course schedule
    }

    // Show course deletion confirmation dialog
    private void confirmDelete() { // Method to confirm deletion
        new AlertDialog.Builder(this) // Create AlertDialog builder
                .setTitle("Delete Course") // Set title
                .setMessage("Are you sure you want to delete this course? This will also delete all related class sessions.") // Set message content
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() { // Delete confirmation button
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // When Delete button is pressed
                        deleteCourse(); // Call delete course method
                    }
                })
                .setNegativeButton("Cancel", null) // Cancel button
                .show(); // Show dialog
    }

    // Delete course: delete class instances on Firebase first, then delete course
    private void deleteCourse() { // Method to delete course
        if (courseId == null) return; // If no courseId then exit

        // Delete class instances on Firebase first
        firebaseManager.deleteClassInstancesByCourseId(courseId, new DatabaseReference.CompletionListener() { // Delete instances on Firebase
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) { // When instance deletion completes
                // After deleting instances, delete course
                firebaseManager.deleteCourse(courseId, new DatabaseReference.CompletionListener() { // Delete course on Firebase
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) { // When course deletion completes
                        if (error == null) { // If no error
                            // Delete local after successful cloud deletion
                            db.courseDao().deleteByFirebaseId(courseId); // Delete course in Room database
                            // Delete local class instances
                            db.classInstanceDao().deleteInstancesByCourseFirebaseId(courseId); // Delete instances in Room database
                            Toast.makeText(CourseDetailActivity.this, "Course and related class instances deleted successfully", Toast.LENGTH_SHORT).show(); // Show success message
                            finish(); // Close Activity
                        } else {
                            Toast.makeText(CourseDetailActivity.this, "Error deleting course", Toast.LENGTH_SHORT).show(); // Show error message
                        }
                    }
                });
            }
        });
    }

    // Show class instance deletion confirmation dialog
    private void confirmDeleteInstance(ClassInstance instance) { // Method to confirm instance deletion
        new AlertDialog.Builder(this) // Create AlertDialog builder
                .setTitle("Delete Class Session") // Set title
                .setMessage("Are you sure you want to delete this class session?") // Set message content
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() { // Delete confirmation button
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // When Delete button is pressed
                        deleteClassInstance(instance); // Call delete instance method
                    }
                })
                .setNegativeButton("Cancel", null) // Cancel button
                .show(); // Show dialog
    }

    // Delete class instance: delete on Firebase first, then delete local
    private void deleteClassInstance(ClassInstance instance) { // Method to delete class instance
        // Delete on Firebase first
        if (instance.getId() != null) { // If has Firebase ID
            firebaseManager.deleteClassInstance(instance.getId(), new DatabaseReference.CompletionListener() { // Delete on Firebase
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) { // When deletion completes
                    // Delete local regardless of Firebase result
                    if (instance.getLocalId() > 0) { // If has local ID
                        db.classInstanceDao().deleteByLocalId(instance.getLocalId()); // Delete by local ID
                    } else if (instance.getId() != null) { // If has Firebase ID
                        db.classInstanceDao().deleteByFirebaseId(instance.getId()); // Delete by Firebase ID
                    }

                    runOnUiThread(() -> { // Run on UI thread
                        if (error == null) { // If no error
                            Toast.makeText(CourseDetailActivity.this, "Class session deleted successfully", Toast.LENGTH_SHORT).show(); // Show success message
                        } else {
                            Toast.makeText(CourseDetailActivity.this, "Deleted locally, sync failed", Toast.LENGTH_SHORT).show(); // Show sync failed message
                        }
                        // Refresh the list
                        if (classInstanceAdapter != null) { // If adapter exists
                            loadClassInstances(courseId); // Reload class instances
                        }
                    });
                }
            });
        } else {
            // If no Firebase ID, only delete local
            if (instance.getLocalId() > 0) { // If has local ID
                db.classInstanceDao().deleteByLocalId(instance.getLocalId()); // Delete by local ID
                Toast.makeText(CourseDetailActivity.this, "Class session deleted locally", Toast.LENGTH_SHORT).show(); // Show local deletion message
                if (classInstanceAdapter != null) { // If adapter exists
                    loadClassInstances(courseId); // Reload class instances
                }
            }
        }
    }
}
