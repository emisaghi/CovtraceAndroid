package com.covtracers.covtrace;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.covtracers.covtrace.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class status extends AppCompatActivity {
    TextView hyperLink;
    Spanned Text;
    Button submit;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        db = FirebaseFirestore.getInstance();
        hyperLink = findViewById(R.id.hyperlink);
        submit = findViewById(R.id.submit_data);
        submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                db.collection("users").add("hello");
                submit.setText("hello");
        }
        });
        Text = Html.fromHtml("Click on this link to visit my Website <br />" +
                "<a href='https://www.android-examples.com//'>Android-Examples.com</a>");
        hyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        hyperLink.setText(Text);

    }
}
