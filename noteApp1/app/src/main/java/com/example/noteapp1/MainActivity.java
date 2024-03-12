package com.example.noteapp1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.noteapp1.model.Note;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener {
    private GoogleSignInClient mGoogleSignInClient;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteLists;

    // Adapter adapter;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DocumentReference documentReference;
    FirebaseFirestore mfirestore;
    TextView displayUser;
    ImageView displayUserProfile;
    FirebaseAuth firebaseAuth;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref (this);
        if ( sharedPref.loadNightModeState ( ) ) {
            setTheme (R.style.DarkTheme_NoActionBar);

        } else setTheme (R.style.AppTheme_NoActionBar);

        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);


        //must make sure androidx toolbar is imported otherwise setSupportActionBar cannot be used
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        getSupportActionBar ( ).setTitle ("All Notes");
        mfirestore = FirebaseFirestore.getInstance ( );
        firebaseAuth = FirebaseAuth.getInstance ( );


        user = firebaseAuth.getCurrentUser ( );
        documentReference = mfirestore.collection ("user").document ((user.getUid ( )));


        //This is to to allow the components in the nav_header.xml file to be used in the MainActivity File
        //Without this block of code,there will be null object reference.
        NavigationView navigationView = (NavigationView) findViewById (R.id.nav_view);
        navigationView.setNavigationItemSelectedListener (this);
        View header = navigationView.getHeaderView (0);

        //the textView here is in our nav_header which is the header of this mainActivity
        displayUser = header.findViewById (R.id.displayUser);
        //display user profile on nav__header
        displayUserProfile = header.findViewById (R.id.profilePic);

        //getting current user who signed in through google
        GoogleSignInAccount signInGoogleAcc = GoogleSignIn.getLastSignedInAccount (this);

        //displaying if user is using temporary acc or a legit email account on the nav_header.xml file
        if ( user.isAnonymous ( ) ) {
            //this condition is true if user account is anonymous
            displayUser.setText ("Temporary User");
        }
        if ( !user.isAnonymous ( ) ) {
            //this condition is true if user is not anonymous
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

        if ( signInGoogleAcc != null ) {
            //this condition is true if a google user is authenticated to the app
            displayUser.setText ("Welcome " + signInGoogleAcc.getEmail ( ) + " !");


            //to display user google email profile picture
            //1. get the profile picture from the google email of the current user
            String googlePhotoUrl = signInGoogleAcc.getPhotoUrl ( ).toString ( );

            //2. To display of profile picture, it  can be done with bumptech glide library. Glide supports fetching, decoding, and displaying video stills, images, and animated GIFs.
            Glide.with (getApplicationContext ( )).load (googlePhotoUrl)
                    .thumbnail (0.5f)
                    .crossFade ( )
                    .diskCacheStrategy (DiskCacheStrategy.ALL)
                    .into (displayUserProfile);
        }
        //display normal login user email
        if ( !user.isAnonymous ( ) ) {
            if ( user.getPhotoUrl ( ) != null ) {
                Glide.with (this)
                        .load (user.getPhotoUrl ( ).toString ( ))
                        .into (displayUserProfile);
            }


        }


        //querying from database in  the collection called notes and sorting the order of the notes by titles. this will save notes saved by specific user.
        Query queryData = mfirestore.collection ("notes").document (user.getUid ( )).collection ("userNotes").orderBy ("title", Query.Direction.DESCENDING);

        //Query Notes >userid >query my notes


        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note> ( ) //passing the model class called Notes
                .setQuery (queryData, Note.class)
                .build ( ); //query data from the firebase it is going to be stored in the allNotes variable


        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder> (allNotes) { //data retrieve from the firestore database
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, final int i, @NonNull final Note note) {
//extracting title and the contents from the list and assign to the title and content text view of the note_view_layout
                noteViewHolder.noteTitle.setText (note.getTitle ( )); //getting the title of the note from the note class
                noteViewHolder.noteContent.setText (note.getContent ( )); //getting the contents of the note from the note class
                //   final int code = getRandomColour ();
                //   noteViewHolder.mCardview.setCardBackgroundColor (noteViewHolder.view.getResources ().getColor (code)); //get colour from the colors.xml file by calling the getRandomColour method

                final String docId = noteAdapter.getSnapshots ( ).getSnapshot (i).getId ( );


                noteViewHolder.view.setOnClickListener (new View.OnClickListener ( ) {
                    @Override
                    public void onClick(View v) {
                        //if user click on the note it will go to next screen.
                        Intent i = new Intent (v.getContext ( ), NoteDetailsActivity.class);

                        //passing the data of the  note title,note content from the recycle view to the note details Activity
                        i.putExtra ("title", note.getTitle ( ));
                        i.putExtra ("content", note.getContent ( ));
                        //i.putExtra("code",code);
                        i.putExtra ("noteId", docId);

                        v.getContext ( ).startActivity (i);
                    }
                });


                //delete and edit button in pop up menu for each note
                ImageView menuIcon = noteViewHolder.view.findViewById (R.id.menuIcon);
                menuIcon.setOnClickListener (new View.OnClickListener ( ) {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(final View v) {
                        final String docId = noteAdapter.getSnapshots ( ).getSnapshot (i).getId ( );
                        PopupMenu menu = new PopupMenu ((v.getContext ( )), v);
                        menu.setGravity (Gravity.END);
                        menu.getMenu ( ).add ("Edit").setOnMenuItemClickListener (new MenuItem.OnMenuItemClickListener ( ) {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                //edit btn will go to edit note class
                                Intent i = new Intent (v.getContext ( ), EditNote.class);

                                i.putExtra ("title", note.getTitle ( ));
                                i.putExtra ("content", note.getContent ( ));
                                i.putExtra ("noteId", docId);
                                startActivity (i);
                                return false;
                            }
                        });

                        //when delete btn is clicked the note will be deleted by just deleting the noteid from the user who login(we used getUid to retrieve the current user who is login)
                        menu.getMenu ( ).add ("Delete").setOnMenuItemClickListener (new MenuItem.OnMenuItemClickListener ( ) {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference documentReference = mfirestore.collection ("notes").document (user.getUid ( )).collection ("userNotes").document (docId); //delete note by id

                                documentReference.delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) //this delete method is going to find the note id in the "notes" collection
                                {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //note deleted
                                        DocumentReference documentBookmark = mfirestore.collection ("bookmark").document (user.getUid ( )).collection ("userBookmark").document (docId); //delete note by id
                                        documentBookmark.delete ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) //this delete method is going to find the note id in the "notes" collection
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //note deleted

                                            }

                                        }).addOnFailureListener (new OnFailureListener ( ) {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText (MainActivity.this, "Error", Toast.LENGTH_SHORT).show ( );
                                            }
                                        });

                                    }

                                }).addOnFailureListener (new OnFailureListener ( ) {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText (MainActivity.this, "Error", Toast.LENGTH_SHORT).show ( );
                                    }
                                });

                                return false;


                            }

                        });


                        //favourite
                        final String bTitle = note.getTitle ( );
                        final String bContent = note.getContent ( );

                        menu.getMenu ( ).add ("Like").setOnMenuItemClickListener (new MenuItem.OnMenuItemClickListener ( ) {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference documentref = mfirestore.collection ("bookmark").document (user.getUid ( )).collection ("userBookmark").document (docId);
                                //Map is used to take key value pairs
                                Map<String, Object> note = new HashMap ( );
                                note.put ("bookmarkTitle", bTitle);
                                note.put ("bookmarkContent", bContent);
                                //   note.put("dateExample", new Timestamp (new Date ()));

                                // to see if the note is added successfully, this code automatically creates the collection in the firestore database
                                documentref.set (note).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //if note is added successfully , the toast message below will be displayed
                                        Toast.makeText (MainActivity.this, "Note Bookmarked", Toast.LENGTH_SHORT).show ( );


                                    }
                                }).addOnFailureListener (new OnFailureListener ( ) {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText (MainActivity.this, "Unable bookmark noted. Please try again", Toast.LENGTH_SHORT).show ( );

                                    }
                                });


                                return false;

                            }
                        });

                        menu.getMenu ( ).add ("Alert").setOnMenuItemClickListener (new MenuItem.OnMenuItemClickListener ( ) {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                DialogFragment timePicker = new TimeAlarm ( );
                                timePicker.show (getSupportFragmentManager ( ), "time picker");

                                return false;
                            }


                        });

                        menu.show ( ); //display the edit and delete button in the pop up menu
                    }


                });


            }


            @NonNull
            @Override
            //layout of title and note
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from (parent.getContext ( )).inflate (R.layout.note_view_layout, parent, false);

                return new NoteViewHolder (view);
            }
        };

        noteLists = findViewById (R.id.noteList);


        drawerLayout = findViewById (R.id.drawer);
        nav_view = findViewById (R.id.nav_view);
        //onNavigationItemSelected method is called
        nav_view.setNavigationItemSelectedListener (this);


        //create an object for action bar drawer
        //passing parameters to current context where we want to put our current context, "handburger sign"
        toggle = new ActionBarDrawerToggle (this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener (toggle);


        //enabling the handburger sign in the toolbar
        toggle.setDrawerIndicatorEnabled (true);
        //informing the navigation xml folder that the navigation drawer is closed or open currently
        toggle.syncState ( );


        //displaying the note layout, first parameter is the number of grid displayed.
        noteLists.setLayoutManager (new StaggeredGridLayoutManager (2, StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter (noteAdapter);

        FloatingActionButton fab = findViewById (R.id.addNoteFloat);
        fab.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {

                startActivity (new Intent (nav_view.getContext ( ), AddNote.class));
            }
        });


    }


    private void googleSignOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken (getString (R.string.default_web_client_id))
                .requestEmail ( )       //request user google email
                .build ( );
        //the above requests will be hand in to GoogleSignInClient
        //Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient (this, gso);
        GoogleSignIn.getClient (this, gso).signOut ( );
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //navigation drawer will close once user click on the selected nav menu item.
        drawerLayout.closeDrawer (GravityCompat.START);

        //switch statement is used as there is too many menu items
        switch (item.getItemId ( )) {

            case R.id.allNotes:
                Toast.makeText (this, "You are already in all notes", Toast.LENGTH_SHORT).show ( );
                break;




            case R.id.syncNotes:
                //if user is using an anonymous account then user will go to Register/login activity
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
            //user will go to account activity
            case R.id.accountSettings:
                if ( user.isAnonymous ( ) ) {
                    Toast.makeText (this, "Create an account first", Toast.LENGTH_SHORT).show ( );
                } else {

                    startActivity (new Intent (this, AccountActivity.class));

                }
                break;

            //user will go to SettingsActivity
            case R.id.settingsMenu:
                startActivity (new Intent (this, SettingsActivity.class));
                break;
            case R.id.weather:
                startActivity (new Intent (this, WeatherActivity.class));
                break;
            //when user clicks on logout button, they will be sign out of the firebase authentication as well
            case R.id.signOut:
                //either soft logout
                checkUser ( );

                //or google sign out
                googleSignOut ( );


                break;

        }
        return false;
    }

    //checking if user is sign in by normal email/anonymous
    private void checkUser() {

        if ( user.isAnonymous ( ) ) {
            displayAlert ( );

        } else {
            //if user has a real email account and clicks on the sign out button, they will be directed to the splash logo screen
            FirebaseAuth.getInstance ( ).signOut ( ); //soft logout


            startActivity (new Intent (getApplicationContext ( ), SplashLogoActivity.class));
            finish ( );
        }
    }

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


    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;
        View view;
        CardView mCardview;

        //as parent view here is itemView(parent layout), it is used to access to the variables in the note_view_layout
        public NoteViewHolder(@NonNull View itemView) {

            super (itemView);
            noteTitle = itemView.findViewById (R.id.titles);
            noteContent = itemView.findViewById (R.id.content);
            mCardview = itemView.findViewById (R.id.noteCard);

            view = itemView;//the view here is use to handle when the user click of on the recycle view items.

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance ( );
        c.set (Calendar.HOUR_OF_DAY, hourOfDay);
        c.set (Calendar.MINUTE, minute);
        c.set (Calendar.SECOND, 0);
        updateTimeText (c);
        startAlarm (c);
    }


    //setting alert for notes.
    //different class is required to receive the alert notification
    private void updateTimeText(Calendar c) {
        String timeText = "Alarm set for: ";
        //DateFormat.short will be taken in to account for timings with 24hr or 12 hr format
        timeText += DateFormat.getTimeInstance (DateFormat.SHORT).format (c.getTime ( ));
        // mTextView.setText(timeText);
        Toast.makeText (MainActivity.this, timeText, Toast.LENGTH_LONG).show ( );
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService (Context.ALARM_SERVICE);
        Intent intent = new Intent (this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast (this, 1, intent, 0);
        if ( c.before (Calendar.getInstance ( )) ) {
            c.add (Calendar.DATE, 1);
        }
        alarmManager.setExact (AlarmManager.RTC_WAKEUP, c.getTimeInMillis ( ), pendingIntent);
    }


    //listen for data change from the firestore database which is our note collection
    //essential method
    @Override
    protected void onStart() {
        super.onStart ( );


        noteAdapter.startListening ( );//listen for any changes in the database
    }

    @Override
    protected void onStop() {
        super.onStop ( );
        if ( noteAdapter != null ) {
            noteAdapter.stopListening ( ); //once user exit the application the app will stop listening to firestore database for any changes
        }
    }

}



