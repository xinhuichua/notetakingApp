package com.example.noteapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNote extends AppCompatActivity {

    Intent data; //global variable

    EditText editNoteTitle, editNoteContent;

    ProgressBar progressBarTwo;
   FirebaseFirestore mfirestore;
   FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        mfirestore = mfirestore.getInstance ();
        user = FirebaseAuth.getInstance ().getCurrentUser ();

        setContentView (R.layout.activity_edit_note);
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        getSupportActionBar ().setDisplayHomeAsUpEnabled (true);

        data = getIntent ();

        editNoteContent = findViewById (R.id.editNoteContent);
        editNoteTitle = findViewById (R.id.editNoteTitle);

        progressBarTwo = findViewById (R.id.progressBar2);

        String noteTitle = data.getStringExtra("title"); //get the title data and pass to the key to extract the data
        String noteContent = data.getStringExtra("content"); //get the content data and pass to the key to extract the data

        //since the data is stored in the noteTitle and noteContent
        //now we set the noteTitle and noteContent variable to editNoteTitle and editNoteContent.
        editNoteTitle.setText(noteTitle);
        editNoteContent.setText (noteContent);

        FloatingActionButton fab = findViewById (R.id.saveEditedNote);
        fab.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View view) {

                String nTitle = editNoteTitle.getText ().toString ();
                String nContent = editNoteContent.getText ().toString ();


                if(nTitle.isEmpty () || nContent.isEmpty ()){
                    Toast.makeText (EditNote.this,"Unable to save note with empty field",Toast.LENGTH_SHORT).show ();

                    return;

                }

                //display progress bar after note has successfully been added, in the xml layout it has been set to invisible so to display that the user is saving the note successfully. It has to be set to visible here.
                progressBarTwo.setVisibility (View.VISIBLE);




                //update note by noteid
                DocumentReference documentref = mfirestore.collection("notes").document (user.getUid ()).collection ("userNotes").document (data.getStringExtra ("noteId"));

                //Map is used to take key value pairs
                Map<String,Object> note = new HashMap ();
                note.put ("title",nTitle);
                note.put ("search",nTitle.toLowerCase ());
                note.put ("content",nContent);

                // to see if the note is added successfully, this code automatically creates the collection in the firestore database
                documentref.update (note).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //if note is added successfully , the toast message below will be displayed
                        Toast.makeText (EditNote.this, "Note is successfully edited",Toast.LENGTH_SHORT).show();
                        onBackPressed (); //go back to mainActivity where the notes are displayed

                    }
                }).addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (EditNote.this, "Unable to save note. Please try again",Toast.LENGTH_SHORT).show ();
                        //displaying progress bar
                        progressBarTwo.setVisibility (View.VISIBLE);
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
