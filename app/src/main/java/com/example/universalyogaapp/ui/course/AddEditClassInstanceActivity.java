
// Activity cho chức năng thêm/sửa buổi học (class instance)
// Activity for adding/editing class instance functionality
package com.example.universalyogaapp.ui.course;

// Import các thư viện cần thiết cho Activity này
// Import necessary libraries for this Activity
import android.app.DatePickerDialog; // Chọn ngày - Date picker dialog
import android.os.Bundle; // Bundle để truyền dữ liệu - Bundle for passing data
import android.text.TextUtils; // Kiểm tra chuỗi rỗng - Check empty strings
import android.view.View; // View cơ bản - Basic view
import android.widget.Button; // Widget nút bấm - Button widget
import android.widget.DatePicker; // Widget chọn ngày - Date picker widget
import android.widget.EditText; // Widget nhập text - Text input widget
import android.widget.Toast; // Hiển thị thông báo - Display toast messages
import androidx.annotation.Nullable; // Annotation cho giá trị có thể null - Nullable annotation
import androidx.appcompat.app.AppCompatActivity; // Activity cơ bản của AppCompat - Base AppCompat activity
import com.example.universalyogaapp.R; // Resource layout - Layout resources
import com.example.universalyogaapp.firebase.FirebaseManager; // Quản lý đồng bộ Firebase - Firebase sync manager
import com.example.universalyogaapp.model.ClassInstance; // Model buổi học - Class instance model
import com.example.universalyogaapp.db.AppDatabase; // Room database - Room database
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity buổi học - Class instance entity
import com.example.universalyogaapp.db.CourseEntity; // Entity khoá học - Course entity
import java.text.ParseException; // Exception khi parse lỗi - Parse exception
import java.text.SimpleDateFormat; // Định dạng ngày - Date formatting
import java.util.Calendar; // Lịch - Calendar
import java.util.Date; // Ngày tháng - Date
import java.util.Locale; // Địa phương hoá - Localization
import android.net.ConnectivityManager; // Quản lý kết nối mạng - Network connectivity manager
import android.net.NetworkInfo; // Thông tin mạng - Network information
import java.util.List; // Danh sách - List collection

// Activity cho phép thêm hoặc sửa một buổi học (class instance)
// Activity that allows adding or editing a class instance
public class AddEditClassInstanceActivity extends AppCompatActivity {
    // Khai báo các biến UI components - Declare UI component variables
    private EditText editTextDate, editTextTeacher, editTextNote; // Các ô nhập liệu - Input fields
    private Button buttonSave; // Nút lưu - Save button
    
    // Khai báo các biến dữ liệu - Declare data variables
    private String courseFirebaseId, courseSchedule; // ID khoá học trên Firebase và lịch học - Course Firebase ID and schedule
    private int courseLocalId; // ID khoá học local - Local course ID
    private FirebaseManager firebaseManager; // Quản lý đồng bộ Firebase - Firebase sync manager
    private ClassInstance editingInstance; // Nếu đang sửa, lưu instance cần sửa - Instance being edited if in edit mode
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Định dạng ngày - Date format
    private AppDatabase db; // Room database - Room database instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Gọi phương thức cha - Call parent method
        setContentView(R.layout.activity_add_edit_class_instance); // Gán layout cho Activity - Set layout for Activity

        // Khởi tạo các view từ layout - Initialize views from layout
        editTextDate = findViewById(R.id.editTextDate); // Tìm và gán ô nhập ngày - Find and assign date input field
        editTextTeacher = findViewById(R.id.editTextTeacher); // Tìm và gán ô nhập giáo viên - Find and assign teacher input field
        editTextNote = findViewById(R.id.editTextNote); // Tìm và gán ô nhập ghi chú - Find and assign note input field
        buttonSave = findViewById(R.id.buttonSaveInstance); // Tìm và gán nút lưu - Find and assign save button
        firebaseManager = new FirebaseManager(); // Khởi tạo quản lý Firebase - Initialize Firebase manager

        // Khởi tạo database Room với migration - Initialize Room database with migration
        db = androidx.room.Room.databaseBuilder(
            getApplicationContext(), // Context ứng dụng - Application context
            AppDatabase.class, // Class database - Database class
            "yoga-db" // Tên database - Database name
        ).allowMainThreadQueries() // Cho phép query trên main thread - Allow main thread queries
         .addMigrations(AppDatabase.MIGRATION_5_6) // Thêm migration từ version 5 lên 6 - Add migration from version 5 to 6
        .build(); // Xây dựng database - Build database

