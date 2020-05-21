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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class statusActivity extends AppCompatActivity {
    private static final String TAG = "Database";
    TextView hyperLink;
    Spanned Text;
    Button submit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        hyperLink = findViewById(R.id.hyperlink);
        submit = findViewById(R.id.submit_data);
        submit.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("SetTextI18n")
            public void onClick(View v){

                Map<String, Object> city = new HashMap<>();
                city.put("name", "Los Angeles");
                city.put("state", "CA");
                city.put("country", "USA");

                db.collection("cities").document("LA")
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
