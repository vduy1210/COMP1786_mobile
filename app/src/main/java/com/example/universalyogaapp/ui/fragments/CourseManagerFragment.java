package com.example.universalyogaapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.ui.course.ClassInstanceListActivity;
import com.example.universalyogaapp.ui.course.CourseListActivity;
import com.google.android.material.button.MaterialButton;

public class CourseManagerFragment extends Fragment {

    private MaterialButton buttonManageCourses;
    private MaterialButton buttonAllClassInstances;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        buttonManageCourses = view.findViewById(R.id.buttonManageCourses);
        buttonAllClassInstances = view.findViewById(R.id.buttonAllClassInstances);
    }

    private void setupClickListeners() {
        buttonManageCourses.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CourseListActivity.class);
            startActivity(intent);
        });
        buttonAllClassInstances.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ClassInstanceListActivity.class);
            startActivity(intent);
        });
    }
}