package com.example.universalyogaapp.firebase;

import androidx.annotation.NonNull;

import com.example.universalyogaapp.model.Course;
import com.example.universalyogaapp.model.ClassInstance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {
    private final String DATABASE_URL = "https://universalyogaapps-fb6fa-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseDatabase database;
    private final DatabaseReference courseRef;
    private final DatabaseReference classInstanceRef;

    public FirebaseManager() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
        courseRef = database.getReference("courses");
        classInstanceRef = database.getReference("class_instances");
    }

    // Add Course
    public void addCourse(Course course, DatabaseReference.CompletionListener listener) {
        String id = courseRef.push().getKey();
        course.setId(id);
        courseRef.child(id).setValue(course, listener);
    }

    // Lấy danh sách Course
    public void getCourses(ValueEventListener listener) {
        courseRef.addValueEventListener(listener);
    }

    // Lấy 1 Course theo id
    public void getCourseById(String courseId, ValueEventListener listener) {
        courseRef.child(courseId).addListenerForSingleValueEvent(listener);
    }

    // Update Course
    public void updateCourse(Course course, DatabaseReference.CompletionListener listener) {
        if (course.getId() == null) return;
        courseRef.child(course.getId()).setValue(course, listener);
    }

    // Delete Course
    public void deleteCourse(String courseId, DatabaseReference.CompletionListener listener) {
        courseRef.child(courseId).removeValue(listener);
    }

    // Add ClassInstance
    public void addClassInstance(ClassInstance instance, DatabaseReference.CompletionListener listener) {
        String id = classInstanceRef.push().getKey();
        instance.setId(id);
        classInstanceRef.child(id).setValue(instance, listener);
    }

    // Lấy danh sách ClassInstance theo courseId
    public void getClassInstancesByCourseId(String courseId, ValueEventListener listener) {
        classInstanceRef.orderByChild("courseId").equalTo(courseId).addValueEventListener(listener);
    }

    // Update ClassInstance
    public void updateClassInstance(ClassInstance instance, DatabaseReference.CompletionListener listener) {
        if (instance.getId() == null) return;
        classInstanceRef.child(instance.getId()).setValue(instance, listener);
    }

    // Delete ClassInstance
    public void deleteClassInstance(String instanceId, DatabaseReference.CompletionListener listener) {
        classInstanceRef.child(instanceId).removeValue(listener);
    }

    // Delete all ClassInstances by courseId
    public void deleteClassInstancesByCourseId(String courseId, DatabaseReference.CompletionListener listener) {
        classInstanceRef.orderByChild("courseId").equalTo(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    child.getRef().removeValue();
                }
                if (listener != null) listener.onComplete(null, classInstanceRef);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) listener.onComplete(error, classInstanceRef);
            }
        });
    }
} 