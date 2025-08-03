// Activity for adding/editing class instance functionality
package com.example.universalyogaapp.ui.course;

// Import necessary libraries for this Activity
import android.app.DatePickerDialog; // Date picker dialog
import android.os.Bundle; // Bundle for passing data
import android.text.TextUtils; // Check empty strings
import android.view.View; // Basic view
import android.widget.Button; // Button widget
import android.widget.DatePicker; // Date picker widget
import android.widget.EditText; // Text input widget
import android.widget.Toast; // Display toast messages
import androidx.annotation.Nullable; // Nullable annotation
import androidx.appcompat.app.AppCompatActivity; // Base AppCompat activity
import com.example.universalyogaapp.R; // Layout resources
import com.example.universalyogaapp.firebase.FirebaseManager; // Firebase sync manager
import com.example.universalyogaapp.model.ClassInstance; // Class instance model
import com.example.universalyogaapp.db.AppDatabase; // Room database
import com.example.universalyogaapp.db.ClassInstanceEntity; // Class instance entity
import com.example.universalyogaapp.db.CourseEntity; // Course entity
import java.text.ParseException; // Parse exception
import java.text.SimpleDateFormat; // Date formatting
import java.util.Calendar; // Calendar
import java.util.Date; // Date
import java.util.Locale; // Localization
import android.net.ConnectivityManager; // Network connectivity manager
import android.net.NetworkInfo; // Network information
import java.util.List; // List collection

// Activity that allows adding or editing a class instance
public class AddEditClassInstanceActivity extends AppCompatActivity {
    // Declare UI component variables
    private EditText editTextDate, editTextTeacher, editTextNote; // Input fields
    private Button buttonSave; // Save button
    
    // Declare data variables
    private String courseFirebaseId, courseSchedule; // Course Firebase ID and schedule
    private int courseLocalId; // Local course ID
    private FirebaseManager firebaseManager; // Firebase sync manager
    private ClassInstance editingInstance; // Instance being edited if in edit mode
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Date format
    private AppDatabase db; // Room database instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call parent method
        setContentView(R.layout.activity_add_edit_class_instance); // Set layout for Activity

        // Initialize views from layout
        editTextDate = findViewById(R.id.editTextDate); // Find and assign date input field
        editTextTeacher = findViewById(R.id.editTextTeacher); // Find and assign teacher input field
        editTextNote = findViewById(R.id.editTextNote); // Find and assign note input field
        buttonSave = findViewById(R.id.buttonSaveInstance); // Find and assign save button
        firebaseManager = new FirebaseManager(); // Initialize Firebase manager

        // Initialize Room database with migration
        db = androidx.room.Room.databaseBuilder(
            getApplicationContext(), // Application context
            AppDatabase.class, // Database class
            "yoga-db" // Database name
        ).allowMainThreadQueries() // Allow main thread queries
         .addMigrations(AppDatabase.MIGRATION_5_6) // Add migration from version 5 to 6
        .build(); // Build database

        // Get course and instance info from Intent
        courseFirebaseId = getIntent().getStringExtra("course_id"); // Get course Firebase ID
        courseSchedule = getIntent().getStringExtra("course_schedule"); // Get course schedule
        editingInstance = (ClassInstance) getIntent().getSerializableExtra("class_instance"); // Get instance being edited

        // Get local course ID from database based on Firebase ID
        if (courseFirebaseId != null) { // If Firebase ID exists
            CourseEntity courseEntity = db.courseDao().getCourseByFirebaseId(courseFirebaseId); // Find course by Firebase ID
            if (courseEntity != null) { // If course found
                courseLocalId = courseEntity.localId; // Get local ID
            }
        }

        // If editing, display existing data
        if (editingInstance != null) { // If there's an instance to edit
            setTitle("Edit Class Session"); // Set title for edit screen
            editTextDate.setText(editingInstance.getDate()); // Display existing date
            editTextTeacher.setText(editingInstance.getTeacher()); // Display existing teacher name
            editTextNote.setText(editingInstance.getNote()); // Display existing note
        } else { // If no instance (adding new)
            setTitle("Add Class Session"); // Set title for add screen
        }

