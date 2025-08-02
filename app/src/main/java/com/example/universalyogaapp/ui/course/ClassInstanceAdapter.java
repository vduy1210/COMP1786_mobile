
// Adapter cho RecyclerView hiển thị danh sách buổi học (class instance)
// Adapter for RecyclerView to display list of class instances
package com.example.universalyogaapp.ui.course;

// Import các thư viện cần thiết cho Adapter
// Import necessary libraries for the Adapter
import android.view.LayoutInflater; // Để inflate layout - For inflating layouts
import android.view.View; // View cơ bản - Basic view
import android.view.ViewGroup; // Nhóm view - View group
import android.widget.Button; // Widget nút bấm - Button widget
import android.widget.TextView; // Widget hiển thị text - Text display widget
import androidx.annotation.NonNull; // Annotation cho giá trị không null - NonNull annotation
import androidx.recyclerview.widget.RecyclerView; // RecyclerView cho danh sách - RecyclerView for lists
import com.example.universalyogaapp.R; // Resource layout - Layout resources
import com.example.universalyogaapp.model.ClassInstance; // Model buổi học - Class instance model
import java.util.List; // Interface danh sách - List interface

import java.text.ParseException; // Exception khi parse ngày - Parse exception for dates
import java.text.SimpleDateFormat; // Định dạng ngày - Date formatting
import java.util.Calendar; // Lịch - Calendar
import java.util.Locale; // Địa phương hoá - Localization

