

package com.example.universalyogaapp.model;


public class Course {
    private String id;
    private String courseId;
    private String name;
    private String schedule;
    private int capacity;
    private double price;
    private int duration;
    private String description;
    private String note;
    private String time;
    private String upcomingDate;
    private int localId;



    public Course() {}



    public Course(String id, String name, String schedule, String time, int capacity, double price, int duration, String description, String note, String upcomingDate, int localId) {
        this.id = id;
        this.name = name;
        this.schedule = schedule;
        this.time = time;
        this.capacity = capacity;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.note = note;
        this.upcomingDate = upcomingDate;
        this.localId = localId; // ID local
    }


    // Getter & Setter
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

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

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }
} 