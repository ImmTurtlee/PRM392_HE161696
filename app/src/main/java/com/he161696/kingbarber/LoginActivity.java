package com.he161696.kingbarber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        getSupportActionBar().setTitle("Login");

        // Khởi tạo View
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);

        // Xử lý login
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        String url = "http://10.0.2.2/api/login.php"; // Localhost cho Android emulator

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject json = new JSONObject(response);

                boolean success = json.getBoolean("success");

                if (success) {
                    String role = json.getString("role");
                    int userId = json.getInt("userId");
                    String fullname = json.getString("fullname");
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("userId", userId);
                    editor.putString("fullname", fullname);
                    editor.putString("role", role);
                    editor.apply();
                    Toast.makeText(this, "Đăng nhập thành công: " + role, Toast.LENGTH_SHORT).show();

                    Intent intent;
                    if (role.equals("client")) {
                        intent = new Intent(LoginActivity.this, ClientHomeActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("fullname", fullname);
                    } else if (role.equals("barber")) {
                        intent = new Intent(LoginActivity.this, BarberHomeActivity.class);
                        intent.putExtra("barberId", userId); // Truyền barberId đúng key
                        intent.putExtra("fullname", fullname);
                    } else {
                        Toast.makeText(this, "Không xác định vai trò người dùng", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }, error -> {
            Toast.makeText(this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("email", email);
                m.put("password", password);
                return m;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
