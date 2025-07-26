package com.example.universalyogaapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "courses")
public class CourseEntity {
    @PrimaryKey(autoGenerate = true)
    public int localId;

    public String firebaseId; // id trên cloud, có thể null nếu chưa sync
    public String name, schedule, time, teacher, description, note, upcomingDate;
    public int capacity, duration;
    public double price;
    public boolean isSynced;

    public CourseEntity() {}

    public CourseEntity(int localId, String firebaseId, String name, String schedule, String time, String teacher, String description, String note, String upcomingDate, int capacity, int duration, double price, boolean isSynced) {
        this.localId = localId;
        this.firebaseId = firebaseId;
        this.name = name;
        this.schedule = schedule;
        this.time = time;
        this.teacher = teacher;
        this.description = description;
        this.note = note;
        this.upcomingDate = upcomingDate;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.isSynced = isSynced;
    }

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
    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
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