package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailField, mPasswordField;
    private TextView createAcc, forgetPass;

    private Button mLoginBtn;
    Button btnGoogleLogin;
    private LoginButton fbLoginBtn;
    private ProgressBar progressBar;
    private static final String TAG = "FacebookAuthenication";

    //it will help us create sign in
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser user;

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123; //request code i assign for starting a new activity

    private CallbackManager mCallbackManager;

    Toolbar toolbar;
    TextView displayUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPref sharedPref;
        sharedPref = new SharedPref (this);
        if ( sharedPref.loadNightModeState ( ) ) {
            setTheme (R.style.DarkTheme_NoActionBar);

        } else setTheme (R.style.AppTheme_NoActionBar);
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);

        //fb login button
        fbLoginBtn = findViewById (R.id.login_button);


        toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        getSupportActionBar ( ).setDisplayHomeAsUpEnabled (true); //makes the back navigation icon clickable
        getSupportActionBar ( ).setTitle ("");

        mEmailField = (EditText) findViewById (R.id.emailInput);
        mPasswordField = (EditText) findViewById (R.id.passwordInput);

        createAcc = (TextView) findViewById (R.id.createAcc);
        forgetPass = (TextView) findViewById (R.id.forgetPassTxt);

        mLoginBtn = (Button) findViewById (R.id.loginBtn);   //normal email login button

        progressBar = (ProgressBar) findViewById (R.id.progressBar3);


        mAuth = FirebaseAuth.getInstance ( );
        user = mAuth.getCurrentUser ( );
        mFirestore = FirebaseFirestore.getInstance ( );
        FacebookSdk.sdkInitialize (getApplicationContext ( ));

        btnGoogleLogin = findViewById (R.id.btn_google_login);

        //google login
        createRequest ( );
        //google login button
        btnGoogleLogin.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                googleLogin ( ); //calling the googleLogin method
            }
        });


        //fb login
        mCallbackManager = CallbackManager.Factory.create ( );

        fbLoginBtn.registerCallback (mCallbackManager, new FacebookCallback<LoginResult> ( ) {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d (TAG, "onSuccess" + loginResult);
                handleFacebookToken (loginResult.getAccessToken ( ));
                startActivity (new Intent (getApplicationContext ( ), MainActivity.class));
            }


            @Override
            public void onCancel() {


            }

            @Override
            public void onError(FacebookException error) {

            }
        });


        //normal email login
        //if user email input and password input are correct, the user will be sign in if he/she clicks on the login button
        mLoginBtn.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {

                String mEmail = mEmailField.getText ( ).toString ( ).trim ( );
                String mPassword = mPasswordField.getText ( ).toString ( ).trim ( );

                if ( TextUtils.isEmpty (mEmail) ) {
                    mEmailField.setError ("Email is Required.");
                    return;
                }

                if ( TextUtils.isEmpty (mPassword) ) {
                    mPasswordField.setError ("Password is Required.");
                    return;
                }

                if ( mPassword.length ( ) < 6 ) {
                    mPasswordField.setError ("Password Must be more than 6 Characters");
                    return;
                }


                //set progress bar to visible when user is trying to sign in
                progressBar.setVisibility (View.VISIBLE);

                mAuth.signInWithEmailAndPassword (mEmail, mPassword)
                        .addOnCompleteListener (new OnCompleteListener<AuthResult> ( ) {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if ( task.isSuccessful ( ) ) {
                                    if ( mAuth.getCurrentUser ( ).isEmailVerified ( ) ) {
                                        Toast.makeText (LoginActivity.this, "Success!", Toast.LENGTH_LONG).show ( );
                                        startActivity (new Intent (LoginActivity.this, MainActivity.class));

                                        // delete notes for anonymous user
                                        if ( user.isAnonymous ( ) ) {
                                            FirebaseUser user = mAuth.getCurrentUser ( );

                                            mFirestore.collection ("notes").document (user.getUid ( )).delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText (LoginActivity.this, "All Temp Notes are Deleted.", Toast.LENGTH_SHORT).show ( );
                                                }
                                            });


                                            // delete temporary user once user syncs login in with a legit email account

                                            user.delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText (LoginActivity.this, "Temp user Deleted.", Toast.LENGTH_SHORT).show ( );
                                                }
                                            });


                                        }

                                    } else {
                                        Toast.makeText (LoginActivity.this, " Please verify your email address!", Toast.LENGTH_LONG).show ( );
                                        progressBar.setVisibility (View.GONE);
                                    }
                                }
                            }
                        }).addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show ( );
                        progressBar.setVisibility (View.GONE);
                    }
                });


            }
        });

        progressBar.setVisibility (View.GONE);


        //if user does not have an account they can register by clicking on the "New User?Sign up here" TextView
        createAcc.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                startActivity (new Intent (getApplicationContext ( ), RegistrationActivity.class));

            }
        });

        //if user click on forget password text view they will go to forget password activity
        forgetPass.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                startActivity (new Intent (getApplicationContext ( ), ForgetPasswordActivity.class));

            }
        });


    }


    //when user presses the back button this method is called, user will go back to the display notes screen
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //user click back button it will go back to the Register login class class
        startActivity (new Intent (this, RegisterLogin.class));
        finish ( ); //destroy current activity
        return super.onOptionsItemSelected (item);
    }

    //google login
    private void createRequest() {
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        //GoogleSignInOptions object here is used to request the user's email address
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken (getString (R.string.default_web_client_id))
                .requestEmail ( )       //request user google email
                .build ( );
        //the above requests will be hand in to GoogleSignInClient
        //Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient (this, gso);


        GoogleSignIn.getClient (this, gso);

    }


    //this will be called when the google sign in through their google email
    private void googleLogin() {

        Intent signIntent = mGoogleSignInClient.getSignInIntent ( );   //this will show the google account options where user can choose to sign in
        startActivityForResult (signIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) //data is the dat of the user email,username
    {
        //ask for fb results
        mCallbackManager.onActivityResult (requestCode, resultCode, data);

        //google login result
        super.onActivityResult (requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if ( requestCode == RC_SIGN_IN ) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent (data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult (ApiException.class);

                firebaseAuthWithGoogle (account);
            } catch (ApiException e) {
                // Google sign in failed.
                Toast.makeText (LoginActivity.this, e.getMessage ( ), Toast.LENGTH_LONG).show ( );
                // ...
            }
        }


    }

    //this will be called when the user data has been received from google
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential (account.getIdToken ( ), null);
        mAuth.signInWithCredential (credential)
                .addOnCompleteListener (this, new OnCompleteListener<AuthResult> ( ) {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if ( task.isSuccessful ( ) ) {
                            // Sign in success get current user info

                            FirebaseUser user = mAuth.getCurrentUser ( );
                            startActivity (new Intent (getApplicationContext ( ), MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText (LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show ( );

                        }


                    }
                });
    }


    //facebook
    private void handleFacebookToken(AccessToken token) {
        final FirebaseUser user = mAuth.getCurrentUser ( );
        Log.d (TAG, "handleFacebookToken" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential (token.getToken ( ));

        mAuth.signInWithCredential ((credential)).addOnCompleteListener (this, new OnCompleteListener<AuthResult> ( ) {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if ( task.isSuccessful ( ) ) {
                    Toast.makeText (LoginActivity.this, "Sign in successful with facebook", Toast.LENGTH_LONG).show ( );
                    Log.d (TAG, "Sign in with credential: successful");


                } else {
                    Toast.makeText (LoginActivity.this, "Sign in unsuccessful with facebook", Toast.LENGTH_LONG).show ( );


                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart ( );


    }
}



