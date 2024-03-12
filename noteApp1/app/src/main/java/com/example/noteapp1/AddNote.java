package com.example.noteapp1;

import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddNote extends AppCompatActivity {


    EditText noteTitle,noteContent;


    ProgressBar progressBarSave;


    FirebaseUser user;
    FirebaseFirestore mfirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        } else setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_add_note);
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        getSupportActionBar ().setDisplayHomeAsUpEnabled (true);




        noteContent = findViewById (R.id.addNoteContent);
        noteTitle = findViewById (R.id.addNoteTitle);

        user = FirebaseAuth.getInstance ().getCurrentUser ();
        mfirestore = FirebaseFirestore.getInstance();

        progressBarSave = findViewById (R.id.progressBar);

        FloatingActionButton fab = findViewById (R.id.fab);
       fab.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View view) {
                String nTitle = noteTitle.getText ().toString ();
                String nContent = noteContent.getText ().toString ();


                if(nTitle.isEmpty () || nContent.isEmpty ()){
                    Toast.makeText (AddNote.this,"Unable to save note with empty field",Toast.LENGTH_SHORT).show ();

                    return;

                }

                //display progress bar after note has successfully been added, in the xml layout it has been set to invisible so to display that the user is saving the note successfully. It has to be set to visible here.
                progressBarSave.setVisibility (View.VISIBLE);


                //save note

              DocumentReference documentref = mfirestore.collection ("notes").document(user.getUid ()).collection ("userNotes").document ();
                //Map is used to take key value pairs
                Map<String,Object> note = new HashMap ();
                note.put ("title",nTitle);
                note.put ("search",nTitle.toLowerCase ());
                note.put ("content",nContent);
             //   note.put("dateExample", new Timestamp (new Date ()));

                // to see if the note is added successfully, this code automatically creates the collection in the firestore database
                documentref.set (note).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //if note is added successfully , the toast message below will be displayed
                        Toast.makeText (AddNote.this, "Note Added",Toast.LENGTH_SHORT).show();
                        onBackPressed (); //go back to mainActivity where the notes are displayed

                    }
                }).addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (AddNote.this, "Unable to save note. Please try again",Toast.LENGTH_SHORT).show ();
                       //displaying progress bar
                        progressBarSave.setVisibility (View.VISIBLE);
                    }
                });

            }
        });
    }
    //when user presses the back button this method is called, user will go back to the display notes screen
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId ()== android.R.id.home){

        onBackPressed ();
        }
        return super.onOptionsItemSelected (item);
    }

}