        // Date selection event
        editTextDate.setOnClickListener(new View.OnClickListener() { // Assign listener to date input field
            @Override
            public void onClick(View v) { // When date field is clicked
                showDatePicker(); // Show date picker
            }
        });

        // Save instance event
        buttonSave.setOnClickListener(new View.OnClickListener() { // Assign listener to save button
            @Override
            public void onClick(View v) { // When save button is clicked
                saveInstance(); // Call save method
            }
        });
    }

    // Display DatePicker for date selection
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(); // Get current calendar
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() { // Create date picker dialog
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { // When user selects date
                String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth); // Format date as yyyy-MM-dd
                editTextDate.setText(dateStr); // Set selected date to input field
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)); // Initialize with current date
        dialog.show(); // Show dialog
    }

    // Check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Get connectivity manager service
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Get active network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Return true if network is available
    }

    // Save new instance or update existing instance
    private void saveInstance() {
        String date = editTextDate.getText().toString().trim(); // Get date from input field
        String teacher = editTextTeacher.getText().toString().trim(); // Get teacher name from input field
        String note = editTextNote.getText().toString().trim(); // Get note from input field

        // Validate input data
        if (TextUtils.isEmpty(date)) { // If date is empty
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show(); // Show error message
            return; // Stop processing
        }

        // Check if date matches course schedule
        if (courseSchedule != null && !courseSchedule.isEmpty()) { // If course schedule is defined
            if (!isDateMatchSchedule(date, courseSchedule)) { // If date doesn't match schedule
                Toast.makeText(this, "Selected date does not match course schedule (" + courseSchedule + ")", Toast.LENGTH_SHORT).show(); // Show error message
                return; // Stop processing
            }
        }

        // If adding new instance
        if (editingInstance == null) {
            addNewInstance(date, teacher, note); // Call add new method
        } else {
            // If editing existing instance
            editExistingInstance(date, teacher, note); // Call edit method
        }
    }

    // Add a new class instance
    private void addNewInstance(String date, String teacher, String note) {
        // Check for duplicate date in same course
        List<ClassInstanceEntity> existingInstances = db.classInstanceDao().getInstancesForCourse(courseFirebaseId); // Get all instances for course
        for (ClassInstanceEntity existing : existingInstances) { // Iterate through existing instances
            if (existing.date.equals(date)) { // If date matches
                Toast.makeText(this, "A class session with this date already exists", Toast.LENGTH_SHORT).show(); // Show error message
                return; // Stop processing
            }
        }

        // Create new entity
        ClassInstanceEntity entity = new ClassInstanceEntity(); // Initialize new entity
        entity.courseId = courseFirebaseId; // Set course Firebase ID
        entity.courseLocalId = courseLocalId; // Set course local ID
        entity.firebaseId = null; // No Firebase ID yet
        entity.date = date; // Set date
        entity.teacher = teacher; // Set teacher name
        entity.note = note; // Set note
        entity.isSynced = false; // Not synced yet

        if (isNetworkAvailable()) { // If network is available
            // If online: save locally first, then sync to Firebase
            long localId = db.classInstanceDao().insert(entity); // Save to local database and get ID

            ClassInstance instance = new ClassInstance( // Create ClassInstance object for Firebase
                null, courseFirebaseId, date, teacher, note, (int) localId // null Firebase ID, with local ID
            );

            firebaseManager.addClassInstance(instance, (error, ref) -> { // Add to Firebase
                if (error == null) { // If successful
                    // If sync successful: update Firebase ID and mark as synced
                    db.classInstanceDao().markInstanceAsSynced((int) localId, ref.getKey()); // Update Firebase ID and sync status
                    runOnUiThread(() -> { // Run on UI thread
                        Toast.makeText(AddEditClassInstanceActivity.this, "Class session saved and synced!", Toast.LENGTH_SHORT).show(); // Show success message
                        finish(); // Close Activity
                    });
                } else { // If failed
                    // If sync failed: only save locally
                    runOnUiThread(() -> { // Run on UI thread
                        Toast.makeText(AddEditClassInstanceActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Show message
                        finish(); // Close Activity
                    });
                }
            });
        } else { // If no network
            // If offline: only save locally
            db.classInstanceDao().insert(entity); // Save to local database
            Toast.makeText(this, "Class session saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Show message
            finish(); // Close Activity
        }
    }

    // Edit an existing class instance
    private void editExistingInstance(String date, String teacher, String note) {
        ClassInstanceEntity entity = null; // Initialize entity as null

        // Find entity by Firebase ID first
        if (editingInstance.getId() != null) { // If Firebase ID exists
            entity = db.classInstanceDao().getInstanceByFirebaseId(editingInstance.getId()); // Find by Firebase ID
        }

        // If not found, try by local ID
        if (entity == null && editingInstance.getLocalId() > 0) { // If not found and has local ID
            entity = db.classInstanceDao().getInstanceByLocalId(editingInstance.getLocalId()); // Find by local ID
        }

        if (entity != null) { // If entity found
            final ClassInstanceEntity finalEntity = entity; // Final variable for lambda
            finalEntity.date = date; // Update date
            finalEntity.teacher = teacher; // Update teacher
            finalEntity.note = note; // Update note

            if (isNetworkAvailable() && finalEntity.firebaseId != null) { // If online and has Firebase ID
                // If online and has Firebase ID: update both local and Firebase
                finalEntity.isSynced = true; // Mark as synced
                db.classInstanceDao().update(finalEntity); // Update local database

                ClassInstance instance = new ClassInstance( // Create object for Firebase sync
                    finalEntity.firebaseId, finalEntity.courseId, date, teacher, note, finalEntity.id // With existing Firebase ID
                );

                firebaseManager.updateClassInstance(instance, (error, ref) -> { // Update Firebase
                    if (error == null) { // If successful
                        runOnUiThread(() -> { // Run on UI thread
                            Toast.makeText(AddEditClassInstanceActivity.this, "Class session updated and synced!", Toast.LENGTH_SHORT).show(); // Show success message
                            finish(); // Close Activity
                        });
                    } else { // If failed
                        // If sync failed: mark as not synced
                        finalEntity.isSynced = false; // Mark as not synced
                        db.classInstanceDao().update(finalEntity); // Update database again
                        runOnUiThread(() -> { // Run on UI thread
                            Toast.makeText(AddEditClassInstanceActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Show message
                            finish(); // Close Activity
                        });
                    }
                });
            } else { // If offline or no Firebase ID
                // If offline or no Firebase ID: only update locally
                finalEntity.isSynced = false; // Mark as not synced
                db.classInstanceDao().update(finalEntity); // Update local database
                Toast.makeText(this, "Class session updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Show message
                finish(); // Close Activity
            }
        } else { // If entity not found
            Toast.makeText(this, "Error: Could not find class session to edit", Toast.LENGTH_SHORT).show(); // Show error message
        }
    }

    // Check if class date matches course schedule
    private boolean isDateMatchSchedule(String dateStr, String schedule) {
        try {
            Date date = sdf.parse(dateStr); // Parse date string to Date object
            Calendar cal = Calendar.getInstance(); // Get Calendar instance
            cal.setTime(date); // Set time for Calendar
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Get day of week (1=Sunday, 2=Monday,...)
            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Array of day names
            String dayName = days[dayOfWeek - 1]; // Get corresponding day name
            return schedule != null && schedule.contains(dayName); // Check if schedule contains day name
        } catch (ParseException e) { // If parse error
            return false; // Return false
        }
    }
}
