package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WeatherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView cityName, cityTemperature, cityDescription, cityDate;
    private GoogleSignInClient mGoogleSignInClient;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;

    private static final int GALLERY_INTENT_CODE = 1023;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;


    FirebaseUser user;

    StorageReference storageReference;
    TextView displayUser;

    ImageView displayUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        } else setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_weather);

        cityName = findViewById (R.id.cityName);
        cityTemperature = findViewById (R.id.cityTemp);
        cityDate = findViewById (R.id.cityDate);
        cityDescription = findViewById (R.id.cityDescription);
        Toolbar toolbar = findViewById (R.id.toolbar6);
        setSupportActionBar (toolbar);
        getSupportActionBar ( ).setTitle ("Weather Forecast");

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
//the textView here is in our nav_header which is the header of this mainActivity
        displayUser = header.findViewById (R.id.displayUser);





        //display user profile on nav__header
        displayUserProfile = header.findViewById (R.id.profilePic);


        fAuth = FirebaseAuth.getInstance ( );
        fStore = FirebaseFirestore.getInstance ( );
        user = fAuth.getCurrentUser ( );
        storageReference = FirebaseStorage.getInstance ( ).getReference ( );
        //getting current user who signed in through google
        GoogleSignInAccount googleUser = GoogleSignIn.getLastSignedInAccount (this);
        if ( googleUser != null ) {
            displayUser.setText ("Welcome " + googleUser.getEmail () +" !");


            //to display user google email profile picture
            //1. get the profile picture from the google email of the current user
            Uri personPhotoUrl = googleUser.getPhotoUrl ( );
            //2. To display of profile picture in nav_header.xml, it  can be done with bumptech glide library. Glide supports fetching, decoding, and displaying video stills, images, and animated GIFs.
            Glide.with (getApplicationContext ( )).load (personPhotoUrl)
                    .thumbnail (0.5f)
                    .crossFade ( )
                    .diskCacheStrategy (DiskCacheStrategy.ALL)
                    .into (displayUserProfile);

        }
        if ( user.isAnonymous ( ) ) {
            fAuth = FirebaseAuth.getInstance ( );
            displayUser.setText ("Temporary user");


        }
        if ( !user.isAnonymous ( ) ) {


            displayUser.setText ("Welcome " + user.getEmail ( ));

            if ( user.getPhotoUrl ( ) != null ) {
                String emailPhotoUrl = user.getPhotoUrl ( ).toString ( );
                Glide.with (getApplicationContext ( )).load (emailPhotoUrl)
                        .thumbnail (0.5f)
                        .crossFade ( )
                        .diskCacheStrategy (DiskCacheStrategy.ALL)
                        .into (displayUserProfile);
            }
        }

        findWeather ( );

    }

    private void findWeather() {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=Singapore,%20SG&appid=e724534964575d17e821bbc7d3ab4cc2&units=metric";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, url, null, new Response.Listener<JSONObject> ( ) {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    //getting weather details from objects in url
                    JSONObject main_object = response.getJSONObject ("main"); //this stores the weather details
                    JSONArray array = response.getJSONArray ("weather"); //we use array to get this as the object is in the array provided by the url
                    JSONObject object = array.getJSONObject (0);
                    String temp = String.valueOf (main_object.getDouble ("temp"));
                    String description = object.getString ("description");
                    String city = response.getString ("name");

                    cityTemperature.setText (temp); //display the temperature on the  Text View
                    cityName.setText (city);
                    cityDescription.setText (description);

                    Calendar calendar = Calendar.getInstance ( );
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat ("EEE, MMM d, yyyy");
                    String date = simpleDateFormat.format (calendar.getTime ( ));


                    cityDate.setText (date);


                    double tempInt = Double.parseDouble (temp);

                    cityTemperature.setText (String.valueOf (tempInt));

                } catch (JSONException e) {
                    e.printStackTrace ( );
                }


            }
        }, new Response.ErrorListener ( ) {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if get request fail
            }
        }
        );

        RequestQueue queue = Volley.newRequestQueue (this);
        queue.add (jsonObjectRequest);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //navigation drawer will close once user click on the selected nav menu item.
        drawerLayout.closeDrawer (GravityCompat.START);

        //switch statement is used as there is too many menu items
        switch (item.getItemId ( )) {

            //to go to MainActivity.java
            case R.id.allNotes:
                startActivity (new Intent (this, MainActivity.class));
                break;


            case R.id.syncNotes:
                //if user is using an anonymous account then user will go to login activity
                if ( user.isAnonymous ( ) ) {
                    startActivity (new Intent (this, RegisterLogin.class));
                } else {
                    Toast.makeText (this, "You are already connected to an account", Toast.LENGTH_SHORT).show ( );

                }
                break;

            case R.id.bookmarkNotes:
                if ( user.isAnonymous ( ) ) {
                    Toast.makeText (this, "Create an account to bookmark your notes", Toast.LENGTH_SHORT).show ( );
                } else {

                    startActivity (new Intent (this, BookmarkNotes.class));

                }
                break;

            //user will go to SettingsActivity
            case R.id.settingsMenu:
                startActivity (new Intent (this, SettingsActivity.class));
                break;

            case R.id.accountSettings:
                startActivity (new Intent (this, AccountActivity.class));
                break;
            case R.id.weather:
                Toast.makeText (this, "You are already here", Toast.LENGTH_SHORT).show ( );
                break;


            //when user clicks on logout button, they will be sign out of the firebase authentication as well
            case R.id.signOut:
                checkUser ( );

                //or google sign out
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken (getString (R.string.default_web_client_id))
                        .requestEmail ( )       //request user google email
                        .build ( );
                //the above requests will be hand in to GoogleSignInClient
                //Build a GoogleSignInClient with the options specified by gso
                mGoogleSignInClient = GoogleSignIn.getClient (this, gso);
                GoogleSignIn.getClient (this, gso).signOut ( );
                break;

        }
        return false;
    }

    //checking if user is sign in by passport/ anonymous
    private void checkUser() {

        if ( user.isAnonymous ( ) ) {
            displayAlert ( );

        } else {
            //if user has a real account and clicks on the sign out button, they will be directed to the splash logo screen
            FirebaseAuth.getInstance ( ).signOut ( );
            startActivity (new Intent (getApplicationContext ( ), SplashLogoActivity.class));
            finish ( );
        }
    }

    //this alert dialogue will appear if the user wants to sign out while he/she is in the anonymous account
    private void displayAlert() {
        //using AlertDialogue it will warn the user if the user is logged into a temporary account
        AlertDialog.Builder warning = new AlertDialog.Builder (this)// in this parameter you have to pass the context
                .setTitle ("Are you sure?")
                .setMessage ("You are logged in with a temporary account. Logging out will delete all data you had just saved.")
                .setPositiveButton ("Sync Notes", new DialogInterface.OnClickListener ( ) {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this will be called when user wants to sync their notes to their login account
                        startActivity (new Intent (getApplicationContext ( ), LoginActivity.class));
                    }
                }).setNegativeButton ("Logout", new DialogInterface.OnClickListener ( ) {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete all notes created by anonymous user


                        //delete anonymous user
                        user.delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity (new Intent (getApplicationContext ( ), SplashLogoActivity.class));
                                finish ( );
                            }
                        });
                    }
                });
        warning.show ( );
    }



}