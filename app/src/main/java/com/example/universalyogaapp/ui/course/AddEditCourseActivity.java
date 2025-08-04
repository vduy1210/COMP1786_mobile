

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
import android.app.TimePickerDialog; //Time picker dialog
import java.util.Calendar; // Calendar
import java.util.Arrays; // TArray utilities
import java.util.List; //List interface
import java.util.stream.Collectors; //Stream collectors
import androidx.room.Room; //Room database builder
import com.example.universalyogaapp.db.AppDatabase; //Application database
import com.example.universalyogaapp.db.CourseEntity; //Course entity
import android.net.ConnectivityManager; //Network connectivity manager
import android.net.NetworkInfo; //Network information


// Activity that allows adding or editing a course
public class AddEditCourseActivity extends AppCompatActivity {
    //Declare UI component variables
    private TextInputEditText editTextName, editTextTime, editTextCapacity, editTextPrice, editTextDuration, editTextDescription, editTextNote; //Input fields
    private ChipGroup chipGroupSchedule; //Chip group for selecting study days
    private Button buttonSave; //Save button

    //Declare data and processing variables
    private FirebaseManager firebaseManager; //Firebase sync manager
    private Course editingCourse; //Course being edited if in edit mode
    private String courseId; //Course ID on Firebase
    private AppDatabase db; // Room database - Room database instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call parent method
        setContentView(R.layout.activity_add_edit_course); // Set layout for Activity

        //Initialize Room database with migration
        db = Room.databaseBuilder(
            getApplicationContext(), //Application context
            AppDatabase.class, //Database class
            "yoga-db" //Database name
        ).allowMainThreadQueries() //Allow main thread queries
                         .addMigrations(AppDatabase.MIGRATION_5_6) //Add migration from version 5 to 6
        .build(); //Build database

        //Initialize views from layout
        editTextName = findViewById(R.id.editTextName); //Find and assign course name input field
        chipGroupSchedule = findViewById(R.id.chipGroupSchedule); //Find and assign schedule chip group
        editTextTime = findViewById(R.id.editTextTime); //Find and assign time input field
        
        //Time selection event when clicked
        editTextTime.setOnClickListener(new View.OnClickListener() { //Assign listener to time input field
            @Override
            public void onClick(View v) { //When time field is clicked
                showTimePicker(); //Show time picker
            }
        });

        //Time selection event when focused
        editTextTime.setOnFocusChangeListener(new View.OnFocusChangeListener() { //Assign focus listener to time input field
            @Override
            public void onFocusChange(View v, boolean hasFocus) { //When focus state changes
                if (hasFocus) { // If focused
                    showTimePicker(); //Show time picker
                }
            }
        });

        editTextCapacity = findViewById(R.id.editTextCapacity); //Find and assign capacity input field
        editTextPrice = findViewById(R.id.editTextPrice); //Find and assign price input field
        editTextDuration = findViewById(R.id.editTextDuration); //Find and assign duration input field
        editTextDescription = findViewById(R.id.editTextDescription); //Find and assign description input field
        editTextNote = findViewById(R.id.editTextNote); //Find and assign note input field
        buttonSave = findViewById(R.id.buttonSave); //Find and assign save button

        firebaseManager = new FirebaseManager(); //Initialize Firebase manager

        //Get courseId from Intent to determine edit/add mode
        courseId = getIntent().getStringExtra("course_id"); //Get course ID from Intent
        if (courseId != null) { //If ID exists (edit mode)
            setTitle("Edit Course"); //Set title for edit screen
            loadCourse(courseId); //Load course data
        } else { //If no ID (add mode)
            setTitle("Add New Course"); //Set title for add screen
        }

