package com.example.universalyogaapp.model;

public class Course {
    private String id;
    private String name;
    private String schedule; // Class schedule (e.g. Mon, Wed, Fri - 18:00-19:00)
    private String teacher;  // Teacher
    private int capacity;
    private double price;
    private int duration;
    private String description;
    private String note;     // Additional note
    private String time;     // Class time
    private String upcomingDate; // Calculated upcoming date

    // Empty constructor for Firebase
    public Course() {}

    public Course(String id, String name, String schedule, String time, String teacher, int capacity, double price, int duration, String description, String note, String upcomingDate) {
        this.id = id;
        this.name = name;
        this.schedule = schedule;
        this.time = time;
        this.teacher = teacher;
        this.capacity = capacity;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.note = note;
        this.upcomingDate = upcomingDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getUpcomingDate() { return upcomingDate; }
    public void setUpcomingDate(String upcomingDate) { this.upcomingDate = upcomingDate; }
} 