        // Lấy thông tin khoá học và instance từ Intent - Get course and instance info from Intent
        courseFirebaseId = getIntent().getStringExtra("course_id"); // Lấy Firebase ID của khoá học - Get course Firebase ID
        courseSchedule = getIntent().getStringExtra("course_schedule"); // Lấy lịch học của khoá học - Get course schedule
        editingInstance = (ClassInstance) getIntent().getSerializableExtra("class_instance"); // Lấy instance đang sửa - Get instance being edited

        // Lấy courseLocalId từ database dựa vào courseFirebaseId - Get local course ID from database based on Firebase ID
        if (courseFirebaseId != null) { // Nếu có Firebase ID - If Firebase ID exists
            CourseEntity courseEntity = db.courseDao().getCourseByFirebaseId(courseFirebaseId); // Tìm khoá học theo Firebase ID - Find course by Firebase ID
            if (courseEntity != null) { // Nếu tìm thấy khoá học - If course found
                courseLocalId = courseEntity.localId; // Lấy local ID - Get local ID
            }
        }

        // Nếu đang sửa, hiển thị dữ liệu cũ - If editing, display existing data
        if (editingInstance != null) { // Nếu có instance để sửa - If there's an instance to edit
            setTitle("Edit Class Session"); // Đặt tiêu đề cho màn hình sửa - Set title for edit screen
            editTextDate.setText(editingInstance.getDate()); // Hiển thị ngày cũ - Display existing date
            editTextTeacher.setText(editingInstance.getTeacher()); // Hiển thị tên giáo viên cũ - Display existing teacher name
            editTextNote.setText(editingInstance.getNote()); // Hiển thị ghi chú cũ - Display existing note
        } else { // Nếu không có instance (thêm mới) - If no instance (adding new)
            setTitle("Add Class Session"); // Đặt tiêu đề cho màn hình thêm - Set title for add screen
        }

        // Sự kiện chọn ngày - Date selection event
        editTextDate.setOnClickListener(new View.OnClickListener() { // Gán listener cho ô nhập ngày - Assign listener to date input field
            @Override
            public void onClick(View v) { // Khi click vào ô ngày - When date field is clicked
                showDatePicker(); // Hiển thị bộ chọn ngày - Show date picker
            }
        });

