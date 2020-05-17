package com.covtracers.covtrace;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Register extends AppCompatActivity implements View.OnClickListener {

    Button bRegister;
    Button bSignIn;
    EditText etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = (EditText) findViewById(R.id.emailText);
        etPassword = (EditText) findViewById(R.id.passwordText);
        etConfirmPassword = (EditText) findViewById(R.id.confirmPasswordText);
        bRegister = (Button) findViewById(R.id.registerButton);

        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
