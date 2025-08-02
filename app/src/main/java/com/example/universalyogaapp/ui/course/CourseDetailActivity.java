
// Activity hiển thị chi tiết khoá học, danh sách buổi học, cho phép sửa/xoá khoá học và buổi học
// Activity to display course details, class instance list, allowing edit/delete course and class instances
package com.example.universalyogaapp.ui.course;

// Import các thư viện cần thiết cho Activity này
// Import necessary libraries for this Activity
import android.content.DialogInterface; // Interface cho dialog interactions - Interface for dialog interactions
import android.content.Intent; // Intent để chuyển Activity - Intent for Activity navigation
import android.os.Bundle; // Bundle để truyền dữ liệu - Bundle for passing data
import android.view.View; // View cơ bản - Basic view
import android.widget.Button; // Widget nút bấm - Button widget
import android.widget.TextView; // Widget hiển thị text - Text display widget
import android.widget.Toast; // Hiển thị thông báo ngắn - Display short messages

import androidx.annotation.NonNull; // Annotation cho giá trị không null - NonNull annotation
import androidx.annotation.Nullable; // Annotation cho giá trị có thể null - Nullable annotation
import androidx.appcompat.app.AlertDialog; // Dialog cảnh báo - Alert dialog
import androidx.appcompat.app.AppCompatActivity; // Activity cơ bản của AppCompat - Base AppCompat activity

import com.example.universalyogaapp.R; // Resource layout - Layout resources
import com.example.universalyogaapp.firebase.FirebaseManager; // Quản lý đồng bộ Firebase - Firebase sync manager
import com.example.universalyogaapp.model.Course; // Model khoá học - Course model
import com.example.universalyogaapp.utils.DateUtils; // Tiện ích xử lý ngày tháng - Date utility functions
import com.google.firebase.database.DataSnapshot; // Snapshot dữ liệu Firebase - Firebase data snapshot
import com.google.firebase.database.DatabaseError; // Lỗi Firebase database - Firebase database error
import com.google.firebase.database.DatabaseReference; // Tham chiếu Firebase database - Firebase database reference
import com.google.firebase.database.ValueEventListener; // Listener cho sự kiện thay đổi giá trị - Value change event listener

import java.util.Locale; // Địa phương hoá - Localization
import androidx.room.Room; // Room database builder - Room database builder
import com.example.universalyogaapp.db.AppDatabase; // Database ứng dụng - Application database
import com.example.universalyogaapp.dao.CourseDao; // DAO cho khoá học - DAO for courses
import com.example.universalyogaapp.db.CourseEntity; // Entity khoá học - Course entity
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager cho RecyclerView - Layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // RecyclerView cho danh sách - RecyclerView for lists
import com.example.universalyogaapp.model.ClassInstance; // Model buổi học - Class instance model
import java.util.ArrayList; // Danh sách ArrayList - ArrayList collection
import java.util.List; // Interface danh sách - List interface
import com.example.universalyogaapp.ui.course.ClassInstanceAdapter; // Adapter cho buổi học - Adapter for class instances
import com.example.universalyogaapp.dao.ClassInstanceDao; // DAO cho buổi học - DAO for class instances
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity buổi học - Class instance entity
import java.util.Map; // Interface Map - Map interface
import java.util.HashMap; // HashMap implementation - HashMap implementation
import android.net.ConnectivityManager; // Quản lý kết nối mạng - Network connectivity manager
import android.net.NetworkInfo; // Thông tin mạng - Network information
import android.net.NetworkCapabilities; // Khả năng mạng - Network capabilities
import android.os.Build; // Thông tin build Android - Android build information

// Activity hiển thị chi tiết khoá học, danh sách buổi học, cho phép sửa/xoá khoá học và buổi học
// Activity to display course details, class instance list, allowing edit/delete course and class instances
public class CourseDetailActivity extends AppCompatActivity {
    // Khai báo các biến UI components cho thông tin khoá học - Declare UI component variables for course information
    private TextView textViewName, textViewSchedule, textViewTime, textViewCapacity, textViewPrice, textViewDuration, textViewDescription, textViewNote; // Các TextView hiển thị thông tin khoá học - TextViews to display course information
    private Button buttonEdit, buttonDelete; // Nút sửa/xoá khoá học - Buttons for edit/delete course
    
