// Activity for searching class instances with different criteria
package com.example.universalyogaapp.ui.course;

// Import necessary libraries for search Activity
import android.os.Bundle; // Bundle for passing data
import android.text.Editable; // Interface for editable text
import android.text.TextWatcher; // Listener for text changes
import android.view.View; // Basic view
import android.widget.EditText; // Text input widget
import android.widget.Toast; // Display short messages
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

// Dedicated Activity for searching class instances
public class ClassInstanceSearchActivity extends AppCompatActivity {
    // Declare UI component variables
    private EditText editTextSearch; // Search keyword input field
    private RecyclerView recyclerViewResults; // RecyclerView to display search results
    private ClassInstanceAdapter adapter; // Adapter for RecyclerView
    
    // Declare data variables
    private AppDatabase db; // Room database instance
    private List<ClassInstance> allInstances = new ArrayList<>(); // List of all instances for searching

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call parent method
        setContentView(R.layout.activity_class_instance_search); // Set layout for Activity
        
        // Initialize views from layout
        editTextSearch = findViewById(R.id.editTextSearch); // Find and assign search input field
        recyclerViewResults = findViewById(R.id.recyclerViewResults); // Find and assign results RecyclerView
        
        // Set up layout manager for RecyclerView
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this)); // Use vertical LinearLayoutManager
        
        // Initialize adapter with empty list and listener
        adapter = new ClassInstanceAdapter(new ArrayList<>(), new ClassInstanceAdapter.OnInstanceActionListener() { // Create adapter with listener
            @Override
            public void onEdit(ClassInstance instance) { // Method called when editing instance
                // Optional: show details or edit in search context
            }
            @Override
            public void onDelete(ClassInstance instance) { // Method called when deleting instance
                // Optional: do nothing or show dialog in search context
            }
        });
        recyclerViewResults.setAdapter(adapter); // Set adapter for RecyclerView
        
        // Initialize database and load data
        db = AppDatabase.getInstance(getApplicationContext()); // Get database instance
        List<ClassInstanceEntity> entityList = db.classInstanceDao().getAllClassInstances(); // Get all entities from database
        allInstances = new ArrayList<>(); // Initialize new list
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
        adapter.setInstanceList(allInstances); // Display all instances initially
        
        // Set up text change listener for search input
        editTextSearch.addTextChangedListener(new TextWatcher() { // Add TextWatcher
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Before text changes (no processing)
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // When text is changing
                filterResults(s.toString()); // Filter results by new keyword
            }
            
            @Override
            public void afterTextChanged(Editable s) {} // After text changes (no processing)
        });
    }

    // Method to filter search results by keyword
    private void filterResults(String keyword) {
        String lower = keyword.toLowerCase(); // Convert keyword to lowercase
        List<ClassInstance> filtered = new ArrayList<>(); // Filtered results list
        
        // Initialize SimpleDateFormat for date parsing
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Standard date format
        String weekday = null; // Variable to store weekday
        
        // Try parsing keyword as date to search by weekday
        try {
            Date date = sdf.parse(keyword); // Parse keyword to Date object
            if (date != null) { // If parsing successful
                Calendar cal = Calendar.getInstance(); // Get Calendar instance
                cal.setTime(date); // Set time for Calendar
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Array of day names
                weekday = days[cal.get(Calendar.DAY_OF_WEEK) - 1].toLowerCase(); // Get day name and convert to lowercase
            }
        } catch (ParseException ignored) {} // Ignore parse errors
        
        // Iterate through all instances for searching
        for (ClassInstance instance : allInstances) { // Iterate through each instance
            boolean match = false; // Variable to mark if there's a match
            
            // Search by teacher name
            if (instance.getTeacher() != null && !lower.isEmpty() && instance.getTeacher().toLowerCase().contains(lower)) match = true; // If teacher name contains keyword
            
            // Search by exact date
            if (!match && instance.getDate() != null && instance.getDate().equals(keyword)) match = true; // If date matches exactly
            
            // Search by weekday (from parsed date)
            if (!match && weekday != null && instance.getDate() != null && getWeekday(instance.getDate()).equals(weekday)) match = true; // If weekday matches
            
            if (match) filtered.add(instance); // If matched, add to result list
        }
        adapter.setInstanceList(filtered); // Update adapter with filtered list
    }

    // Helper method to get weekday from date string
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
}
