package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    EditText userEmail,userPassword,confirmPassword ,userPhone;
    Button btnRegister;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    Toolbar toolbar;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    String userID;



    protected void onCreate(Bundle savedInstanceState) {
        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        } else setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_registration);

        firebaseAuth = FirebaseAuth.getInstance ();

        toolbar = findViewById (R.id.toolbar);
        //back button will be displayed
        setSupportActionBar (toolbar);
        getSupportActionBar ( ).setDisplayHomeAsUpEnabled (true);
        getSupportActionBar ().setTitle ("");

        userEmail = findViewById (R.id.emailInput);
        userPassword = findViewById (R.id.passwordInput);
        confirmPassword = findViewById (R.id.passwordTwo);
        userPhone = findViewById (R.id.phoneInput);
        progressBar = findViewById (R.id.progressBar4);

        btnRegister = findViewById (R.id.registerBtn);



        btnRegister.setOnClickListener (new View.OnClickListener ( ) {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

               final String email = userEmail.getText ().toString ().trim ();
               final String phone = userPhone.getText ().toString ().trim();
                final String passwordOne = userPassword.getText ( ).toString ();
                String passwordTwo =  confirmPassword.getText ().toString ();


                if(TextUtils.isEmpty(email)){
                    userEmail.setError ("Email is required");
                    return;
                }

                if( TextUtils.isEmpty(passwordOne)){
                    userPassword.setError("Password is Required.");
                    return;
                }

                if(passwordOne.length() < 6 ){
                    userPassword.setError("Password must be more than 6 Characters");
                    return;
                }

                if(!passwordOne.equals (passwordTwo)){
                    //if the two passwords are not equal there will be an error message
                    confirmPassword.setError ("passwords do not match");
                    return;

                }

                progressBar.setVisibility (View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword (userEmail.getText ().toString (),userPassword.getText ().toString ())
                        .addOnCompleteListener (new OnCompleteListener<AuthResult> ( ) {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if ( task.isSuccessful () ){
                                    //sending email verification to current user
                                    firebaseAuth.getCurrentUser ().sendEmailVerification ().addOnCompleteListener (new OnCompleteListener<Void> ( ) {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //checking if the verification email was sent to user
                                            if ( task.isSuccessful ( ) ) {
                                                Toast.makeText (RegistrationActivity.this, "Registered successfully,Please check your email for verification", Toast.LENGTH_LONG).show ( );

                                                //linking anonymous account with real account
                                                AuthCredential credential = EmailAuthProvider.getCredential (email, passwordOne);

                                                //get anonymous account and link with real firebase account
                                                firebaseAuth.getCurrentUser ( ).linkWithCredential (credential).addOnSuccessListener (new OnSuccessListener<AuthResult> ( ) {
                                                    @Override
                                                    public void onSuccess(AuthResult authResult) {
                                                        Toast.makeText (RegistrationActivity.this, "Notes Are synced", Toast.LENGTH_LONG).show ( );

                                                        //everytime user access sync to a real acc, we save the email to the usr object and we can use it on the MainActivity
                                                        FirebaseUser usr = firebaseAuth.getCurrentUser ( );
                                                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder ( )
                                                                .setDisplayName (email)
                                                                .build ( );
                                                        usr.updateProfile (request);
                                                        startActivity (new Intent (getApplicationContext ( ), MainActivity.class));
                                                    }
                                                });

                                                //if user fails to sign up the progress bar will be gone
                                                progressBar.setVisibility (View.GONE);

                                            }

                                        }


                                    }).addOnFailureListener (new OnFailureListener ( ) {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText (RegistrationActivity.this, "Email not sent" + e.getMessage (), Toast.LENGTH_SHORT).show ( );
                                        }
                                    });
                                    //putting user registered data to user collection in firestore database
                                    userID = firebaseAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = firestore.collection("user").document(userID);
                                    Map<String,Object> user = new HashMap<>();
                                    user.put("email",email);
                                    user.put("phone",phone);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText (RegistrationActivity.this, "user profile created", Toast.LENGTH_SHORT).show ( );
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText (RegistrationActivity.this, "User profile failed", Toast.LENGTH_SHORT).show ( );
                                        }
                                    });
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));

                                }else {
                                    Toast.makeText (RegistrationActivity.this, "Error ! " + task.getException ( ).getMessage ( ), Toast.LENGTH_SHORT).show ( );
                                    progressBar.setVisibility (View.GONE);
                                }


                                }


                        });

                //linking anonymous account with real account
             /*   AuthCredential credential = EmailAuthProvider.getCredential (email,passwordOne) ;

                //get anonymous account and link with real firebase account
                firebaseAuth.getCurrentUser ().linkWithCredential (credential).addOnSuccessListener (new OnSuccessListener<AuthResult> ( ) {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText (RegistrationActivity.this,"Notes Are synced",Toast.LENGTH_LONG).show ();

                        //every time user access sync to a real acc, we save the email to the usr object and we can use it on the MainActivity
                        FirebaseUser usr = firebaseAuth.getCurrentUser ();
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder ()
                                .setDisplayName (email)
                                .build ();
                        usr.updateProfile (request);
                        startActivity (new Intent (getApplicationContext (),MainActivity.class));
                    }
                }).addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (RegistrationActivity.this,"Failed to register.Try again",Toast.LENGTH_LONG).show ();

                        //if user fails to sign up the progress bar will be gone
                        progressBar.setVisibility (View.GONE);
                    }
                });*/


            }

        });


    }



        @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //user click back button it will go back to the Register login class class
        startActivity (new Intent (this, RegisterLogin.class));
        finish(); //destroy current activity
        return super.onOptionsItemSelected (item);
    }
}
