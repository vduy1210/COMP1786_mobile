
// Entity representing 'class_instances' table in Room Database
package com.example.universalyogaapp.db;


import androidx.room.Entity; // Annotation to mark class as Room entity
import androidx.room.PrimaryKey; // Annotation to mark field as primary key
import androidx.room.Index;
import androidx.room.Ignore; // Annotation for Room to ignore this constructor when mapping


@Entity(tableName = "class_instances") // Set table name as 'class_instances'
public class ClassInstanceEntity {
    @PrimaryKey(autoGenerate = true) // id is primary key, auto increment
    public int id; // Auto increment ID in SQLite, unique for each record

    public String courseId; // Firebase ID of the course (for display and sync)
    public int courseLocalId; // Local ID of the course (for internal processing)
    public String firebaseId; // ID on Firebase, null if not synced yet
    public String date; // Specific class date (format yyyy-MM-dd)
    public String teacher; // Teacher name
    public String note; // Additional notes for the class
    public boolean isSynced; // Whether synced with Firebase

    // Default constructor (required for Room)
    public ClassInstanceEntity() {}

    // Full constructor, used when creating new or mapping data
    @Ignore
    public ClassInstanceEntity(int id, String courseId, int courseLocalId, String firebaseId, String date, String teacher, String note, boolean isSynced) {
        this.id = id; // Local ID
        this.courseId = courseId; // Firebase course ID
        this.courseLocalId = courseLocalId; // Local course ID
        this.firebaseId = firebaseId; // Firebase ID
        this.date = date; // Class date
        this.teacher = teacher; // Teacher
        this.note = note; // Notes
        this.isSynced = isSynced; // Sync status
    }

    // Getter & Setter for each field (required for Room and adapter)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getCourseLocalId() { return courseLocalId; }
    public void setCourseLocalId(int courseLocalId) { this.courseLocalId = courseLocalId; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}