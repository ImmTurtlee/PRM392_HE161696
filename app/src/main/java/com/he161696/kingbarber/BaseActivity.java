package com.he161696.kingbarber;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class BaseActivity extends AppCompatActivity {

    protected void setupToolbar(MaterialToolbar toolbar) {
        setSupportActionBar(toolbar);
    }
}

