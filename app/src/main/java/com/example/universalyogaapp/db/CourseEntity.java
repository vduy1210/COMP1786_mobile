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
}