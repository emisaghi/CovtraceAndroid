package com.covtracers.covtrace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    EditText emailText, passwordText;
    ImageView imageView;
    TextView textView, forgotPassword, signUpButton;
    Button logInButton;
    FirebaseAuth fAuth;

    @Override
    public void onStart() {
        super.onStart();

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpButton = findViewById(R.id.signUpButton);
        logInButton = findViewById(R.id.logInButton);

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and go to the next activity.
        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Map.class));
            finish();
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailText.setError("Please fill in email!");
                    return;
                }
                fAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            emailText.setError("Email sent!");
                            Log.d(TAG, "Email sent.");
                        } else {
                            emailText.setError("Email was not found!");
                            Log.d(TAG, "Email was not found.");
                        }
                    }
                });
            }
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailText.setError("Please fill in email!");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordText.setError("Please fill in password!");
                    return;
                }
                // authenticate the user
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, go to the map
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LogIn.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = fAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), Map.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            emailText.setError("signInWithEmail:failure " + task.getException());
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
