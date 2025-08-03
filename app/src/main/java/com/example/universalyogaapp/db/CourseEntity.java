
// Entity representing 'courses' table in Room Database
package com.example.universalyogaapp.db;


import androidx.room.Entity; // Annotation to mark class as Room entity
import androidx.room.PrimaryKey; // Annotation to mark field as primary key
import androidx.room.Index;
import androidx.room.Ignore; // Annotation for Room to ignore this constructor when mapping

@Entity(tableName = "courses") // Set table name as 'courses'

// Entity class for 'courses' table, storing course information
public class CourseEntity {
    @PrimaryKey(autoGenerate = true) // localId is primary key, auto increment
    public int localId; // Auto increment ID in SQLite, unique for each record


    public String firebaseId; // ID on Firebase, null if not synced yet

    @androidx.room.ColumnInfo(name = "courseid") // Map to 'courseid' column in table
    public String courseId; // Shared ID for local & cloud, usually = localId when newly created

    // Course information
    public String name; // Course name
    public String schedule; // Schedule (e.g.: Monday,Tuesday)
    public String time; // Class time (e.g.: 08:00)
    public String description; // Course description
    public String note; // Additional notes
    public String upcomingDate; // Next class date (for display)

    public int capacity; // Maximum number of students
    public int duration; // Class duration (minutes)
    public double price; // Course price
    public boolean isSynced; // Whether synced with Firebase


    // Default constructor (required for Room)
    public CourseEntity() {}

    // Full constructor, used when creating new or mapping data
    @Ignore
    public CourseEntity(int localId, String firebaseId, String courseId, String name, String schedule, String time, String description, String note, String upcomingDate, int capacity, int duration, double price, boolean isSynced) {
        this.localId = localId; // Local ID
        this.firebaseId = firebaseId; // Firebase ID
        this.courseId = courseId; // Shared ID
        this.name = name; // Course name
        this.schedule = schedule; // Schedule
        this.time = time; // Class time
        this.description = description; // Description
        this.note = note; // Notes
        this.upcomingDate = upcomingDate; // Next class date
        this.capacity = capacity; // Capacity
        this.duration = duration; // Duration
        this.price = price; // Price
        this.isSynced = isSynced; // Sync status
    }

    // Getter & Setter for each field (required for Room and adapter)
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUpcomingDate() { return upcomingDate; }
    public void setUpcomingDate(String upcomingDate) { this.upcomingDate = upcomingDate; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}