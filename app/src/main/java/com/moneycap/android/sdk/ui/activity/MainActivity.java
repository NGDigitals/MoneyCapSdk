package com.moneycap.android.sdk.ui.activity;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.reflect.Type;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.moneycap.android.sdk.R;
import com.moneycap.android.sdk.preference.PrefManager;
import com.moneycap.android.sdk.scanner.ui.activity.ScanActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        prefManager = new PrefManager(mContext);
        Button checkInBtn = findViewById(R.id.check_in_btn);

        checkInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ScanActivity.class);
            HashMap<String, String> bookingMap = new HashMap<String, String>();
            bookingMap.put("qr_code_type", "tr_pre_check");
            bookingMap.put("qr_client_name", "Zeno");
            bookingMap.put("id", "1");
            bookingMap.put("start_terminal", "Lekki Phase 1 Terminal");
            bookingMap.put("stop_terminal", "Ikoyi Bridge Terminal");
            bookingMap.put("amount", "1350");
            intent.putExtra("BOOKING_MAP", bookingMap);
            startActivity(intent);
        });
        /*Gson gson = new Gson();
        ArrayList<HashMap<String, String>> content = new ArrayList<>();

        String contentString = gson.toJson(content);
        prefManager.saveContent(contentString);*/
    }
}
