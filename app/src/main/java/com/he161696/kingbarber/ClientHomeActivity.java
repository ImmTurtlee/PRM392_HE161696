package com.he161696.kingbarber;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class ClientHomeActivity extends BaseActivity {
    private Button btnCurrentAppointment, btnTopBarbers, btnBookAppointment, btnPastAppointments, btnEditProfile, btnLogout;
    private int clientId;
    private String fullname;
    private static final String BASE_URL = "http://10.0.2.2/kingbarber/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        // Lấy thông tin người dùng từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        clientId = prefs.getInt("userId", -1);
        fullname = prefs.getString("fullname", "Khách");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        getSupportActionBar().setTitle("Home client");

        btnCurrentAppointment = findViewById(R.id.btnCurrentAppointment);
        btnTopBarbers = findViewById(R.id.btnTopBarbers);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnPastAppointments = findViewById(R.id.btnPastAppointments);
        btnEditProfile = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);
        btnCurrentAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(ClientHomeActivity.this, AppointmentsActivity.class);
            // Gửi clientId để AppointmentsActivity lọc lịch theo user
            intent.putExtra("clientId", clientId);
            startActivity(intent);
        });
        btnBookAppointment.setOnClickListener(v -> {
                    Intent intent = new Intent(ClientHomeActivity.this, BookAppointmentActivity.class);
                    // Gửi clientId để AppointmentsActivity lọc lịch theo user
                    intent.putExtra("clientId", clientId);
                    startActivity(intent);
                });
        btnTopBarbers.setOnClickListener(v -> {
            Intent intent = new Intent(ClientHomeActivity.this, TopBarbersActivity.class);
            intent.putExtra("clientId", clientId);
            startActivity(intent);
        });
        btnPastAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(ClientHomeActivity.this, PastAppointmentsActivity.class);
            intent.putExtra("clientId", clientId);
            startActivity(intent);
        });
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> {
            // Xoá thông tin đăng nhập khỏi SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // hoặc editor.remove("userId") nếu chỉ muốn xoá userId
            editor.apply();

            // Quay lại LoginActivity
            Intent intent = new Intent(ClientHomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xoá ngăn xếp
            startActivity(intent);
            finish();
        });

    }

    @Override
    public void onBackPressed() {
        // Vô hiệu hóa nút back vật lý để không quay về Login
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_client_profile, null);
        builder.setView(dialogView);

        TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        TextInputLayout tilConfirmPassword = dialogView.findViewById(R.id.tilConfirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Load current profile data
        loadClientProfile(etFullName, etEmail);

        // Show/hide confirm password field based on new password input
        etNewPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s.length() > 0) {
                    tilConfirmPassword.setVisibility(View.VISIBLE);
                } else {
                    tilConfirmPassword.setVisibility(View.GONE);
                    etConfirmPassword.setText("");
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            // Validation
            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Full name is required");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email format");
                return;
            }

            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Current password is required");
                return;
            }

            if (!TextUtils.isEmpty(newPassword)) {
                if (newPassword.length() < 6) {
                    etNewPassword.setError("Password must be at least 6 characters");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    etConfirmPassword.setError("Passwords do not match");
                    return;
                }
            }

            // Update profile
            updateClientProfile(fullName, email, currentPassword, newPassword, dialog);
        });

        dialog.show();
    }

    private void loadClientProfile(TextInputEditText etFullName, TextInputEditText etEmail) {
        // Gọi API để lấy thông tin hiện tại của client
        String url = BASE_URL + "get_client_profile.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONObject clientData = jsonResponse.getJSONObject("data");
                            String fullName = clientData.getString("fullName");
                            String email = clientData.getString("email");
                            
                            etFullName.setText(fullName);
                            etEmail.setText(email);
                        } else {
                            // Fallback: load từ SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            String currentFullName = prefs.getString("fullname", "");
                            String currentEmail = prefs.getString("email", "");
                            
                            etFullName.setText(currentFullName);
                            etEmail.setText(currentEmail);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Fallback: load từ SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        String currentFullName = prefs.getString("fullname", "");
                        String currentEmail = prefs.getString("email", "");
                        
                        etFullName.setText(currentFullName);
                        etEmail.setText(currentEmail);
                    }
                },
                error -> {
                    // Fallback: load từ SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String currentFullName = prefs.getString("fullname", "");
                    String currentEmail = prefs.getString("email", "");
                    
                    etFullName.setText(currentFullName);
                    etEmail.setText(currentEmail);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("clientId", String.valueOf(clientId));
                return params;
            }
        };
        
        queue.add(request);
    }

    private void updateClientProfile(String fullName, String email, String currentPassword, String newPassword, AlertDialog dialog) {
        String url = BASE_URL + "update_client_profile.php";
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
                params.put("clientId", String.valueOf(clientId));
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("currentPassword", currentPassword);
                if (!TextUtils.isEmpty(newPassword)) {
                    params.put("newPassword", newPassword);
                }
                return params;
            }
        };
        
        queue.add(request);
    }
}
