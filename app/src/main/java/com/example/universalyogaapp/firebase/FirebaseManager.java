package com.example.universalyogaapp.firebase;

import androidx.annotation.NonNull;

import com.example.universalyogaapp.model.Course;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {
    private final String DATABASE_URL = "https://universalyogaapps-fb6fa-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseDatabase database;
    private final DatabaseReference courseRef;

    public FirebaseManager() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
        courseRef = database.getReference("courses");
    }

    // Thêm Course
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

    // Cập nhật Course
    public void updateCourse(Course course, DatabaseReference.CompletionListener listener) {
        if (course.getId() == null) return;
        courseRef.child(course.getId()).setValue(course, listener);
    }

    // Xóa Course
    public void deleteCourse(String courseId, DatabaseReference.CompletionListener listener) {
        courseRef.child(courseId).removeValue(listener);
    }
} 