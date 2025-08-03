// Adapter for RecyclerView to display list of class instances
package com.example.universalyogaapp.ui.course;

// Import necessary libraries for the Adapter
import android.view.LayoutInflater; // For inflating layouts
import android.view.View; // Basic view
import android.view.ViewGroup; // View group
import android.widget.Button; // Button widget
import android.widget.TextView; // Text display widget
import androidx.annotation.NonNull; // NonNull annotation
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for lists
import com.example.universalyogaapp.R; // Layout resources
import com.example.universalyogaapp.model.ClassInstance; // Class instance model
import java.util.List; // List interface

import java.text.ParseException; // Parse exception for dates
import java.text.SimpleDateFormat; // Date formatting
import java.util.Calendar; // Calendar
import java.util.Locale; // Localization

// Adapter for RecyclerView to display list of class instances
public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.ViewHolder> {
    
    // Interface for edit/delete class instance events
    public interface OnInstanceActionListener {
        void onEdit(ClassInstance instance); // Method called when editing instance
        void onDelete(ClassInstance instance); // Method called when deleting instance
    }

    // Declare instance variables
    private List<ClassInstance> instanceList; // List of class instances
    private final OnInstanceActionListener actionListener; // Event listener

    // Constructor to initialize adapter with list and listener
    public ClassInstanceAdapter(List<ClassInstance> instanceList, OnInstanceActionListener actionListener) {
        this.instanceList = instanceList; // Assign instance list
        this.actionListener = actionListener; // Assign action listener
    }

    // Reassign class instance list to adapter
    public void setInstanceList(List<ClassInstance> list) {
        this.instanceList = list; // Update with new list
        notifyDataSetChanged(); // Notify RecyclerView to update UI
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each class instance item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_instance, parent, false); // Create view from XML layout
        return new ViewHolder(view); // Return new ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassInstance instance = instanceList.get(position); // Get instance at current position
        holder.textViewDate.setText(formatDate(instance.getDate())); // Display formatted date
        holder.textViewNote.setText("Note: " + (instance.getNote() != null ? instance.getNote() : "")); // Display note or empty string
        holder.textViewTeacher.setText("Teacher: " + (instance.getTeacher() != null ? instance.getTeacher() : "")); // Display teacher name or empty string
        
        // Display day of week
        String dayOfWeek = getDayOfWeek(instance.getDate()); // Get day from date
        holder.textViewDayOfWeek.setText(dayOfWeek.isEmpty() ? "" : "(" + dayOfWeek + ")"); // Display day in parentheses or empty
        
        // Set up button events
        holder.buttonEdit.setOnClickListener(v -> actionListener.onEdit(instance)); // Edit button click event
        holder.buttonDelete.setOnClickListener(v -> actionListener.onDelete(instance)); // Delete button click event
    }

    // Format date from yyyy-MM-dd to dd/MM/yyyy
    private String formatDate(String dateStr) {
        try {
            java.text.SimpleDateFormat sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US); // Input format
            java.text.SimpleDateFormat sdfOutput = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US); // Output format
            java.util.Date date = sdfInput.parse(dateStr); // Parse date string to Date object
            if (date != null) { // If parsing successful
                return sdfOutput.format(date); // Return formatted date
            }
        } catch (Exception ignored) {} // Catch and ignore exceptions
        return dateStr; // Return original string if error
    }

    // Get day of week from date string yyyy-MM-dd
    private String getDayOfWeek(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Date format
            java.util.Date date = sdf.parse(dateStr); // Parse string to Date
            if (date != null) { // If parsing successful
                Calendar cal = Calendar.getInstance(); // Get Calendar instance
                cal.setTime(date); // Set time for Calendar
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Array of day names
                return days[cal.get(Calendar.DAY_OF_WEEK) - 1]; // Return day name (Calendar.DAY_OF_WEEK starts from 1)
            }
        } catch (ParseException ignored) {} // Catch and ignore ParseException
        return ""; // Return empty string if error
    }

    @Override
    public int getItemCount() {
        return instanceList != null ? instanceList.size() : 0; // Return number of instances or 0 if list is null
    }

    // ViewHolder for each class instance item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare view components
        TextView textViewDate, textViewNote, textViewTeacher, textViewDayOfWeek; // TextViews for displaying information
        Button buttonEdit, buttonDelete; // Edit and delete buttons
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView); // Call parent constructor
            
            // Initialize views from layout
            textViewDate = itemView.findViewById(R.id.textViewDate); // Find and assign date TextView
            textViewDayOfWeek = itemView.findViewById(R.id.textViewDayOfWeek); // Find and assign day of week TextView
            textViewNote = itemView.findViewById(R.id.textViewNote); // Find and assign note TextView
            textViewTeacher = itemView.findViewById(R.id.textViewTeacher); // Find and assign teacher TextView
            buttonEdit = itemView.findViewById(R.id.buttonEditInstance); // Find and assign edit button
            buttonDelete = itemView.findViewById(R.id.buttonDeleteInstance); // Find and assign delete button
        }
    }
}
