package com.he161696.kingbarber;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.he161696.kingbarber.model.Barbers;
import com.he161696.kingbarber.model.BarbersApdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TopBarbersActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_top_barbers);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        getSupportActionBar().setTitle("Top Barbers");
        // Lấy clientId từ Intent
        Intent intent = getIntent();
        int clientId = intent.getIntExtra("clientId", -1);
        // ListView thay vì RecyclerView vì BarbersApdapter là BaseAdapter
        ListView listView = findViewById(R.id.listAppointments);
        List<Barbers> barberList = new ArrayList<>();
        BarbersApdapter adapter = new BarbersApdapter(this, barberList);
        adapter.setClientId(clientId);
        listView.setAdapter(adapter);
        // Gọi API lấy danh sách barber
        String url = "http://10.0.2.2/api/top_barbers.php"; // Đổi lại đúng IP nếu chạy thật
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        barberList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                Barbers barber = new Barbers();
                                barber.setBarberId(obj.getInt("BarberId"));
                                barber.setFullName(obj.getString("FullName"));
                                barber.setEmail(obj.getString("Email"));
                                barber.setAverageRating((float) obj.getDouble("AverageRating"));
                                barber.setRatingCount(obj.getInt("RatingCount"));
                                if(obj.has("Image_barber")) barber.setImage_barber(obj.getString("Image_barber"));
                                barber.setPassword(""); // Không cần password
                                barberList.add(barber);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TopBarbersActivity.this, "Lỗi tải danh sách barber", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsonArrayRequest);
    }
}