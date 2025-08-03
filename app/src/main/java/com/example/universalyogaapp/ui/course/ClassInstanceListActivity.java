// Activity to display list of all class instances, allowing search, edit/delete operations
package com.example.universalyogaapp.ui.course;

// Import necessary libraries for this Activity
import android.os.Bundle; // Bundle for passing data
import android.text.Editable; // Interface for editable text
import android.text.TextWatcher; // Listener for text changes
import android.widget.EditText; // Text input widget
import androidx.annotation.Nullable; // Nullable annotation
import androidx.appcompat.app.AppCompatActivity; // Base AppCompat activity
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for lists
import com.example.universalyogaapp.R; // Layout resources
import com.example.universalyogaapp.db.AppDatabase; // Application database
import com.example.universalyogaapp.db.ClassInstanceEntity; // Class instance entity
import com.example.universalyogaapp.model.ClassInstance; // Class instance model
import java.text.ParseException; // Parse exception
import java.text.SimpleDateFormat; // Date formatting
import java.util.ArrayList; // ArrayList collection
import java.util.Calendar; // Calendar
import java.util.Date; // Date
import java.util.List; // List interface
import java.util.Locale; // Localization

// Activity to display list of all class instances, allowing search, edit/delete operations
public class ClassInstanceListActivity extends AppCompatActivity {
    // Declare UI component variables
    private EditText editTextSearch; // Search input field for class instances
    private RecyclerView recyclerView; // RecyclerView to display class instance list
    private ClassInstanceAdapter adapter; // Adapter for RecyclerView
    
    // Declare data variables
    private AppDatabase db; // Room database instance
    private List<ClassInstance> allInstances = new ArrayList<>(); // List of all class instances
    private android.widget.Button buttonGoToCourseManage; // Button to navigate to course management

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call parent method
        setContentView(R.layout.activity_class_instance_list); // Set layout for Activity
        
        // Initialize views from layout
        editTextSearch = findViewById(R.id.editTextSearchClassInstance); // Find and assign search input field
        recyclerView = findViewById(R.id.recyclerViewClassInstances); // Find and assign RecyclerView
        buttonGoToCourseManage = findViewById(R.id.buttonGoToCourseManage); // Find and assign navigation button
        
        // Set up layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Use vertical LinearLayoutManager
        
        // Initialize adapter for class instance list
        adapter = new ClassInstanceAdapter(new ArrayList<>(), new ClassInstanceAdapter.OnInstanceActionListener() { // Create adapter with empty list and listener
            @Override
            public void onEdit(ClassInstance instance) { // Method called when editing instance
                // Pass complete ClassInstance object to edit screen
                android.content.Intent intent = new android.content.Intent(ClassInstanceListActivity.this, AddEditClassInstanceActivity.class); // Create Intent to navigate Activity
                intent.putExtra("class_instance", instance); // Pass instance object via Intent
                startActivity(intent); // Start Activity
            }
            @Override
            public void onDelete(ClassInstance instance) { // Method called when deleting instance
                // Delete instance from database and reload list
                new android.app.AlertDialog.Builder(ClassInstanceListActivity.this) // Create confirmation dialog
                        .setTitle("Delete Class Session") // Set dialog title
                        .setMessage("Are you sure you want to delete this class session?") // Set confirmation message
                        .setPositiveButton("Delete", (dialog, which) -> { // Delete confirmation button
                            db.classInstanceDao().deleteByLocalId(instance.getLocalId()); // Delete from database by local ID
                            loadAllInstances(); // Reload list
                        })
                        .setNegativeButton("Cancel", null) // Cancel button (do nothing)
                        .show(); // Show dialog
            }
        });
        recyclerView.setAdapter(adapter); // Set adapter for RecyclerView
        
        db = AppDatabase.getInstance(getApplicationContext()); // Get database instance
        loadAllInstances(); // Load all instances from database
        
