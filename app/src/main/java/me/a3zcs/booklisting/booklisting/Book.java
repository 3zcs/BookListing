package me.a3zcs.booklisting.booklisting;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by 3zcs on 21/07/17.
 */

public class Book implements Parcelable{
    private String name;
    private List<String> author;

    public Book() {
    }

    public Book(String name, List<String> author) {
        this.name = name;
        this.author = author;
    }

    protected Book(Parcel in) {
        name = in.readString();
        author = in.createStringArrayList();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        String authors = "";
        for (String s: author) {
            authors = s + ",";
        }
        return authors.substring(0,authors.length()-1);
    }

    public void setAuthor(List<String> author) {
        this.author = author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeStringList(author);
    }
}