        //Save course event
        buttonSave.setOnClickListener(new View.OnClickListener() { //Assign listener to save button
            @Override
            public void onClick(View v) { //When save button is clicked
                showConfirmDialog(); //Show confirmation dialog
            }
        });
    }

    // Load course data from Firebase for editing
    private void loadCourse(String id) {
        firebaseManager.getCourseById(id, new ValueEventListener() { //Get course by ID
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { //When data changes
                editingCourse = snapshot.getValue(Course.class); //Convert snapshot to Course object
                if (editingCourse != null) { //If course data exists
                    editingCourse.setId(snapshot.getKey()); //Set ID for course
                    fillCourseData(editingCourse); //Fill data to interface
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //When error occurs
                Toast.makeText(AddEditCourseActivity.this, "Error loading data", Toast.LENGTH_SHORT).show(); //Show error message
            }
        });
    }

    // Fill course data to interface when editing
    private void fillCourseData(Course course) {
        editTextName.setText(course.getName()); // Set course name to input field

        // Mark selected study day chips
        String schedule = course.getSchedule(); // Get schedule
        if (schedule != null && !schedule.isEmpty()) { // If schedule exists
            List<String> selectedDays = Arrays.asList(schedule.split(",")); // Split schedule into day list
            for (int i = 0; i < chipGroupSchedule.getChildCount(); i++) { // Iterate through all chips
                Chip chip = (Chip) chipGroupSchedule.getChildAt(i); // Get chip at position i
                if (selectedDays.contains(chip.getText().toString())) { // If this day is selected
                    chip.setChecked(true); // Mark chip as checked
                }
            }
        }

        editTextTime.setText(course.getTime()); // Set time to input field

        editTextCapacity.setText(String.valueOf(course.getCapacity())); // Convert number to string and set to capacity field
        editTextPrice.setText(String.valueOf(course.getPrice())); // Convert number to string and set to price field
        editTextDuration.setText(String.valueOf(course.getDuration())); // Convert number to string and set to duration field
        editTextDescription.setText(course.getDescription()); // Set description to input field
        editTextNote.setText(course.getNote()); // Set note to input field
    }

    // Check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Get connectivity manager service
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Get active network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Return true if network is available
    }

    //Show confirmation dialog before saving course
    private void showConfirmDialog() {
        String name = editTextName.getText().toString().trim(); // Get course name from input field
        List<String> selectedChips = new java.util.ArrayList<>(); // Initialize selected days list
        for (int id : chipGroupSchedule.getCheckedChipIds()) { // Iterate through checked chips
            Chip chip = chipGroupSchedule.findViewById(id); // Find chip by ID
            selectedChips.add(chip.getText().toString()); // Add chip text to list
        }
        String schedule = String.join(",", selectedChips); // Join days with comma
        String upcomingDate = DateUtils.getNextUpcomingDate(schedule); // Calculate next upcoming date
        String time = editTextTime.getText().toString().trim(); // Get time from input field
        String capacityStr = editTextCapacity.getText().toString().trim(); // Get capacity from input field
        String priceStr = editTextPrice.getText().toString().trim(); // Get price from input field
        String durationStr = editTextDuration.getText().toString().trim(); // Get duration from input field
        String description = editTextDescription.getText().toString().trim(); // Get description from input field
        String note = editTextNote.getText().toString().trim(); // Get note from input field

        // Validate input data
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(schedule) || // If name or schedule is empty
                TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(priceStr) || // If capacity or price is empty
                TextUtils.isEmpty(durationStr)) { // If duration is empty
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show(); // Show error message
            return; // Stop processing
        }

        // Create confirmation message
        StringBuilder message = new StringBuilder(); // Initialize StringBuilder
        message.append("Name: ").append(name).append("\n"); // Add course name
        message.append("Schedule: ").append(schedule).append("\n"); // Add schedule
        message.append("Time: ").append(time).append("\n"); // Add time
        message.append("Capacity: ").append(capacityStr).append("\n"); // Add capacity
        message.append("Price: ").append(priceStr).append("\n"); // Add price
        message.append("Duration: ").append(durationStr).append("\n"); // Add duration
        message.append("Description: ").append(description).append("\n"); // Add description
        message.append("Note: ").append(note).append("\n"); // Add note

        new AlertDialog.Builder(this) // Create confirmation dialog
            .setTitle("Confirm Course Details") // Set title
            .setMessage(message.toString()) // Set message
            .setPositiveButton("Confirm", (dialog, which) -> saveCourse()) // Confirm button
            .setNegativeButton("Edit", null) // Edit button (do nothing)
            .show(); // Show dialog
    }

    // Save new course or update existing course
    private void saveCourse() {
        String name = editTextName.getText().toString().trim(); // Get course name
        List<String> selectedChips = new java.util.ArrayList<>(); // Initialize selected study days list
        for (int id : chipGroupSchedule.getCheckedChipIds()) { // Iterate through checked chips
            Chip chip = chipGroupSchedule.findViewById(id); // Find chip by ID
            selectedChips.add(chip.getText().toString()); // Add chip text to list
        }
        String schedule = String.join(",", selectedChips); // Join days into schedule string
        String upcomingDate = DateUtils.getNextUpcomingDate(schedule); // Calculate next upcoming date
        String time = editTextTime.getText().toString().trim(); // Get time
        String capacityStr = editTextCapacity.getText().toString().trim(); // Get capacity as string
        String priceStr = editTextPrice.getText().toString().trim(); // Get price as string
        String durationStr = editTextDuration.getText().toString().trim(); // Get duration as string
        String description = editTextDescription.getText().toString().trim(); // Get description
        String note = editTextNote.getText().toString().trim(); // Get note

        // Convert strings to numbers
        int capacity = Integer.parseInt(capacityStr); // Convert capacity to integer
        double price = Double.parseDouble(priceStr); // Convert price to double
        int duration = Integer.parseInt(durationStr); // Convert duration to integer

        if (editingCourse != null) { // If editing existing course
            Course course = new Course( // Create new Course object
                editingCourse.getId(), name, schedule, time, // Old ID, new name, new schedule, new time
                capacity, price, duration, description, note, upcomingDate, editingCourse.getLocalId() // Các thông tin mới - New information
            );

            if (isNetworkAvailable()) { // If network is available
                // If online: update Firebase first, then local
                DatabaseReference.CompletionListener listener = (error, ref) -> { // Result handling listener
                    if (error == null) { // If no error
                        // Update local database
                        CourseEntity entity = new CourseEntity(); // Create new entity
                        entity.localId = editingCourse.getLocalId(); // Set old local ID
                        entity.firebaseId = editingCourse.getId(); // Set old Firebase ID
                        entity.name = name; // Set new name
                        entity.schedule = schedule; // Set new schedule
                        entity.time = time; // Set new time
                        entity.capacity = capacity; // Set new capacity
                        entity.price = price; // Set new price
                        entity.duration = duration; // Set new duration
                        entity.description = description; // Set new description
                        entity.note = note; // Set new note
                        entity.upcomingDate = upcomingDate; // Set next upcoming date
                        entity.isSynced = true; // Mark as synced

                        db.courseDao().update(entity); // Update to database
                        runOnUiThread(() -> { // Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Course updated and synced!", Toast.LENGTH_SHORT).show(); // Show success message
                            finish(); // Close Activity
                        });
                    } else { // If error occurs
                        runOnUiThread(() -> { // Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Show failure message
                            finish(); // Close Activity
                        });
                    }
                };
                firebaseManager.updateCourse(course, listener); // Update to Firebase
            } else { // If no network
                // If offline: only update locally
                CourseEntity entity = new CourseEntity(); // Create new entity
                entity.localId = editingCourse.getLocalId(); // Set old local ID
                entity.firebaseId = editingCourse.getId(); // Set old Firebase ID
                entity.name = name; // Set new name
                entity.schedule = schedule; // Set new schedule
                entity.time = time; // Set new time
                entity.capacity = capacity; // Set new capacity
                entity.price = price; // Set new price
                entity.duration = duration; // Set new duration
                entity.description = description; // Set new description
                entity.note = note; // Set new note
                entity.upcomingDate = upcomingDate; // Set next upcoming date
                entity.isSynced = false; // Mark as not synced

                db.courseDao().update(entity); // Update to local database
                Toast.makeText(AddEditCourseActivity.this, "Course updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Show message
                finish(); // Close Activity
            }
        } else { // If adding new course
            // If adding new course
            CourseEntity entity = new CourseEntity(); // Create new entity
            entity.name = name; // Set name
            entity.schedule = schedule; // Set schedule
            entity.time = time; // Set time
            entity.capacity = capacity; // Set capacity
            entity.price = price; // Set price
            entity.duration = duration; // Set duration
            entity.description = description; // Set description
            entity.note = note; // Set note
            entity.upcomingDate = upcomingDate; // Set next upcoming date

            if (isNetworkAvailable()) { // If network is available
                // If online: save locally with isSynced=true, push to Firebase
                entity.isSynced = true; // Mark as synced
                long localId = db.courseDao().insert(entity); // Save to database and get local ID
                // Set courseId = localId (auto-increment, unique)
                entity.courseId = String.valueOf(localId); // Convert local ID to courseId string
                db.courseDao().update(entity); // Update entity with courseId

                Course course = new Course( // Create Course object for Firebase
                    entity.firebaseId, entity.name, entity.schedule, entity.time, // Firebase ID, name, schedule, time
                    entity.capacity, entity.price, entity.duration, entity.description, entity.note, entity.upcomingDate, (int) localId // Other info and local ID
                );
                course.setCourseId(entity.courseId); // Set courseId for Course object

                DatabaseReference.CompletionListener listener = (error, ref) -> { // Firebase result handling listener
                    if (error == null) { // If successful
                        db.courseDao().markCourseAsSynced((int) localId, ref.getKey()); // Mark as synced and update Firebase ID
                        runOnUiThread(() -> { // Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Course saved and synced!", Toast.LENGTH_SHORT).show(); // Show success message
                            finish(); // Close Activity
                        });
                    } else { // If failed
                        runOnUiThread(() -> { // Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Show failure message
                            finish(); // Close Activity
                        });
                    }
                };

                if (entity.firebaseId == null || entity.firebaseId.isEmpty()) { // If no Firebase ID yet
                    firebaseManager.addCourse(course, listener); // Add new to Firebase
                } else { // If Firebase ID exists
                    course.setId(entity.firebaseId); // Set Firebase ID
                    firebaseManager.updateCourse(course, listener); // Update to Firebase
                }
            } else { // If no network
                // If offline: save locally with isSynced=false
                entity.isSynced = false; // Mark as not synced
                long localId = db.courseDao().insert(entity); // Save to local database
                entity.courseId = String.valueOf(localId); // Set courseId = localId
                db.courseDao().update(entity); // Update entity
                Toast.makeText(AddEditCourseActivity.this, "Course saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Show message
                finish(); // Close Activity
            }
        }
    }

    // Display TimePicker for time selection
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance(); // Get current calendar
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Get current hour (24h format)
        int minute = calendar.get(Calendar.MINUTE); // Get current minute
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> { // Create time picker dialog
            String time = String.format("%02d:%02d", hourOfDay, minute1); // Format time as HH:mm
            editTextTime.setText(time); // Set selected time to input field
        }, hour, minute, true); // Initialize with current time, 24h format
        timePickerDialog.show(); // Show dialog
    }
}