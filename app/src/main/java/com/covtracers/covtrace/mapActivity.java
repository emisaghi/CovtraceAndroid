package com.covtracers.covtrace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class mapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Button toDashboard = findViewById(R.id.toDashboard);
        toDashboard.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(mapActivity.this, statusActivity.class);
                startActivity(intent);
            }
        });

    }
}
