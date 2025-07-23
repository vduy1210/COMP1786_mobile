package com.example.universalyogaapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.universalyogaapp.dao.CourseDao;

@Database(entities = {CourseEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CourseDao courseDao();
}