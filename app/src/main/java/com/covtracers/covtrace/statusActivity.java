package com.covtracers.covtrace;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class statusActivity extends AppCompatActivity {
    private static final String TAG = "Database";
    TextView hyperLink;
    Spanned Text;
    Button submit;
    Button yes1;
    Button no1;
    Button yes2;
    Button no2;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        hyperLink = findViewById(R.id.hyperlink);
        submit = findViewById(R.id.submit_data);
        yes1 = findViewById(R.id.yes1);
        yes2 = findViewById(R.id.yes2);
        no1 = findViewById(R.id.no1);
        no2 = findViewById(R.id.no2);
        final Button q2 = findViewById(R.id.question2);
        final boolean[] test = new boolean[1];
        final boolean[] result = new boolean[1];
        yes1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test[0] = true;
                result[0] = false;
                q2.setText("What was the result?");
            }
        });
        yes2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test[0] = true;
                result[0] = true;
            }
        });
        no1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test[0] = false;
                result[0] = false;
                q2.setText("Do you have any symptoms?");
                //q2.setText("Do you have any symptoms");
            }
        });
        no2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test[0] = false;
                result[0] = false;
            }
        });
        submit.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("SetTextI18n")
            public void onClick(View v){
                Map<String, Object> city = new HashMap<>();
                if (test[0] == true && result[0] == true) {
                    city.put("status", "positive");
                }else{
                    city.put("status", "negative");
                }

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("HH:mm aa");
                String time = format.format(cal.getTime());
                String formatted = DateFormat.getDateInstance(DateFormat.SHORT).format(cal.getTime()) +", " + time;
                city.put("date-time", formatted);
                db.collection("users").document("PeerID")
                        .set(city)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

                submit.setText("hello");
            }
        });
        Text = Html.fromHtml("Click on this link to visit my Website <br />" +
                "<a href='https://www.android-examples.com//'>Android-Examples.com</a>");
        hyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        hyperLink.setText(Text);

    }
}