    // Khai báo các biến dữ liệu và xử lý - Declare data and processing variables
    private Course course; // Đối tượng khoá học đang xem - Course object being viewed
    private FirebaseManager firebaseManager; // Quản lý đồng bộ Firebase - Firebase sync manager
    private String courseId; // ID khoá học trên Firebase - Course ID on Firebase
    private AppDatabase db; // Room database instance - Room database instance
    
    // Khai báo các biến cho danh sách buổi học - Declare variables for class instance list
    private RecyclerView recyclerViewClassInstances; // RecyclerView hiển thị danh sách buổi học - RecyclerView to display class instance list
    private ClassInstanceAdapter classInstanceAdapter; // Adapter cho danh sách buổi học - Adapter for class instance list
    private Button buttonAddClassInstance; // Nút thêm buổi học - Button to add class instance
    private List<ClassInstance> classInstanceList = new ArrayList<>(); // Danh sách buổi học - List of class instances
    private String courseSchedule; // Lưu lịch khoá học để truyền sang màn thêm/sửa buổi học - Store course schedule to pass to add/edit class instance screen

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Gọi phương thức cha - Call parent method
        setContentView(R.layout.activity_course_detail); // Gán layout cho Activity - Set layout for Activity

        // Khởi tạo database Room với migration - Initialize Room database with migration
        db = Room.databaseBuilder(
                getApplicationContext(), // Context ứng dụng - Application context
                AppDatabase.class, // Class database - Database class
                "yoga-db" // Tên database - Database name
            ).allowMainThreadQueries() // Cho phép query trên main thread - Allow main thread queries
            .addMigrations(AppDatabase.MIGRATION_5_6) // Thêm migration từ version 5 lên 6 - Add migration from version 5 to 6
            .build(); // Xây dựng database - Build database

        // Khởi tạo các view từ layout - Initialize views from layout
        textViewName = findViewById(R.id.textViewName); // Tìm và gán TextView tên khoá học - Find and assign course name TextView
        textViewSchedule = findViewById(R.id.textViewSchedule); // Tìm và gán TextView lịch học - Find and assign schedule TextView
        textViewTime = findViewById(R.id.textViewTime); // Tìm và gán TextView giờ học - Find and assign time TextView
        textViewCapacity = findViewById(R.id.textViewCapacity); // Tìm và gán TextView sức chứa - Find and assign capacity TextView
        textViewPrice = findViewById(R.id.textViewPrice); // Tìm và gán TextView giá - Find and assign price TextView
        textViewDuration = findViewById(R.id.textViewDuration); // Tìm và gán TextView thời lượng - Find and assign duration TextView
        textViewDescription = findViewById(R.id.textViewDescription); // Tìm và gán TextView mô tả - Find and assign description TextView
        textViewNote = findViewById(R.id.textViewNote); // Tìm và gán TextView ghi chú - Find and assign note TextView
        buttonEdit = findViewById(R.id.buttonEdit); // Tìm và gán nút sửa - Find and assign edit button
        buttonDelete = findViewById(R.id.buttonDelete); // Tìm và gán nút xoá - Find and assign delete button
        recyclerViewClassInstances = findViewById(R.id.recyclerViewClassInstances); // Tìm và gán RecyclerView buổi học - Find and assign class instances RecyclerView
        buttonAddClassInstance = findViewById(R.id.buttonAddClassInstance); // Tìm và gán nút thêm buổi học - Find and assign add class instance button

        firebaseManager = new FirebaseManager(); // Khởi tạo quản lý Firebase - Initialize Firebase manager

        // Khởi tạo adapter cho danh sách buổi học - Initialize adapter for class instance list
        classInstanceAdapter = new ClassInstanceAdapter(classInstanceList, new ClassInstanceAdapter.OnInstanceActionListener() { // Tạo adapter với listener - Create adapter with listener
            @Override
            public void onEdit(ClassInstance instance) { // Khi nhấn sửa buổi học - When edit instance is pressed
                // Mở màn hình sửa buổi học - Open edit instance screen
                Intent intent = new Intent(CourseDetailActivity.this, AddEditClassInstanceActivity.class); // Tạo Intent - Create Intent
                intent.putExtra("course_id", courseId); // Truyền ID khoá học - Pass course ID
                intent.putExtra("course_schedule", courseSchedule); // Truyền lịch khoá học - Pass course schedule
                intent.putExtra("class_instance", instance); // Truyền buổi học để sửa - Pass instance to edit
                startActivity(intent); // Khởi chạy Activity - Start Activity
            }
            @Override
            public void onDelete(ClassInstance instance) { // Khi nhấn xoá buổi học - When delete instance is pressed
                confirmDeleteInstance(instance); // Hiện dialog xác nhận xoá buổi học - Show delete instance confirmation dialog
            }
        });
        recyclerViewClassInstances.setLayoutManager(new LinearLayoutManager(this)); // Gán layout manager dạng danh sách - Set linear layout manager
        recyclerViewClassInstances.setAdapter(classInstanceAdapter); // Gán adapter cho RecyclerView - Set adapter for RecyclerView