// Adapter cho RecyclerView hiển thị danh sách buổi học (class instance)
// Adapter for RecyclerView to display list of class instances
public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.ViewHolder> {
    
    // Interface cho sự kiện sửa/xoá buổi học - Interface for edit/delete class instance events
    public interface OnInstanceActionListener {
        void onEdit(ClassInstance instance); // Phương thức được gọi khi sửa buổi học - Method called when editing instance
        void onDelete(ClassInstance instance); // Phương thức được gọi khi xoá buổi học - Method called when deleting instance
    }

    // Khai báo biến instance - Declare instance variables
    private List<ClassInstance> instanceList; // Danh sách buổi học - List of class instances
    private final OnInstanceActionListener actionListener; // Listener cho sự kiện - Event listener

    // Constructor khởi tạo adapter với danh sách và listener - Constructor to initialize adapter with list and listener
    public ClassInstanceAdapter(List<ClassInstance> instanceList, OnInstanceActionListener actionListener) {
        this.instanceList = instanceList; // Gán danh sách buổi học - Assign instance list
        this.actionListener = actionListener; // Gán listener sự kiện - Assign action listener
    }

    // Gán lại danh sách buổi học cho adapter - Reassign class instance list to adapter
    public void setInstanceList(List<ClassInstance> list) {
        this.instanceList = list; // Cập nhật danh sách mới - Update with new list
        notifyDataSetChanged(); // Thông báo RecyclerView cập nhật giao diện - Notify RecyclerView to update UI
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho từng item buổi học - Inflate layout for each class instance item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_instance, parent, false); // Tạo view từ layout XML - Create view from XML layout
        return new ViewHolder(view); // Trả về ViewHolder mới - Return new ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassInstance instance = instanceList.get(position); // Lấy buổi học tại vị trí hiện tại - Get instance at current position
        holder.textViewDate.setText(formatDate(instance.getDate())); // Hiển thị ngày học đã định dạng - Display formatted date
        holder.textViewNote.setText("Note: " + (instance.getNote() != null ? instance.getNote() : "")); // Hiển thị ghi chú hoặc chuỗi rỗng - Display note or empty string
        holder.textViewTeacher.setText("Teacher: " + (instance.getTeacher() != null ? instance.getTeacher() : "")); // Hiển thị tên giáo viên hoặc chuỗi rỗng - Display teacher name or empty string
        
        // Hiển thị thứ trong tuần - Display day of week
        String dayOfWeek = getDayOfWeek(instance.getDate()); // Lấy thứ từ ngày - Get day from date
        holder.textViewDayOfWeek.setText(dayOfWeek.isEmpty() ? "" : "(" + dayOfWeek + ")"); // Hiển thị thứ trong ngoặc hoặc rỗng - Display day in parentheses or empty
        
        // Thiết lập sự kiện cho các nút - Set up button events
        holder.buttonEdit.setOnClickListener(v -> actionListener.onEdit(instance)); // Sự kiện click nút sửa - Edit button click event
        holder.buttonDelete.setOnClickListener(v -> actionListener.onDelete(instance)); // Sự kiện click nút xoá - Delete button click event
    }

    // Định dạng ngày từ yyyy-MM-dd sang dd/MM/yyyy - Format date from yyyy-MM-dd to dd/MM/yyyy
    private String formatDate(String dateStr) {
        try {
            java.text.SimpleDateFormat sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US); // Định dạng đầu vào - Input format
            java.text.SimpleDateFormat sdfOutput = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US); // Định dạng đầu ra - Output format
            java.util.Date date = sdfInput.parse(dateStr); // Parse chuỗi ngày thành Date object - Parse date string to Date object
            if (date != null) { // Nếu parse thành công - If parsing successful
                return sdfOutput.format(date); // Trả về ngày đã định dạng - Return formatted date
            }
        } catch (Exception ignored) {} // Bắt và bỏ qua exception - Catch and ignore exceptions
        return dateStr; // Trả về chuỗi gốc nếu có lỗi - Return original string if error
    }

    // Lấy thứ trong tuần từ chuỗi ngày yyyy-MM-dd - Get day of week from date string yyyy-MM-dd
    private String getDayOfWeek(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Định dạng ngày - Date format
            java.util.Date date = sdf.parse(dateStr); // Parse chuỗi thành Date - Parse string to Date
            if (date != null) { // Nếu parse thành công - If parsing successful
                Calendar cal = Calendar.getInstance(); // Lấy instance Calendar - Get Calendar instance
                cal.setTime(date); // Đặt thời gian cho Calendar - Set time for Calendar
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Mảng tên các ngày - Array of day names
                return days[cal.get(Calendar.DAY_OF_WEEK) - 1]; // Trả về tên ngày (Calendar.DAY_OF_WEEK bắt đầu từ 1) - Return day name (Calendar.DAY_OF_WEEK starts from 1)
            }
        } catch (ParseException ignored) {} // Bắt và bỏ qua ParseException - Catch and ignore ParseException
        return ""; // Trả về chuỗi rỗng nếu có lỗi - Return empty string if error
    }

    @Override
    public int getItemCount() {
        return instanceList != null ? instanceList.size() : 0; // Trả về số lượng buổi học hoặc 0 nếu list null - Return number of instances or 0 if list is null
    }

    // ViewHolder cho từng item buổi học - ViewHolder for each class instance item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Khai báo các view components - Declare view components
        TextView textViewDate, textViewNote, textViewTeacher, textViewDayOfWeek; // Các TextView hiển thị thông tin - TextViews for displaying information
        Button buttonEdit, buttonDelete; // Các nút sửa và xoá - Edit and delete buttons
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView); // Gọi constructor cha - Call parent constructor
            
            // Khởi tạo các view từ layout - Initialize views from layout
            textViewDate = itemView.findViewById(R.id.textViewDate); // Tìm và gán TextView ngày - Find and assign date TextView
            textViewDayOfWeek = itemView.findViewById(R.id.textViewDayOfWeek); // Tìm và gán TextView thứ - Find and assign day of week TextView
            textViewNote = itemView.findViewById(R.id.textViewNote); // Tìm và gán TextView ghi chú - Find and assign note TextView
            textViewTeacher = itemView.findViewById(R.id.textViewTeacher); // Tìm và gán TextView giáo viên - Find and assign teacher TextView
            buttonEdit = itemView.findViewById(R.id.buttonEditInstance); // Tìm và gán nút sửa - Find and assign edit button
            buttonDelete = itemView.findViewById(R.id.buttonDeleteInstance); // Tìm và gán nút xoá - Find and assign delete button
        }
    }
}