        // Sự kiện lưu instance - Save instance event
        buttonSave.setOnClickListener(new View.OnClickListener() { // Gán listener cho nút lưu - Assign listener to save button
            @Override
            public void onClick(View v) { // Khi click nút lưu - When save button is clicked
                saveInstance(); // Gọi phương thức lưu - Call save method
            }
        });
    }

    // Hiển thị DatePicker để chọn ngày - Display DatePicker for date selection
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(); // Lấy lịch hiện tại - Get current calendar
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() { // Tạo dialog chọn ngày - Create date picker dialog
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { // Khi người dùng chọn ngày - When user selects date
                String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth); // Định dạng ngày yyyy-MM-dd - Format date as yyyy-MM-dd
                editTextDate.setText(dateStr); // Đặt ngày đã chọn vào ô nhập - Set selected date to input field
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)); // Khởi tạo với ngày hiện tại - Initialize with current date
        dialog.show(); // Hiển thị dialog - Show dialog
    }

    // Kiểm tra kết nối mạng - Check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Lấy service quản lý kết nối - Get connectivity manager service
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Lấy thông tin mạng đang hoạt động - Get active network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Trả về true nếu có mạng - Return true if network is available
    }

    // Lưu instance mới hoặc cập nhật instance cũ - Save new instance or update existing instance
    private void saveInstance() {
        String date = editTextDate.getText().toString().trim(); // Lấy ngày học từ ô nhập - Get date from input field
        String teacher = editTextTeacher.getText().toString().trim(); // Lấy tên giáo viên từ ô nhập - Get teacher name from input field
        String note = editTextNote.getText().toString().trim(); // Lấy ghi chú từ ô nhập - Get note from input field

        // Kiểm tra dữ liệu đầu vào - Validate input data
        if (TextUtils.isEmpty(date)) { // Nếu ngày học trống - If date is empty
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi - Show error message
            return; // Dừng xử lý - Stop processing
        }

        // Kiểm tra ngày học có khớp với lịch khoá học không - Check if date matches course schedule
        if (courseSchedule != null && !courseSchedule.isEmpty()) { // Nếu có lịch học được định nghĩa - If course schedule is defined
            if (!isDateMatchSchedule(date, courseSchedule)) { // Nếu ngày không khớp lịch - If date doesn't match schedule
                Toast.makeText(this, "Selected date does not match course schedule (" + courseSchedule + ")", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi - Show error message
                return; // Dừng xử lý - Stop processing
            }
        }

        // Nếu là thêm mới - If adding new instance
        if (editingInstance == null) {
            addNewInstance(date, teacher, note); // Gọi phương thức thêm mới - Call add new method
        } else {
            // Nếu là sửa - If editing existing instance
            editExistingInstance(date, teacher, note); // Gọi phương thức sửa - Call edit method
        }
    }

    // Thêm mới một buổi học - Add a new class instance
    private void addNewInstance(String date, String teacher, String note) {
        // Kiểm tra trùng ngày trong cùng khoá học - Check for duplicate date in same course
        List<ClassInstanceEntity> existingInstances = db.classInstanceDao().getInstancesForCourse(courseFirebaseId); // Lấy tất cả buổi học của khoá học - Get all instances for course
        for (ClassInstanceEntity existing : existingInstances) { // Duyệt qua từng buổi học hiện có - Iterate through existing instances
            if (existing.date.equals(date)) { // Nếu trùng ngày - If date matches
                Toast.makeText(this, "A class session with this date already exists", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi - Show error message
                return; // Dừng xử lý - Stop processing
            }
        }

        // Tạo entity mới - Create new entity
        ClassInstanceEntity entity = new ClassInstanceEntity(); // Khởi tạo entity mới - Initialize new entity
        entity.courseId = courseFirebaseId; // Gán Firebase ID khoá học - Set course Firebase ID
        entity.courseLocalId = courseLocalId; // Gán local ID khoá học - Set course local ID
        entity.firebaseId = null; // Chưa có Firebase ID - No Firebase ID yet
        entity.date = date; // Gán ngày học - Set date
        entity.teacher = teacher; // Gán tên giáo viên - Set teacher name
        entity.note = note; // Gán ghi chú - Set note
        entity.isSynced = false; // Chưa đồng bộ - Not synced yet

        if (isNetworkAvailable()) { // Nếu có kết nối mạng - If network is available
            // Nếu có mạng: lưu local trước, sau đó sync lên Firebase - If online: save locally first, then sync to Firebase
            long localId = db.classInstanceDao().insert(entity); // Lưu vào database local và lấy ID - Save to local database and get ID

            ClassInstance instance = new ClassInstance( // Tạo đối tượng ClassInstance cho Firebase - Create ClassInstance object for Firebase
                null, courseFirebaseId, date, teacher, note, (int) localId // null Firebase ID, với local ID - null Firebase ID, with local ID
            );

            firebaseManager.addClassInstance(instance, (error, ref) -> { // Thêm vào Firebase - Add to Firebase
                if (error == null) { // Nếu thành công - If successful
                    // Nếu sync thành công: cập nhật Firebase ID và đánh dấu đã sync - If sync successful: update Firebase ID and mark as synced
                    db.classInstanceDao().markInstanceAsSynced((int) localId, ref.getKey()); // Cập nhật Firebase ID và trạng thái sync - Update Firebase ID and sync status
                    runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                        Toast.makeText(AddEditClassInstanceActivity.this, "Class session saved and synced!", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo thành công - Show success message
                        finish(); // Đóng Activity - Close Activity
                    });
                } else { // Nếu thất bại - If failed
                    // Nếu sync thất bại: chỉ lưu local - If sync failed: only save locally
                    runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                        Toast.makeText(AddEditClassInstanceActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
                        finish(); // Đóng Activity - Close Activity
                    });
                }
            });
        } else { // Nếu không có mạng - If no network
            // Nếu không có mạng: chỉ lưu local - If offline: only save locally
            db.classInstanceDao().insert(entity); // Lưu vào database local - Save to local database
            Toast.makeText(this, "Class session saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
            Toast.makeText(this, "Class session saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
            finish(); // Đóng Activity - Close Activity
        }
    }

    // Sửa một buổi học đã có - Edit an existing class instance
    private void editExistingInstance(String date, String teacher, String note) {
        ClassInstanceEntity entity = null; // Khởi tạo entity null - Initialize entity as null

        // Tìm entity theo firebaseId trước - Find entity by Firebase ID first
        if (editingInstance.getId() != null) { // Nếu có Firebase ID - If Firebase ID exists
            entity = db.classInstanceDao().getInstanceByFirebaseId(editingInstance.getId()); // Tìm theo Firebase ID - Find by Firebase ID
        }

        // Nếu không tìm thấy, thử theo localId - If not found, try by local ID
        if (entity == null && editingInstance.getLocalId() > 0) { // Nếu chưa tìm thấy và có local ID - If not found and has local ID
            entity = db.classInstanceDao().getInstanceByLocalId(editingInstance.getLocalId()); // Tìm theo local ID - Find by local ID
        }

        if (entity != null) { // Nếu tìm thấy entity - If entity found
            final ClassInstanceEntity finalEntity = entity; // Biến final cho lambda - Final variable for lambda
            finalEntity.date = date; // Cập nhật ngày - Update date
            finalEntity.teacher = teacher; // Cập nhật giáo viên - Update teacher
            finalEntity.note = note; // Cập nhật ghi chú - Update note

            if (isNetworkAvailable() && finalEntity.firebaseId != null) { // Nếu có mạng và có Firebase ID - If online and has Firebase ID
                // Nếu có mạng và đã có Firebase ID: cập nhật cả local và Firebase - If online and has Firebase ID: update both local and Firebase
                finalEntity.isSynced = true; // Đánh dấu đã đồng bộ - Mark as synced
                db.classInstanceDao().update(finalEntity); // Cập nhật local database - Update local database

                ClassInstance instance = new ClassInstance( // Tạo đối tượng để sync Firebase - Create object for Firebase sync
                    finalEntity.firebaseId, finalEntity.courseId, date, teacher, note, finalEntity.id // Với Firebase ID hiện có - With existing Firebase ID
                );

                firebaseManager.updateClassInstance(instance, (error, ref) -> { // Cập nhật Firebase - Update Firebase
                    if (error == null) { // Nếu thành công - If successful
                        runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                            Toast.makeText(AddEditClassInstanceActivity.this, "Class session updated and synced!", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo thành công - Show success message
                            finish(); // Đóng Activity - Close Activity
                        });
                    } else { // Nếu thất bại - If failed
                        // Nếu sync thất bại: đánh dấu chưa sync - If sync failed: mark as not synced
                        finalEntity.isSynced = false; // Đánh dấu chưa đồng bộ - Mark as not synced
                        db.classInstanceDao().update(finalEntity); // Cập nhật lại database - Update database again
                        runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                            Toast.makeText(AddEditClassInstanceActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
                            finish(); // Đóng Activity - Close Activity
                        });
                    }
                });
            } else { // Nếu offline hoặc chưa có Firebase ID - If offline or no Firebase ID
                // Nếu offline hoặc chưa có Firebase ID: chỉ cập nhật local - If offline or no Firebase ID: only update locally
                finalEntity.isSynced = false; // Đánh dấu chưa đồng bộ - Mark as not synced
                db.classInstanceDao().update(finalEntity); // Cập nhật local database - Update local database
                Toast.makeText(this, "Class session updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
                finish(); // Đóng Activity - Close Activity
            }
        } else { // Nếu không tìm thấy entity - If entity not found
            Toast.makeText(this, "Error: Could not find class session to edit", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi - Show error message
        }
    }

    // Kiểm tra ngày học có khớp với lịch khoá học không - Check if class date matches course schedule
    private boolean isDateMatchSchedule(String dateStr, String schedule) {
        try {
            Date date = sdf.parse(dateStr); // Parse chuỗi ngày thành đối tượng Date - Parse date string to Date object
            Calendar cal = Calendar.getInstance(); // Lấy đối tượng Calendar - Get Calendar instance
            cal.setTime(date); // Đặt thời gian cho Calendar - Set time for Calendar
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Lấy thứ trong tuần (1=Chủ nhật, 2=Thứ hai,...) - Get day of week (1=Sunday, 2=Monday,...)
            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Mảng tên các ngày - Array of day names
            String dayName = days[dayOfWeek - 1]; // Lấy tên ngày tương ứng - Get corresponding day name
            return schedule != null && schedule.contains(dayName); // Kiểm tra lịch có chứa tên ngày không - Check if schedule contains day name
        } catch (ParseException e) { // Nếu lỗi parse - If parse error
            return false; // Trả về false - Return false
        }
    }
}