        // Lấy courseId từ Intent để load dữ liệu - Get courseId from Intent to load data
        courseId = getIntent().getStringExtra("course_id"); // Đọc courseId từ extra - Read courseId from extra
        if (courseId != null) { // Nếu có courseId - If courseId exists
            loadCourse(courseId); // Tải thông tin khoá học - Load course information
        }

        // Sự kiện sửa khoá học - Edit course event
        buttonEdit.setOnClickListener(new View.OnClickListener() { // Listener cho nút sửa - Listener for edit button
            @Override
            public void onClick(View v) { // Khi nhấn nút sửa - When edit button is pressed
                // Mở màn hình sửa khoá học - Open edit course screen
                Intent intent = new Intent(CourseDetailActivity.this, AddEditCourseActivity.class); // Tạo Intent - Create Intent
                intent.putExtra("course_id", courseId); // Truyền ID khoá học - Pass course ID
                startActivity(intent); // Khởi chạy Activity - Start Activity
            }
        });

        // Sự kiện xoá khoá học - Delete course event
        buttonDelete.setOnClickListener(new View.OnClickListener() { // Listener cho nút xoá - Listener for delete button
            @Override
            public void onClick(View v) { // Khi nhấn nút xoá - When delete button is pressed
                confirmDelete(); // Hiện dialog xác nhận xoá - Show delete confirmation dialog
            }
        });

