package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashLogoActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        } else setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_splash_logo);

        mAuth = FirebaseAuth.getInstance ();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mAuth.getCurrentUser () != null){
                    startActivity (new Intent (getApplicationContext (),MainActivity.class));
                }
                else{
                    mAuth.signInAnonymously ().addOnSuccessListener (new OnSuccessListener<AuthResult> ( ) {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText (SplashLogoActivity.this, "Logged in with Temporary Act",Toast.LENGTH_SHORT).show ();
                            startActivity (new Intent (getApplicationContext (),MainActivity.class));
                            finish (); //this is to prevent the user from going back to the previous activity, it removes the cache from the previous activity

                        }
                    }).addOnFailureListener (new OnFailureListener ( ) {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText (SplashLogoActivity.this,"Error!" + e.getMessage (),Toast.LENGTH_SHORT).show ();
                            finish ();
                        }
                    });
                }



           /*     Intent homeIntent = new Intent(SplashLogoActivity.this, RegisterLogin.class);

                startActivity(homeIntent);

                finish();*/
            }
        }, SPLASH_TIME_OUT);

    }
}
