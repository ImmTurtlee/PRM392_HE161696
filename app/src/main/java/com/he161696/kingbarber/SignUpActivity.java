package com.he161696.kingbarber;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText fullnameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signupBtn, barberBtn, clientBtn;
    private String selectedRole = "";  // "barber" or "client"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sign Up");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Init views
        fullnameInput = findViewById(R.id.fullnameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupBtn = findViewById(R.id.signupBtn);
        barberBtn = findViewById(R.id.BarberBtn);
        clientBtn = findViewById(R.id.ClientBtn);

        // Role selection
        barberBtn.setOnClickListener(v -> {
            selectedRole = "barber";
            barberBtn.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
            clientBtn.setBackgroundColor(getColor(android.R.color.darker_gray));
        });

        clientBtn.setOnClickListener(v -> {
            selectedRole = "client";
            clientBtn.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
            barberBtn.setBackgroundColor(getColor(android.R.color.darker_gray));
        });

        // Sign up button logic
        signupBtn.setOnClickListener(v -> {
            String fullname = fullnameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi dữ liệu tới API
            sendSignupRequest(fullname, email, password, selectedRole);
        });
    }

    private void sendSignupRequest(String fullname, String email, String password, String role) {
        String url = "http://10.0.2.2/api/signup.php"; // 10.0.2.2 cho Android Emulator

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");

                        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SignUpActivity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(SignUpActivity.this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fullname", fullname);
                params.put("email", email);
                params.put("password", password);
                params.put("role", role);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);
        queue.add(stringRequest);
    }
}
