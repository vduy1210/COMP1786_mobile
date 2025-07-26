package com.example.universalyogaapp.model;

public class ClassInstance implements java.io.Serializable {
    private String id; // Firebase id
    private String courseId; // Firebase id của Course
    private String date;
    private String teacher;
    private String note;
    private int localId;

    public ClassInstance() {}

    public ClassInstance(String id, String courseId, String date, String teacher, String note, int localId) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.note = note;
        this.localId = localId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }
} 