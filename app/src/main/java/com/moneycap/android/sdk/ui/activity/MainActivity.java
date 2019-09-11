package com.moneycap.android.sdk.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;

import com.moneycap.android.sdk.R;

import androidx.appcompat.app.AppCompatActivity;
import com.moneycap.android.sdk.scanner.ui.activity.ScanActivity;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        Button checkInBtn = findViewById(R.id.check_in_btn);

        checkInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ScanActivity.class);
            startActivity(intent);
        });
    }
}
