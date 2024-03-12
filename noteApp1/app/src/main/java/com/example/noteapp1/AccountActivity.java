package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private GoogleSignInClient mGoogleSignInClient;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;

    private static final int GALLERY_INTENT_CODE = 1023 ;
    TextView userAccountemail,userAccountphone;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button resendCode;
    Button resetPassLocal,changeProfileImage;

    FirebaseUser user;

    StorageReference storageReference;
    TextView deleteAcc,displayUser;

    ImageView displayUserProfile, userAccountProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        }
        else setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_account);

        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        getSupportActionBar ( ).setTitle ("Account");

        //This is to to allow the components in the nav_header.xml file to be used in the MainActivity File
        //Without this block of code,there will be null object reference.
        NavigationView navigationView = (NavigationView) findViewById (R.id.nav_view);
        navigationView.setNavigationItemSelectedListener (this);
        View header = navigationView.getHeaderView (0);

        drawerLayout = findViewById (R.id.drawer);

        //create an object for action bar drawer
        //passing parameters to current context where we want to put our current context, "handburger sign"
        toggle = new ActionBarDrawerToggle (this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener (toggle);
        toggle.syncState ( );

        userAccountphone = findViewById(R.id.profilePhone);

        userAccountemail    = findViewById(R.id.profileEmail);
        userAccountProfile = (ImageView) findViewById (R.id.profileImage);
        userAccountphone = findViewById (R.id.profilePhoneNo);
        resetPassLocal = findViewById(R.id.resetPasswordLocal);


        changeProfileImage = findViewById(R.id.changeProfile);

//the textView here is in our nav_header which is the header of this mainActivity
        displayUser = header.findViewById (R.id.displayUser);


        deleteAcc = (TextView) findViewById (R.id.deleteAcc);


        //display user profile on nav__header
        displayUserProfile = header.findViewById (R.id.profilePic);



        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser ();
        storageReference = FirebaseStorage.getInstance().getReference();
        //getting current user who signed in through google
        GoogleSignInAccount googleUser = GoogleSignIn.getLastSignedInAccount (this);
        if ( googleUser != null ) {
            //this condition is true if a google user is authenticated to the app
            displayUser.setText ("Welcome " + googleUser.getEmail () + " !");

            userAccountemail.setText (googleUser.getEmail ( ));

            //to display user google email profile picture
            //1. get the profile picture from the google email of the current user
           Uri personPhotoUrl = googleUser.getPhotoUrl ( );
            //2. To display of profile picture in nav_header.xml, it  can be done with bumptech glide library. Glide supports fetching, decoding, and displaying video stills, images, and animated GIFs.
            Glide.with (getApplicationContext ( )).load (personPhotoUrl)
                    .thumbnail (0.5f)
                    .crossFade ( )
                    .diskCacheStrategy (DiskCacheStrategy.ALL)
                    .into (displayUserProfile);

            //3. To display of profile picture in account activity, it  can be done with bumptech glide library. Glide supports fetching, decoding, and displaying video stills, images, and animated GIFs.
            Glide.with (getApplicationContext ( )).load (personPhotoUrl)
                    .thumbnail (0.5f)
                    .crossFade ( )
                    .diskCacheStrategy (DiskCacheStrategy.ALL)
                    .into (userAccountProfile);

            //if user login to google they cannot edit their profile and reset their password in this app
            changeProfileImage.setEnabled(false);
            changeProfileImage.setVisibility (View.GONE);
            resetPassLocal.setEnabled (false);
            resetPassLocal.setVisibility (View.GONE);

            deleteAcc.setVisibility (View.GONE);
        }
        if(user.isAnonymous ()){
            fAuth = FirebaseAuth.getInstance();
            displayUser.setText ("Temporary user");
            userAccountemail.setText ("Temporary user");

        }
        if(!user.isAnonymous ()){


            displayUser.setText ("Welcome " + user.getEmail () );
            userAccountemail.setText (user.getEmail ( ));
            if(user.getPhotoUrl () != null){
                String emailPhotoUrl = user.getPhotoUrl ().toString ();
                Glide.with(getApplicationContext()).load(emailPhotoUrl)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(displayUserProfile);
                Glide.with(getApplicationContext()).load(emailPhotoUrl)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(userAccountProfile);
            }
        }



        //if user click on the textView deactivate account, their account will be deleted
        deleteAcc.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                deleteUserAcc ( );

            }
        });

        user = fAuth.getCurrentUser ();
        StorageReference profileRef = storageReference.child("users/"+ user.getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get ().load (uri).into (displayUserProfile);

            }
        });

        resendCode = findViewById(R.id.resendCode);



        userId = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        if(!user.isEmailVerified()){

            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                        }
                    });
                }
            });
        }

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot> () {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    userAccountphone.setText(documentSnapshot.getString("phone"));
                    userAccountemail.setText(documentSnapshot.getString("email"));
                    userAccountphone.setText(documentSnapshot.getString("phone"));

                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });


        resetPassLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = fAuth.getCurrentUser ();
                final EditText resetPassword = new EditText(v.getContext());

                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password ?");
                passwordResetDialog.setMessage("Enter New Password > 6 Characters long.");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String newPassword = resetPassword.getText().toString();
                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AccountActivity.this, "Password Reset Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(AccountActivity.this, e.getMessage (), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close
                    }
                });

                passwordResetDialog.create().show();

            }
        });

        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                if(userAccountphone != null) {
                    Intent i = new Intent (v.getContext ( ), EditProfile.class);
                    i.putExtra ("email", userAccountemail.getText ( ).toString ( ));
                    i.putExtra ("phone", userAccountphone.getText ( ).toString ( ));
                    startActivity (i);

                }
                else {
                    Intent i = new Intent (v.getContext ( ), EditProfile.class);
                    i.putExtra ("email", userAccountemail.getText ( ).toString ( ));



                    startActivity (i);

                }
            }
        });


    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //navigation drawer will close once user click on the selected nav menu item.
        drawerLayout.closeDrawer (GravityCompat.START);

        //switch statement is used as there is too many menu items
        switch(item.getItemId ()){

            //to go to MainActivity.java
            case R.id.allNotes:
                startActivity (new Intent (this,MainActivity.class));
                break;



            case R.id.syncNotes:
                //if user is using an anonymous account then user will go to login activity
                if(user.isAnonymous ()) {
                    startActivity (new Intent (this, RegisterLogin.class));
                }
                else{
                    Toast.makeText (this,"You are already connected to an account",Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.bookmarkNotes:
            if(user.isAnonymous ()) {
                Toast.makeText (this,"Create an account to bookmark your notes",Toast.LENGTH_SHORT).show();
            }
            else{

                startActivity (new Intent (this, BookmarkNotes.class));

            }
            break;

            //user will go to SettingsActivity
            case R.id.settingsMenu:
                startActivity (new Intent (this,SettingsActivity.class));
                break;

            case R.id.accountSettings:
                Toast.makeText (this, "You are already in Account Settings", Toast.LENGTH_SHORT).show ( );
                break;
            case R.id.weather:
                startActivity (new Intent(this,WeatherActivity.class));
                break;


            //when user clicks on logout button, they will be sign out of the firebase authentication as well
            case R.id.signOut:
                checkUser ();

                //or google sign out
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken (getString (R.string.default_web_client_id))
                        .requestEmail ()       //request user google email
                        .build ();
                //the above requests will be hand in to GoogleSignInClient
                //Build a GoogleSignInClient with the options specified by gso
                mGoogleSignInClient = GoogleSignIn.getClient (this,gso);
                GoogleSignIn.getClient (this,gso).signOut ();
                break;

        }
        return false;
    }


    //checking if user is sign in by passport/ anonymous
    private void checkUser(){

        if(user.isAnonymous ()){
            displayAlert();

        } else{
            //if user has a real account and clicks on the sign out button, they will be directed to the splash logo screen
            FirebaseAuth.getInstance ().signOut ();
            startActivity (new Intent(getApplicationContext (),SplashLogoActivity.class));
            finish ();
        }
    }
    //this alert dialogue will appear if the user wants to sign out while he/she is in the anonymous account
    private void displayAlert(){
        //using AlertDialogue it will warn the user if the user is logged into a temporary account
        AlertDialog.Builder warning = new AlertDialog.Builder (this)// in this parameter you have to pass the context
                .setTitle ("Are you sure?")
                .setMessage ("You are logged in with a temporary account. Logging out will delete all data you had just saved.")
                .setPositiveButton ("Sync Notes", new DialogInterface.OnClickListener ( ) {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this will be called when user wants to sync their notes to their login account
                        startActivity (new Intent(getApplicationContext (),LoginActivity.class));
                    }
                }).setNegativeButton ("Logout", new DialogInterface.OnClickListener ( ) {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete all notes created by anonymous user


                        //delete anonymous user
                        user.delete ().addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity (new Intent(getApplicationContext (),SplashLogoActivity.class));
                                finish ();
                            }
                        });
                    }
                });
        warning.show ();
    }


    //deleting user account method
    private void deleteUserAcc(){

        if(!user.isAnonymous ()){
            alertDeactivate ();

       /* } else{
            //if user has a real account and clicks on the delete acc button they will be redirected to splashlogo
            String userId = firebaseAuth.getCurrentUser ( ).getUid ( );
            db.collection ("notes").document(userId).delete ();
            user.delete ();
           Toast.makeText (AccountActivity.this,"Deleted temporary account",Toast.LENGTH_LONG).show ();
            startActivity (new Intent(getApplicationContext (),SplashLogoActivity.class));*/


        }
    }

    //this alert dialogue will show if the registered users that are not anonymous who wants to deactivate their account
    private void alertDeactivate() {

        //using AlertDialogue it will warn the user if the user wants to delete their account
        AlertDialog.Builder warning = new AlertDialog.Builder (this)// in this parameter you have to pass the context
                .setTitle ("Are you sure?")
                .setMessage ("Deactivating account will delete all data .")
                .setPositiveButton ("Cancel", new DialogInterface.OnClickListener ( ) {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel the alert dialogue operation
                        dialog.cancel ( );

                    }
                }).setNegativeButton ("Deactivate", new DialogInterface.OnClickListener ( ) {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete all notes created by current user

                            fStore.collection ("notes").document (user.getUid ( )).delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                            fStore.collection ("user").document (user.getUid ( )).delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        fStore.collection ("bookmark").document (user.getUid ( )).delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                            user.delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    Toast.makeText (AccountActivity.this, "Your account has been deleted", Toast.LENGTH_LONG).show ( );
                                    startActivity (new Intent (getApplicationContext ( ), SplashLogoActivity.class));

                                }
                            });
                        }



                });

        warning.show ( );


    }




}
