package com.he161696.kingbarber;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Map;
import com.squareup.picasso.Picasso;
import com.he161696.kingbarber.CircleTransform;

public class BarberHomeActivity extends AppCompatActivity {

    private ImageView imageBarber;
    private Button CheckSchedule, CheckAppointment, Logout, EditProfile;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private int barberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barber_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.image_barber), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barberId = getIntent().getIntExtra("barberId", -1);
        
        imageBarber = findViewById(R.id.image_barber);
        CheckSchedule = findViewById(R.id.check_shedules);
        CheckAppointment = findViewById(R.id.chek_appointment);
        Logout = findViewById(R.id.btnLogout1);
        EditProfile = findViewById(R.id.edit_profile);
        
        // Đăng ký launcher để nhận kết quả chọn ảnh
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imageBarber.setImageURI(selectedImageUri);
                        }
                    }
                }
        );
        
        // Khi click vào ảnh thì mở gallery
        imageBarber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            }
        });
        
        // Nút Edit Profile
        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        
        CheckAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BarberHomeActivity.this, CheckAppointmentActivity.class);
                intent.putExtra("barberId", barberId);
                startActivity(intent);
            }
        });
        CheckSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BarberHomeActivity.this, CheckScheduleActivity.class);
                intent.putExtra("barberId", barberId);
                startActivity(intent);
            }
        });
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BarberHomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Load barber profile and image
        loadBarberProfile();
    }

    private void loadBarberProfile() {
        String url = "http://10.0.2.2/api/get_barber_profile.php?barberId=" + barberId;
        RequestQueue queue = Volley.newRequestQueue(this);
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String imageUrl = response.optString("image", null);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .transform(new CircleTransform())
                                        .fit()
                                        .centerCrop()
                                        .into(imageBarber);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Có thể hiển thị ảnh mặc định nếu lỗi
                }
        );
        queue.add(request);
    }
    
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Thiết lập để dialog không bị che bởi bàn phím
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        // Lấy các view từ dialog
        TextInputEditText edtFullName = dialogView.findViewById(R.id.edtFullName);
        TextInputEditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        TextInputEditText edtCurrentPassword = dialogView.findViewById(R.id.edtCurrentPassword);
        TextInputEditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        TextInputEditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        // Load thông tin hiện tại
        loadCurrentProfile(edtFullName, edtEmail);
        
        // Ẩn confirm password ban đầu
        edtConfirmPassword.setVisibility(View.GONE);
        
        // Lắng nghe sự kiện thay đổi new password
        edtNewPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s.length() > 0) {
                    // Có nhập new password thì hiện confirm password
                    edtConfirmPassword.setVisibility(View.VISIBLE);
                } else {
                    // Không có new password thì ẩn confirm password
                    edtConfirmPassword.setVisibility(View.GONE);
                    edtConfirmPassword.setText(""); // Xóa text
                }
            }
        });
        
        // Xử lý nút Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Xử lý nút Save
        btnSave.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String currentPassword = edtCurrentPassword.getText().toString();
            String newPassword = edtNewPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();
            
            // Validation
            if (fullName.isEmpty()) {
                edtFullName.setError("Full name is required");
                return;
            }
            
            if (email.isEmpty()) {
                edtEmail.setError("Email is required");
                return;
            }
            
            if (currentPassword.isEmpty()) {
                edtCurrentPassword.setError("Current password is required");
                return;
            }
            
            // Nếu có nhập new password thì phải confirm
            if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError("Passwords do not match");
                return;
            }
            
            // Gọi API để update profile
            updateProfile(fullName, email, currentPassword, newPassword, dialog);
        });
        
        dialog.show();
    }
    
    private void loadCurrentProfile(TextInputEditText edtFullName, TextInputEditText edtEmail) {
        // Gọi API để lấy thông tin hiện tại của barber
        String url = "http://10.0.2.2/api/get_barber_profile.php?barberId=" + barberId;
        RequestQueue queue = Volley.newRequestQueue(this);
        
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String fullName = response.getString("fullName");
                            String email = response.getString("email");
                            
                            edtFullName.setText(fullName);
                            edtEmail.setText(email);
                        } else {
                            // Fallback: load từ SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            String currentFullName = prefs.getString("fullname", "");
                            String currentEmail = prefs.getString("email", "");
                            
                            edtFullName.setText(currentFullName);
                            edtEmail.setText(currentEmail);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Fallback: load từ SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        String currentFullName = prefs.getString("fullname", "");
                        String currentEmail = prefs.getString("email", "");
                        
                        edtFullName.setText(currentFullName);
                        edtEmail.setText(currentEmail);
                    }
                },
                error -> {
                    // Fallback: load từ SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String currentFullName = prefs.getString("fullname", "");
                    String currentEmail = prefs.getString("email", "");
                    
                    edtFullName.setText(currentFullName);
                    edtEmail.setText(currentEmail);
                }
        );
        
        queue.add(request);
    }
    
    private void updateProfile(String fullName, String email, String currentPassword, String newPassword, AlertDialog dialog) {
        String url = "http://10.0.2.2/api/update_barber_profile.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Cập nhật SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("fullname", fullName);
                    editor.putString("email", email);
                    editor.apply();
                    
                    dialog.dismiss();
                },
                error -> {
                    Toast.makeText(this, "Failed to update profile: " + error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("barberId", String.valueOf(barberId));
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("currentPassword", currentPassword);
                if (!newPassword.isEmpty()) {
                    params.put("newPassword", newPassword);
                }
                return params;
            }
        };
        
        queue.add(request);
    }
    
    @Override
    public void onBackPressed() {
        // Vô hiệu hóa nút back vật lý để không quay về Login
    }
}