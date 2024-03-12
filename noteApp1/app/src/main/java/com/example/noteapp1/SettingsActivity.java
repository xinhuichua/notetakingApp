package com.example.noteapp1;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.widget.Switch;



import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    private GoogleSignInClient mGoogleSignInClient;

    private final String CHANNEL_ID = "personal notifications";
    private final int NOTIFICATION_ID = 001;
    Switch notificationSwitch,lightDarkSwitch;
    SharedPref sharedPref;

    NavigationView nav_view;
    TextView displayUser;
    ImageView displayUserProfile;

    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;

    FirebaseAuth firebaseAuth;

    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref (this);
        if(sharedPref.loadNightModeState ()==true) {

            setTheme (R.style.DarkTheme_NoActionBar);

        }
        else setTheme (R.style.AppTheme_NoActionBar);
            super.onCreate (savedInstanceState);
            setContentView (R.layout.activity_settings);
            lightDarkSwitch = findViewById (R.id.LightDarkTheme);
            if(sharedPref.loadNightModeState ()==true){
                lightDarkSwitch.setChecked (true);
                lightDarkSwitch.setText ("Dark Mode");

            }
            lightDarkSwitch.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener ( ) {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked ){
                        sharedPref.setNightModeState (true);
                        recreate ();

                    }
                    else{
                        sharedPref.setNightModeState (false);
                        recreate ();
                        lightDarkSwitch.setText ("Light Mode");
                    }
                }
            });


        Toolbar toolbar = findViewById (R.id.toolbar2);
        setSupportActionBar (toolbar);

        getSupportActionBar ().setTitle ("Settings");


       //This is to to allow the components in the nav_header.xml file to be used in the MainActivity File
        //Without this block of code,there will be null object reference.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header= navigationView.getHeaderView(0);


        drawerLayout = findViewById(R.id.drawer);
        //create an object for action bar drawer
        //passing parameters to current context where we want to put our current context, "handburger sign"
        toggle = new ActionBarDrawerToggle (this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener (toggle);
        //enabling the handburger sign in the toolbar
        toggle.setDrawerIndicatorEnabled(true);
        //informing the navigation xml folder that the navigation drawer is closed or open currently
        toggle.syncState ();

        //the textView here is in our nav_header which is the header of this mainActivity
        displayUser = header.findViewById(R.id.displayUser);
        //display user profile on nav__header
        displayUserProfile = header.findViewById (R.id.profilePic);



        //notification
        notificationSwitch = findViewById(R.id.notificationSwitch);
        firebaseAuth = FirebaseAuth.getInstance ();
        user = firebaseAuth.getCurrentUser ();
        //getting current user who signed in through google
        GoogleSignInAccount signInGoogleAcc = GoogleSignIn.getLastSignedInAccount(this);

        //displaying if user is using temporary acc or a legit email account on the nav_header.xml file
        if(user.isAnonymous ()){
            //this condition is true if user account is anonymous
            displayUser.setText ("Temporary User");
        }
        if(!user.isAnonymous ()){
            //this condition is true if user is not anonymous
            displayUser.setText ("Welcome " + user.getEmail ());

            if(user.getPhotoUrl () != null){
                String emailPhotoUrl = user.getPhotoUrl ().toString ();
                Glide.with(getApplicationContext()).load(emailPhotoUrl)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(displayUserProfile);
            }
        }

        if(signInGoogleAcc !=null){
            //this condition is true if a google user is authenticated to the app
            displayUser.setText ("Welcome " + signInGoogleAcc.getEmail () +" !");


            //to display user google email profile picture
            //1. get the profile picture from the google email of the current user
            String googlePhotoUrl = signInGoogleAcc.getPhotoUrl().toString ();

            //2. To display of profile picture, it  can be done with bumptech glide library. Glide supports fetching, decoding, and displaying video stills, images, and animated GIFs.
            Glide.with(getApplicationContext()).load(googlePhotoUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(displayUserProfile);
        }
        //display normal login user email
        if(!user.isAnonymous ()){
            if(user.getPhotoUrl () != null) {
                Glide.with (this)
                        .load (user.getPhotoUrl ().toString ())
                        .into (displayUserProfile);
            }



        }



    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //navigation drawer will close once user click on the selected nav menu item.
        drawerLayout.closeDrawer (GravityCompat.START);

        //switch statement is used as there is too many menu items
        switch(item.getItemId ()){

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
                //user is in account settings
            case R.id.accountSettings:
                if(user.isAnonymous ()) {
                    Toast.makeText (this,"Create an account first",Toast.LENGTH_SHORT).show();
                }
                else{

                    startActivity (new Intent (this, AccountActivity.class));

                }
                break;

            //user will go to SettingsActivity
            case R.id.settingsMenu:
                Toast.makeText (this, "You are already in Settings", Toast.LENGTH_SHORT).show ( );
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


    //checking if user is sign in by normal login/ anonymous
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

    //method when i turn on the notifications
    public void setSwitchNotification(View view) {
        createNotificationChannel();

        //notificationCompat.Builder helps to construct typical notification layout
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        //this will be my app icon when the notification pops up
        builder.setSmallIcon(R.drawable.splash_logo);

        // what the notification will say as the description
        builder.setContentText("T-notes App is running in the background");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        //Build.VERSION.SDK_INT returns the API Level of the device where the app is installed
        // Build.VERSION_CODES.O is the API level to compile against in the app/build.gradle file
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) //This statement is true,as my device is running on android SDK 26 and above,this IF statement will be executed.
        {
            CharSequence name = "Personal Notifications";
            String description = "Include all the personal notifications";

            //notification will be shown everywhere, allowed to make noise but does not visually intrude
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel (CHANNEL_ID, name, importance);

            notificationChannel.setDescription (description);

            NotificationManager notificationManager = (NotificationManager) getSystemService (NOTIFICATION_SERVICE);

            //using notification object,we create the channel
            notificationManager.createNotificationChannel (notificationChannel);
        }


    }


}