        // Set up class instance search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() { // Add text change listener
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Before text changes (no processing)
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // When text is changing
                String input = s.toString(); // Get input string
                if (input.trim().isEmpty()) { // If search string is empty
                    // If search is cleared, reload complete list
                    adapter.setInstanceList(new ArrayList<>(allInstances)); // Display all instances again
                } else { // If there's search keyword
                    filterResults(input); // Filter results by keyword
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {} // After text changes (no processing)
        });
        
        // Set up navigation to course management screen
        buttonGoToCourseManage.setOnClickListener(v -> { // Listener for navigation button
            startActivity(new android.content.Intent(ClassInstanceListActivity.this, CourseListActivity.class)); // Navigate to CourseListActivity
        });
    }

    // Load all class instances from local database
    private void loadAllInstances() {
        List<ClassInstanceEntity> entityList = db.classInstanceDao().getAllClassInstances(); // Get all entities from database
        allInstances.clear(); // Clear old list
        for (ClassInstanceEntity entity : entityList) { // Iterate through each entity
            allInstances.add(new ClassInstance( // Convert entity to model and add to list
                entity.firebaseId, // Firebase ID of instance
                entity.courseId, // Course ID
                entity.date, // Date
                entity.teacher, // Teacher name
                entity.note, // Note
                entity.id // Local ID
            ));
        }
        adapter.setInstanceList(new ArrayList<>(allInstances)); // Update adapter with new list
    }

    // Filter class instances by keyword (teacher, date, day, etc.)
    private void filterResults(String keyword) {
        String lower = keyword.trim().toLowerCase(); // Convert keyword to lowercase and trim whitespace
        List<ClassInstance> filtered = new ArrayList<>(); // Filtered results list
        
        // Support multiple date formats
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] { // Array of supported date formats
            new SimpleDateFormat("yyyy-MM-dd", Locale.US), // ISO format
            new SimpleDateFormat("dd/MM/yyyy", Locale.US), // European format
            new SimpleDateFormat("d/M/yyyy", Locale.US) // Short format
        };
        
        // Variables to store day from input date
        String weekdayFromDate = null; // Day parsed from date
        Date parsedDate = null; // Parsed date
        
        // Try parsing keyword as date with different formats
        for (SimpleDateFormat sdf : dateFormats) { // Iterate through each format
            try {
                parsedDate = sdf.parse(keyword); // Try parsing keyword as date
                if (parsedDate != null) { // If parsing successful
                    Calendar cal = Calendar.getInstance(); // Get Calendar instance
                    cal.setTime(parsedDate); // Set time for Calendar
                    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Array of day names
                    weekdayFromDate = days[cal.get(Calendar.DAY_OF_WEEK) - 1].toLowerCase(); // Get day name and convert to lowercase
                    break; // Break loop when parsing successful
                }
            } catch (ParseException ignored) {} // Ignore parse errors
        }
        
        // Supported English day keywords
        String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", // Full names
                "mon", "tue", "wed", "thu", "fri", "sat", "sun"}; // Abbreviated names

        // Normalize keyword to compare date parts
        String[] keywordParts = lower.split("/|-"); // Split keyword by / or -

        // Iterate through all instances for searching
        for (ClassInstance instance : allInstances) { // Iterate through each instance
            boolean match = false; // Variable to mark if there's a match
            
            // Search by teacher name
            if (instance.getTeacher() != null && !lower.isEmpty() && instance.getTeacher().toLowerCase().contains(lower)) match = true; // If teacher name contains keyword

            // Search by exact date format or partial date
            if (!match && instance.getDate() != null && !lower.isEmpty()) { // If not matched yet and has date
                String dateStr = instance.getDate(); // Get date string in yyyy-MM-dd format
                String[] dateParts = dateStr.split("-"); // Split date into parts
                // dateParts[0]=year, [1]=month, [2]=day
                String day = dateParts.length > 2 ? dateParts[2] : ""; // Get day (dd)
                String month = dateParts.length > 1 ? dateParts[1] : ""; // Get month (MM)
                String year = dateParts.length > 0 ? dateParts[0] : ""; // Get year (yyyy)
                
                // Compare keyword parts with date
                if (keywordParts.length == 1) { // If only 1 part entered
                    // If only 1 part (e.g. "01"), compare with day, month, year
                    if (day.equalsIgnoreCase(keywordParts[0]) || month.equalsIgnoreCase(keywordParts[0]) || year.equalsIgnoreCase(keywordParts[0])) { // Case-insensitive comparison
                        match = true; // Mark as matched
                    }
                } else if (keywordParts.length == 2) { // If 2 parts entered
                    // If 2 parts (e.g. "08/2025"), compare month/year or day/month
                    if ((month.equalsIgnoreCase(keywordParts[0]) && year.equalsIgnoreCase(keywordParts[1])) || // Month/year
                        (day.equalsIgnoreCase(keywordParts[0]) && month.equalsIgnoreCase(keywordParts[1]))) { // Day/month
                        match = true; // Mark as matched
                    }
                } else if (keywordParts.length == 3) { // If all 3 parts entered
                    // If all 3 parts entered, compare complete date
                    if ((day.equalsIgnoreCase(keywordParts[0]) && month.equalsIgnoreCase(keywordParts[1]) && year.equalsIgnoreCase(keywordParts[2])) || // dd/MM/yyyy
                        (year.equalsIgnoreCase(keywordParts[0]) && month.equalsIgnoreCase(keywordParts[1]) && day.equalsIgnoreCase(keywordParts[2]))) { // yyyy/MM/dd
                        match = true; // Mark as matched
                    }
                }
                
                // Additionally, compare date string with keyword (allows searching by 01/08, 08/2025, etc.)
                String dateDisplay = day + "/" + month + "/" + year; // Create display string dd/MM/yyyy
                if (!match && dateDisplay.contains(lower)) match = true; // If display string contains keyword
                if (!match && dateStr.contains(lower)) match = true; // If original string contains keyword
            }

            // Search by date entered in dd/MM/yyyy format
            if (!match && parsedDate != null && instance.getDate() != null) { // If not matched yet and has parsed date
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Standard database format
                    Date instDate = sdf.parse(instance.getDate()); // Parse instance date
                    if (instDate != null && instDate.equals(parsedDate)) match = true; // If both dates are equal
                } catch (Exception ignored) {} // Ignore parse errors
            }

            // Search by weekday from entered date
            if (!match && weekdayFromDate != null && instance.getDate() != null && getWeekday(instance.getDate()).equals(weekdayFromDate)) match = true; // If weekday matches

            // Search by English day keywords
            if (!match && instance.getDate() != null && !lower.isEmpty()) { // If not matched yet and has date
                String weekdayOfInstance = getWeekday(instance.getDate()); // Get weekday of instance
                for (String wd : weekDays) { // Iterate through day keywords
                    if (lower.equals(wd) && (weekdayOfInstance.equals(wd) || weekdayOfInstance.startsWith(wd) || weekdayOfInstance.contains(wd))) { // If keyword matches day
                        match = true; // Mark as matched
                        break; // Break loop
                    }
                }
                // Partial match (e.g. "tue" matches "Tuesday")
                if (!match && weekdayOfInstance.toLowerCase().contains(lower)) match = true; // Partial search in day name
            }
            if (match) filtered.add(instance); // If matched, add to result list
        }
        adapter.setInstanceList(filtered); // Update adapter with filtered list
    }

    // Get day of week from date string yyyy-MM-dd
    private String getWeekday(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Standard date format
            Date date = sdf.parse(dateStr); // Parse string to Date object
            if (date != null) { // If parsing successful
                Calendar cal = Calendar.getInstance(); // Get Calendar instance
                cal.setTime(date); // Set time for Calendar
                String[] days = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"}; // Array of day names (lowercase)
                return days[cal.get(Calendar.DAY_OF_WEEK) - 1]; // Return day name (Calendar.DAY_OF_WEEK starts from 1)
            }
        } catch (Exception ignored) {} // Catch and ignore exceptions
        return ""; // Return empty string if error
    }

    @Override
    protected void onResume() {
        super.onResume(); // Call parent method
        loadAllInstances(); // Auto reload when new instances added or returning to Activity
    }
}
