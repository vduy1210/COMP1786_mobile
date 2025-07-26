package com.example.universalyogaapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.universalyogaapp.dao.CourseDao;
import com.example.universalyogaapp.dao.ClassInstanceDao;
import com.example.universalyogaapp.db.ClassInstanceEntity;

@Database(entities = {CourseEntity.class, ClassInstanceEntity.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CourseDao courseDao();
    public abstract ClassInstanceDao classInstanceDao();
}