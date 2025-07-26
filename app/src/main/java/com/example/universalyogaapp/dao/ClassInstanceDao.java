package com.example.universalyogaapp.dao;

import androidx.room.*;
import com.example.universalyogaapp.db.ClassInstanceEntity;
import java.util.List;

@Dao
public interface ClassInstanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ClassInstanceEntity instance);

    @Update
    void update(ClassInstanceEntity instance);

    @Delete
    void delete(ClassInstanceEntity instance);

    @Query("SELECT * FROM class_instances WHERE courseId = :courseId")
    List<ClassInstanceEntity> getInstancesForCourse(String courseId);

    @Query("SELECT * FROM class_instances WHERE isSynced = 0")
    List<ClassInstanceEntity> getUnsyncedInstances();

    @Query("DELETE FROM class_instances WHERE firebaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    @Query("SELECT * FROM class_instances WHERE firebaseId = :firebaseId LIMIT 1")
    ClassInstanceEntity getInstanceByFirebaseId(String firebaseId);

    @Query("UPDATE class_instances SET isSynced = 1, firebaseId = :firebaseId WHERE id = :id")
    void markInstanceAsSynced(int id, String firebaseId);

    @Query("DELETE FROM class_instances")
    void deleteAllInstances();
} 