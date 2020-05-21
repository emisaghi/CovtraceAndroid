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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class registerActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    EditText emailText, passwordText, confirmPasswordText;
    ImageView imageView;
    TextView textView, signInButton;
    Button registerButton;
    FirebaseAuth fAuth;

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{8,}";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        confirmPasswordText = findViewById(R.id.confirmPasswordText);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        signInButton = findViewById(R.id.signInButton);
        registerButton = findViewById(R.id.registerButton);

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), logInActivity.class));
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();
                String confirmPassword = confirmPasswordText.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailText.setError("Please fill in email!");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordText.setError("Please fill in password!");
                    return;
                }
                if (!password.matches(confirmPassword)) {
                    confirmPasswordText.setError("Passwords don't match!");
                    return;
                }
                if (!isValidPassword(password)) {
                    passwordText.setError("Please ensure your password is at least 8 characters, contains an uppercase, a lower case and a number.");
                    return;
                }

                // Register the user in firebase
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, go to the map
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(registerActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = fAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), mapActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            emailText.setError("createUserWithEmail:failure " + task.getException());
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(registerActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
