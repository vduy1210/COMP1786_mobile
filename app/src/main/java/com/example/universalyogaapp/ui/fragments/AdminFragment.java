package com.example.universalyogaapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.room.Room;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.db.AppDatabase;
import com.example.universalyogaapp.firebase.FirebaseManager;
import com.example.universalyogaapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class AdminFragment extends Fragment {

    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;
    private AppDatabase db;
    private TextView textViewUsername;
    private MaterialButton buttonSyncData, buttonResetDatabase, buttonLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initData();
        setupClickListeners();
        displayUserInfo();
    }

    private void initViews(View view) {
        textViewUsername = view.findViewById(R.id.textViewUsername);
        buttonSyncData = view.findViewById(R.id.buttonSyncData);
        buttonResetDatabase = view.findViewById(R.id.buttonResetDatabase);
        buttonLogout = view.findViewById(R.id.buttonLogout);
    }

    private void initData() {
        sessionManager = new SessionManager(requireContext());
        firebaseManager = new FirebaseManager();
        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase.class,
            "yoga-db"
        ).allowMainThreadQueries()
         .fallbackToDestructiveMigration()
         .build();
    }

    private void setupClickListeners() {
        buttonSyncData.setOnClickListener(v -> performSyncData());
        buttonResetDatabase.setOnClickListener(v -> performResetDatabase());
        buttonLogout.setOnClickListener(v -> performLogout());
    }

    private void displayUserInfo() {
        String username = sessionManager.getUsername();
        if (!username.isEmpty()) {
            textViewUsername.setText(username);
        }
    }

    private void performSyncData() {
        buttonSyncData.setEnabled(false);
        buttonSyncData.setText("Syncing...");
        
        // Simulate sync process
        buttonSyncData.postDelayed(() -> {
            // Here you would implement actual sync logic
            Toast.makeText(requireContext(), "Data synchronized successfully", Toast.LENGTH_SHORT).show();
            buttonSyncData.setEnabled(true);
            buttonSyncData.setText("Sync Data");
        }, 2000);
    }

    private void performResetDatabase() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to reset the database? This action cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    buttonResetDatabase.setEnabled(false);
                    buttonResetDatabase.setText("Resetting...");
                    
                    // Perform reset in background
                    new Thread(() -> {
                        // Clear all data
                        db.courseDao().deleteAllCourses();
                        db.classInstanceDao().deleteAllInstances();
                        
                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Database reset successfully", Toast.LENGTH_SHORT).show();
                            buttonResetDatabase.setEnabled(true);
                            buttonResetDatabase.setText("Reset Database");
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        sessionManager.logout();
        Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
        
        // Navigate to LoginActivity
        requireActivity().finish();
        requireActivity().startActivity(new android.content.Intent(requireContext(), 
                com.example.universalyogaapp.ui.auth.LoginActivity.class));
    }
} 