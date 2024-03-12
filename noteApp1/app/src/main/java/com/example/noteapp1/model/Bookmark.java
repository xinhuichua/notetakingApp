package com.example.noteapp1.model;

public class Bookmark {
    // variable name here must be the same as the database
    private String bookmarkTitle;
    private String bookmarkContent;
    //private String date;
    public Bookmark(){}
    public Bookmark(String bookmarkTitle, String bookmarkContent){
        this.bookmarkTitle = bookmarkTitle;
        this.bookmarkContent = bookmarkContent;
        //  this.date = date;

    }

    // to be pass to the FirestoreRecycleOptions<> in the MainActivity.Java
    public String getBookmarkTitle()
    {
        return bookmarkTitle; //getting title of the notes from firebase
    }

    public void setBookmarkTitle (String bookmarkTitle)
    {
        this.bookmarkTitle = bookmarkTitle;
    }

    public String getBookmarkContent() {
        return bookmarkContent; //getting content of the note from firebase
    }

    public void setBookmarkContent(String bookmarkContent) {
        this.bookmarkContent = bookmarkContent;
    }


}