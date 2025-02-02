package com.example.romero_andresimdbappp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    // Atributos
    private String id;
    private String title;
    private String imageUrl;
    private String releaseYear;
    private String rating;
    private String overview;
    private String genreId;
    //Constructor vacio
    public Movie() {
    }
    protected Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        releaseYear = in.readString();
        rating = in.readString();
        overview = in.readString();
        genreId = in.readString();

    }
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(releaseYear);
        dest.writeString(rating);
        dest.writeString(overview);
        dest.writeString(genreId);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

}