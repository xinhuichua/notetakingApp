package com.example.noteapp1;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class NoteDetailsActivity extends AppCompatActivity {
    Intent data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getting dark mode to be implemented into this activity
        final SharedPref sharedPref;
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);

        } else setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate (savedInstanceState);
        setContentView (R.layout.note_details_activity);
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        getSupportActionBar ().setDisplayHomeAsUpEnabled (true); //makes the back navigation icon clickable

        getSupportActionBar().setDisplayShowTitleEnabled(false); //the title of the activity will not be displayed
        //receiving the data from the mainactivity

      data = getIntent ();


        //textview for details of the note
        TextView content = findViewById (R.id.noteDetailsContent);

        //textview for note title
        TextView title = findViewById (R.id.noteDetailsTitle);


        //to allow scrolling movement on the textview which store the note details
        content.setMovementMethod (new ScrollingMovementMethod ());


        //getting the note title and note content from the recycle view in the main activity
        content.setText (data.getStringExtra ("content"));
        title.setText (data.getStringExtra ("title"));




        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View view) {

                //passing the note title and contents to editNote class
                Intent i = new Intent (view.getContext (),EditNote.class);

                i.putExtra ("title",data.getStringExtra ("title"));
                i.putExtra ("content",data.getStringExtra ("content"));
                i.putExtra ("noteId",data.getStringExtra ("noteId"));
                startActivity (i);
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
