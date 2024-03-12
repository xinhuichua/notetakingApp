package com.example.noteapp1.model;

public class Note {
    // variable name here must be the same as the database
    private String title;
    private String content;
    private String search;
    //private String date;
    public Note(){}
    public Note(String title,String content){
        this.title = title;
        this.content = content;
      //  this.date = date;

    }

    // to be pass to the FirestoreRecycleOptions<> in the MainActivity.Java
    public String getTitle()
    {
        return title; //getting title of the notes from firebase
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content; //getting content of the note from firebase
    }

    public void setContent(String content)
    {
        this.content = content;
    }
    public String getSearch()
    {
        return search; //getting content of the note from firebase
    }
    public void setSearch(String search)
    {
       this.search = search; //getting content of the note from firebase
    }


   /* public String getDate() {
        return date; //getting content of the note from firebase
    }

    public void setDate(String date) {
        this.date = date;
    }*/
}
