package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPasswordActivity extends AppCompatActivity {


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    EditText email;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        }
        else setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_forget_password);
        email = (EditText) findViewById(R.id.editText3);

        toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);

        getSupportActionBar ( ).setDisplayHomeAsUpEnabled (true); //back icon
        getSupportActionBar ().setTitle ("Forget Password"); // title of action bar

    }

    public void clickSend(View v) {
        String userEmail = email.getText().toString();

        if (userEmail.length() != 0) {
            mAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void> () {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Email send success
                                Toast.makeText(getBaseContext(), "Email sent successfully.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Email send fails
                                Toast.makeText(getBaseContext(), "Error sending email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "ERROR: Email cannot be empty.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //user click back button it will go back to the MainActivity class
        startActivity (new Intent (this, LoginActivity.class));
        finish(); //destroy current activity
        return super.onOptionsItemSelected (item);
    }
}
