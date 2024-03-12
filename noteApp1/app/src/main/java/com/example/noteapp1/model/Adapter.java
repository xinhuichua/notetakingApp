package com.example.noteapp1.model;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteapp1.NoteDetailsActivity;

import com.example.noteapp1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    List<String> titles; //to store title of note
    List<String> content; //to store note content


    public Adapter(List<String> title,List<String>content){ //receive the data from the main activity and assign to the titles and content

        this.titles = title;
        this.content = content;


    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.note_view_layout,parent,false);//create new view for recycleview holder
        return new ViewHolder (view); //create a new viewholder to pass down the variable view as a parameter
    }

    @Override
    //to assign the data to the recycle view to display the notes data/content
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //extracting title and the contents from the list and assign to the title and content text view of the note_view_layout
        holder.noteTitle.setText (titles.get (position));
        holder.noteContent.setText (content.get (position));

        final int code = getRandomColour ();
        holder.mCardview.setCardBackgroundColor (holder.view.getResources ().getColor (code)); //get colour from the colors.xml file by calling the getRandomColour method

        holder.view.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                //if user click on the note it will go to next screen.
                Intent i = new Intent (v.getContext (), NoteDetailsActivity.class);

                //passing the data of the  note title,note content from the recycle view to the note details Activity
                i.putExtra ("title",titles.get(position));
                i.putExtra ("content",content.get (position));
                i.putExtra("code",code);
                v.getContext().startActivity (i);
            }
        });

    }

    //method is used to allow the cardView to store the notes, to change colours
    private int getRandomColour() {
        //
        List<Integer> colourCode = new ArrayList<> ();
        //get colors from the color.xml file

        colourCode.add(R.color.blue);
        colourCode.add (R.color.yellow);
        colourCode.add(R.color.skyblue);
        colourCode.add(R.color.lightPurple);
        colourCode.add (R.color.lightGreen);
        colourCode.add (R.color.gray);
        colourCode.add(R.color.pink);
        colourCode.add(R.color.red);
        colourCode.add(R.color.greenlight);
        colourCode.add (R.color.notgreen);

        Random randomColor = new Random();
        int number = randomColor.nextInt (colourCode.size());
        return colourCode.get(number);
    }



    @Override
    public int getItemCount()
    {
        //displaying the correct number of notes in the recycle view
        return titles.size ();

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle,noteContent;
        View view;
        CardView mCardview;
        //as parent view here is itemView(parent layout), it is used to access to the variables in the note_view_layout
        public ViewHolder(@NonNull View itemView) {
            super (itemView);
            noteTitle = itemView.findViewById (R.id.titles);
            noteContent = itemView.findViewById (R.id.content);
            mCardview = itemView.findViewById (R.id.noteCard);

            view = itemView;//the view here is use to handle when the user click of on the recycle view items.

        }
    }
}