        // Sự kiện thêm buổi học - Add class instance event
        buttonAddClassInstance.setOnClickListener(new View.OnClickListener() { // Listener cho nút thêm buổi học - Listener for add instance button
            @Override
            public void onClick(View v) { // Khi nhấn nút thêm - When add button is pressed
                // Mở màn hình thêm buổi học mới - Open add new instance screen
                Intent intent = new Intent(CourseDetailActivity.this, AddEditClassInstanceActivity.class); // Tạo Intent - Create Intent
                intent.putExtra("course_id", courseId); // Truyền ID khoá học - Pass course ID
                intent.putExtra("course_schedule", courseSchedule); // Truyền lịch khoá học - Pass course schedule
                startActivity(intent); // Khởi chạy Activity - Start Activity
            }
        });
    }

    @Override
    protected void onResume() { // Phương thức được gọi khi Activity quay lại tiền cảnh - Method called when Activity returns to foreground
        super.onResume(); // Gọi phương thức cha - Call parent method
        // Khi quay lại màn hình, reload danh sách buổi học - When returning to screen, reload class instance list
        if (classInstanceAdapter != null) { // Nếu adapter đã được khởi tạo - If adapter is initialized
            if (course != null) { // Nếu có đối tượng khoá học - If course object exists
                loadClassInstances(course.getId()); // Tải lại danh sách buổi học theo ID khoá học - Reload class instances by course ID
            } else if (courseId != null) { // Nếu có courseId - If courseId exists
                loadClassInstances(courseId); // Tải lại danh sách buổi học theo courseId - Reload class instances by courseId
            }
        }
    }

    // Load dữ liệu khoá học từ local, nếu không có thì lấy từ Firebase
    // Load course data from local database, if not available then get from Firebase
    private void loadCourse(String id) { // Phương thức tải thông tin khoá học - Method to load course information
        // Thử lấy từ database local trước - Try to get from local database first
        CourseEntity localCourse = db.courseDao().getCourseByFirebaseId(id); // Truy vấn khoá học từ Room database - Query course from Room database

        if (localCourse != null) { // Nếu có local - If local data exists
            // Nếu có local, hiển thị thông tin - If local exists, display information
            Course course = new Course( // Tạo đối tượng Course từ entity - Create Course object from entity
                localCourse.firebaseId, // ID Firebase - Firebase ID
                localCourse.name, // Tên khoá học - Course name
                localCourse.schedule, // Lịch học - Schedule
                localCourse.time, // Giờ học - Time
                localCourse.capacity, // Sức chứa - Capacity
                localCourse.price, // Giá - Price
                localCourse.duration, // Thời lượng - Duration
                localCourse.description, // Mô tả - Description
                localCourse.note, // Ghi chú - Note
                localCourse.upcomingDate, // Ngày sắp tới - Upcoming date
                localCourse.localId // ID local - Local ID
            );
            course.setId(localCourse.firebaseId); // Gán Firebase ID - Set Firebase ID
            course.setLocalId(localCourse.localId); // Gán local ID - Set local ID
            showCourseInfo(course); // Hiển thị thông tin khoá học - Display course information
            loadClassInstances(course.getId()); // Tải danh sách buổi học - Load class instances
        } else {
            // Nếu không có local, lấy từ Firebase - If no local data, get from Firebase
            firebaseManager.getCourseById(id, new ValueEventListener() { // Lắng nghe dữ liệu từ Firebase - Listen for data from Firebase
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { // Khi dữ liệu thay đổi - When data changes
                    Integer capacityObj = snapshot.child("capacity").getValue(Integer.class); // Lấy giá trị capacity - Get capacity value
                    int capacity = capacityObj != null ? capacityObj : 0; // Xử lý null safety - Handle null safety
                    Double priceObj = snapshot.child("price").getValue(Double.class); // Lấy giá trị price - Get price value
                    double price = priceObj != null ? priceObj : 0.0; // Xử lý null safety cho price - Handle null safety for price
                    Integer durationObj = snapshot.child("duration").getValue(Integer.class); // Lấy giá trị duration - Get duration value
                    int duration = durationObj != null ? durationObj : 0; // Xử lý null safety cho duration - Handle null safety for duration
                    com.example.universalyogaapp.model.Course course = new com.example.universalyogaapp.model.Course( // Tạo đối tượng Course từ Firebase - Create Course object from Firebase
                        snapshot.getKey(), // ID từ key của snapshot - ID from snapshot key
                        snapshot.child("name").getValue(String.class), // Tên khoá học - Course name
                        snapshot.child("schedule").getValue(String.class), // Lịch học - Schedule
                        snapshot.child("time").getValue(String.class), // Giờ học - Time
                        capacity, // Sức chứa - Capacity
                        price, // Giá - Price
                        duration, // Thời lượng - Duration
                        snapshot.child("description").getValue(String.class), // Mô tả - Description
                        snapshot.child("note").getValue(String.class), // Ghi chú - Note
                        snapshot.child("upcomingDate").getValue(String.class), // Ngày sắp tới - Upcoming date
                        0 // Khi lấy từ Firebase, localId không tồn tại, gán 0 - When getting from Firebase, localId doesn't exist, assign 0
                    );
                    if (course != null) { // Nếu course không null - If course is not null
                        course.setId(snapshot.getKey()); // Gán ID từ key - Set ID from key
                        course.setLocalId(0); // Khi lấy từ Firebase, localId không tồn tại, gán 0 - When getting from Firebase, localId doesn't exist, assign 0
                        showCourseInfo(course); // Hiển thị thông tin khoá học - Display course information
                        loadClassInstances(course.getId()); // Tải danh sách buổi học - Load class instances
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { // Khi có lỗi kết nối Firebase - When Firebase connection error occurs
                    Toast.makeText(CourseDetailActivity.this, "Error loading data", Toast.LENGTH_SHORT).show(); // Hiện thông báo lỗi - Show error message
                }
            });
        }
    }

    // Load danh sách buổi học của khoá học từ local, nếu có mạng thì merge thêm từ Firebase (chỉ hiển thị, không lưu local)
    // Load class instances of course from local, if network available then merge additional from Firebase (display only, don't save locally)
    private void loadClassInstances(String courseId) { // Phương thức tải danh sách buổi học - Method to load class instances
        if (classInstanceAdapter == null) { // Nếu adapter chưa khởi tạo - If adapter not initialized
            // Adapter chưa khởi tạo, bỏ qua - Adapter not initialized, skip
            return; // Thoát khỏi phương thức - Exit method
        }

        classInstanceList.clear(); // Xoá danh sách cũ - Clear old list

        // Lấy từ database local trước - Get from local database first
        List<com.example.universalyogaapp.db.ClassInstanceEntity> localEntities = db.classInstanceDao().getInstancesForCourse(courseId); // Truy vấn danh sách buổi học từ Room - Query class instances from Room

        // Chuyển entity local sang ClassInstance - Convert local entity to ClassInstance
        for (ClassInstanceEntity entity : localEntities) { // Duyệt qua từng entity - Iterate through each entity
            ClassInstance instance = new ClassInstance( // Tạo đối tượng ClassInstance - Create ClassInstance object
                entity.firebaseId, // Firebase ID - Firebase ID
                entity.courseId, // ID khoá học - Course ID
                entity.date, // Ngày học - Class date
                entity.teacher, // Giáo viên - Teacher
                entity.note, // Ghi chú - Comment
                entity.id // ID local - Local ID
            );
            classInstanceList.add(instance); // Thêm vào danh sách - Add to list
        }

        // Hiển thị dữ liệu local trước - Display local data first
        classInstanceAdapter.setInstanceList(new ArrayList<>(classInstanceList)); // Cập nhật adapter với dữ liệu local - Update adapter with local data

        // Nếu có mạng, lấy thêm từ Firebase để hiển thị (không lưu local) - If network available, get additional from Firebase for display (don't save locally)
        if (isNetworkAvailable()) { // Kiểm tra kết nối mạng - Check network connection
            firebaseManager.getClassInstancesByCourseId(courseId, new ValueEventListener() { // Lắng nghe dữ liệu từ Firebase - Listen for data from Firebase
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { // Khi dữ liệu thay đổi - When data changes
                    // Kiểm tra adapter còn tồn tại không - Check if adapter still exists
                    if (classInstanceAdapter == null) { // Nếu adapter null - If adapter is null
                        return; // Thoát khỏi phương thức - Exit method
                    }

                    // Tạo map các instance đã có theo firebaseId - Create map of existing instances by firebaseId
                    Map<String, ClassInstance> existingInstanceMap = new HashMap<>(); // Map lưu các instance đã có - Map to store existing instances
                    for (ClassInstance instance : classInstanceList) { // Duyệt qua danh sách hiện tại - Iterate through current list
                        if (instance.getId() != null) { // Nếu có ID - If has ID
                            existingInstanceMap.put(instance.getId(), instance); // Thêm vào map - Add to map
                        }
                    }

                    // Xử lý các instance từ Firebase chỉ để hiển thị - Process instances from Firebase for display only
                    for (DataSnapshot child : snapshot.getChildren()) { // Duyệt qua từng child snapshot - Iterate through each child snapshot
                        ClassInstance firebaseInstance = child.getValue(ClassInstance.class); // Chuyển đổi thành đối tượng ClassInstance - Convert to ClassInstance object
                        if (firebaseInstance != null) { // Nếu instance không null - If instance is not null
                            firebaseInstance.setId(child.getKey()); // Gán ID từ key - Set ID from key

                            // Nếu chưa có trong danh sách hiển thị - If not yet in display list
                            if (!existingInstanceMap.containsKey(child.getKey())) { // Kiểm tra trùng ID - Check for duplicate ID
                                // Kiểm tra trùng ngày trong danh sách hiển thị - Check for duplicate date in display list
                                boolean isDuplicate = false; // Flag kiểm tra trùng lặp - Duplicate check flag
                                for (ClassInstance existingInstance : classInstanceList) { // Duyệt qua danh sách hiện có - Iterate through existing list
                                    if (existingInstance.getDate().equals(firebaseInstance.getDate())) { // So sánh ngày - Compare dates
                                        isDuplicate = true; // Đánh dấu trùng lặp - Mark as duplicate
                                        break; // Thoát khỏi vòng lặp - Break loop
                                    }
                                }

                                if (!isDuplicate) { // Nếu không trùng lặp - If not duplicate
                                    // Instance mới từ Firebase, chỉ thêm vào danh sách hiển thị - New instance from Firebase, only add to display list
                                    classInstanceList.add(firebaseInstance); // Thêm vào danh sách - Add to list
                                }
                            }
                        }
                    }

                    // Hiển thị dữ liệu đã merge - Display merged data
                    if (classInstanceAdapter != null) { // Nếu adapter vẫn tồn tại - If adapter still exists
                        classInstanceAdapter.setInstanceList(new ArrayList<>(classInstanceList)); // Cập nhật adapter với dữ liệu đã merge - Update adapter with merged data
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { // Khi có lỗi Firebase - When Firebase error occurs
                    // Nếu Firebase lỗi, vẫn hiển thị dữ liệu local - If Firebase error, still display local data
                    Toast.makeText(CourseDetailActivity.this, "Failed to load from cloud", Toast.LENGTH_SHORT).show(); // Hiện thông báo lỗi - Show error message
                }
            });
        }
    }

    // Kiểm tra kết nối mạng - Check network connection
    private boolean isNetworkAvailable() { // Phương thức kiểm tra mạng - Method to check network
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Lấy service quản lý kết nối - Get connectivity manager service
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Lấy thông tin mạng hiện tại - Get current network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Trả về true nếu có mạng - Return true if network available
    }

    // Hiển thị thông tin khoá học lên giao diện - Display course information on UI
    private void showCourseInfo(Course course) { // Phương thức hiển thị thông tin khoá học - Method to display course information
        this.course = course; // Lưu đối tượng course - Store course object
        textViewName.setText(course.getName()); // Hiển thị tên khoá học - Display course name
        textViewDescription.setText(course.getDescription()); // Hiển thị mô tả - Display description
        textViewSchedule.setText(DateUtils.getNextUpcomingDate(course.getSchedule())); // Hiển thị ngày sắp tới - Display next upcoming date
        textViewTime.setText(course.getTime() != null ? course.getTime() : "Not set"); // Hiển thị giờ học hoặc "Not set" - Display time or "Not set"
        textViewCapacity.setText(String.format(Locale.getDefault(), "%d Students", course.getCapacity())); // Hiển thị sức chứa - Display capacity
        textViewPrice.setText(String.format(Locale.US, "$%.2f", course.getPrice())); // Hiển thị giá với định dạng tiền tệ - Display price with currency format
        textViewDuration.setText(String.format(Locale.getDefault(), "%d min", course.getDuration())); // Hiển thị thời lượng - Display duration

        if (course.getNote() != null && !course.getNote().isEmpty()) { // Nếu có ghi chú - If note exists
            textViewNote.setText(course.getNote()); // Hiển thị ghi chú - Display note
            textViewNote.setVisibility(View.VISIBLE); // Hiện TextView ghi chú - Show note TextView
        } else {
            textViewNote.setVisibility(View.GONE); // Ẩn TextView ghi chú - Hide note TextView
        }
        // Lưu lịch để truyền sang màn thêm/sửa buổi học - Save schedule to pass to add/edit class instance screen
        courseSchedule = course.getSchedule(); // Lưu lịch khoá học - Store course schedule
    }

    // Hiển thị dialog xác nhận xoá khoá học - Show course deletion confirmation dialog
    private void confirmDelete() { // Phương thức xác nhận xoá - Method to confirm deletion
        new AlertDialog.Builder(this) // Tạo AlertDialog builder - Create AlertDialog builder
                .setTitle("Delete Course") // Đặt tiêu đề - Set title
                .setMessage("Are you sure you want to delete this course? This will also delete all related class sessions.") // Đặt nội dung thông báo - Set message content
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() { // Nút xác nhận xoá - Delete confirmation button
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // Khi nhấn nút Delete - When Delete button is pressed
                        deleteCourse(); // Gọi phương thức xoá khoá học - Call delete course method
                    }
                })
                .setNegativeButton("Cancel", null) // Nút hủy bỏ - Cancel button
                .show(); // Hiển thị dialog - Show dialog
    }

    // Xoá khoá học: xoá các buổi học trên Firebase trước, sau đó xoá khoá học
    // Delete course: delete class instances on Firebase first, then delete course
    private void deleteCourse() { // Phương thức xoá khoá học - Method to delete course
        if (courseId == null) return; // Nếu không có courseId thì thoát - If no courseId then exit

        // Xoá các buổi học trên Firebase trước - Delete class instances on Firebase first
        firebaseManager.deleteClassInstancesByCourseId(courseId, new DatabaseReference.CompletionListener() { // Xoá buổi học trên Firebase - Delete instances on Firebase
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) { // Khi hoàn thành xoá buổi học - When instance deletion completes
                // Sau khi xoá buổi học, xoá khoá học - After deleting instances, delete course
                firebaseManager.deleteCourse(courseId, new DatabaseReference.CompletionListener() { // Xoá khoá học trên Firebase - Delete course on Firebase
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) { // Khi hoàn thành xoá khoá học - When course deletion completes
                        if (error == null) { // Nếu không có lỗi - If no error
                            // Xoá local sau khi xoá cloud thành công - Delete local after successful cloud deletion
                            db.courseDao().deleteByFirebaseId(courseId); // Xoá khoá học trong Room database - Delete course in Room database
                            // Xoá các buổi học local - Delete local class instances
                            db.classInstanceDao().deleteInstancesByCourseFirebaseId(courseId); // Xoá buổi học trong Room database - Delete instances in Room database
                            Toast.makeText(CourseDetailActivity.this, "Course and related class instances deleted successfully", Toast.LENGTH_SHORT).show(); // Hiện thông báo thành công - Show success message
                            finish(); // Đóng Activity - Close Activity
                        } else {
                            Toast.makeText(CourseDetailActivity.this, "Error deleting course", Toast.LENGTH_SHORT).show(); // Hiện thông báo lỗi - Show error message
                        }
                    }
                });
            }
        });
    }

    // Hiển thị dialog xác nhận xoá buổi học - Show class instance deletion confirmation dialog
    private void confirmDeleteInstance(ClassInstance instance) { // Phương thức xác nhận xoá buổi học - Method to confirm instance deletion
        new AlertDialog.Builder(this) // Tạo AlertDialog builder - Create AlertDialog builder
                .setTitle("Delete Class Session") // Đặt tiêu đề - Set title
                .setMessage("Are you sure you want to delete this class session?") // Đặt nội dung thông báo - Set message content
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() { // Nút xác nhận xoá - Delete confirmation button
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // Khi nhấn nút Delete - When Delete button is pressed
                        deleteClassInstance(instance); // Gọi phương thức xoá buổi học - Call delete instance method
                    }
                })
                .setNegativeButton("Cancel", null) // Nút hủy bỏ - Cancel button
                .show(); // Hiển thị dialog - Show dialog
    }

    // Xoá buổi học: xoá trên Firebase trước, sau đó xoá local
    // Delete class instance: delete on Firebase first, then delete local
    private void deleteClassInstance(ClassInstance instance) { // Phương thức xoá buổi học - Method to delete class instance
        // Xoá trên Firebase trước - Delete on Firebase first
        if (instance.getId() != null) { // Nếu có Firebase ID - If has Firebase ID
            firebaseManager.deleteClassInstance(instance.getId(), new DatabaseReference.CompletionListener() { // Xoá trên Firebase - Delete on Firebase
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) { // Khi hoàn thành xoá - When deletion completes
                    // Xoá local bất kể kết quả Firebase - Delete local regardless of Firebase result
                    if (instance.getLocalId() > 0) { // Nếu có local ID - If has local ID
                        db.classInstanceDao().deleteByLocalId(instance.getLocalId()); // Xoá theo local ID - Delete by local ID
                    } else if (instance.getId() != null) { // Nếu có Firebase ID - If has Firebase ID
                        db.classInstanceDao().deleteByFirebaseId(instance.getId()); // Xoá theo Firebase ID - Delete by Firebase ID
                    }

                    runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                        if (error == null) { // Nếu không có lỗi - If no error
                            Toast.makeText(CourseDetailActivity.this, "Class session deleted successfully", Toast.LENGTH_SHORT).show(); // Hiện thông báo thành công - Show success message
                        } else {
                            Toast.makeText(CourseDetailActivity.this, "Deleted locally, sync failed", Toast.LENGTH_SHORT).show(); // Hiện thông báo đồng bộ thất bại - Show sync failed message
                        }
                        // Refresh lại danh sách - Refresh the list
                        if (classInstanceAdapter != null) { // Nếu adapter tồn tại - If adapter exists
                            loadClassInstances(courseId); // Tải lại danh sách buổi học - Reload class instances
                        }
                    });
                }
            });
        } else {
            // Nếu không có Firebase ID, chỉ xoá local - If no Firebase ID, only delete local
            if (instance.getLocalId() > 0) { // Nếu có local ID - If has local ID
                db.classInstanceDao().deleteByLocalId(instance.getLocalId()); // Xoá theo local ID - Delete by local ID
                Toast.makeText(CourseDetailActivity.this, "Class session deleted locally", Toast.LENGTH_SHORT).show(); // Hiện thông báo xoá local - Show local deletion message
                if (classInstanceAdapter != null) { // Nếu adapter tồn tại - If adapter exists
                    loadClassInstances(courseId); // Tải lại danh sách buổi học - Reload class instances
                }
            }
        }
    }
}