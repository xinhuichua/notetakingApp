package com.example.noteapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterLogin extends AppCompatActivity {

    Button btnRegister;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_register_login);

        btnLogin = findViewById (R.id.signIn);
        btnRegister = findViewById (R.id.signUp);

        //login btn will go to loginActivity
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeIntent = new Intent(RegisterLogin.this, LoginActivity.class);


                startActivity(homeIntent);

                finish();
            }
        });

        //Register button will go to Registration activity
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeIntent = new Intent(RegisterLogin.this, RegistrationActivity.class);


                startActivity(homeIntent);

                finish();
            }
        });

    }


}
