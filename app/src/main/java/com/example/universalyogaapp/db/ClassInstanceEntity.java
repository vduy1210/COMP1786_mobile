package com.example.universalyogaapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "class_instances")
public class ClassInstanceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String courseId; // Liên kết tới CourseEntity (firebaseId)
    public String firebaseId; // id trên cloud, có thể null nếu chưa sync
    public String date; // Ngày học cụ thể (yyyy-MM-dd)
    public String teacher;
    public String note;
    public boolean isSynced;

    public ClassInstanceEntity() {}

    public ClassInstanceEntity(int id, String courseId, String firebaseId, String date, String teacher, String note, boolean isSynced) {
        this.id = id;
        this.courseId = courseId;
        this.firebaseId = firebaseId;
        this.date = date;
        this.teacher = teacher;
        this.note = note;
        this.isSynced = isSynced;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
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