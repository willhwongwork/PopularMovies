package bbr.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    String originalTitle;
    String plotSynopsis;
    double userRating;
    double popularity;
    String releaseDate;
    String image;
    String movie_id;

    public Movie(String title, String plot, double rating, double pop, String date, String image, String id) {
        this.originalTitle = title;
        this.plotSynopsis = plot;
        this.userRating = rating;
        this.popularity = pop;
        this.releaseDate = date;
        this.image = image;
        this.movie_id = id;
    }

    private Movie(Parcel in) {
        originalTitle = in.readString();
        plotSynopsis = in.readString();
        userRating = in.readDouble();
        popularity = in.readDouble();
        releaseDate = in.readString();
        image = in.readString();
        movie_id = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(originalTitle);
        parcel.writeString(plotSynopsis);
        parcel.writeDouble(userRating);
        parcel.writeDouble(popularity);
        parcel.writeString(releaseDate);
        parcel.writeString(image);
        parcel.writeString(movie_id);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };
}
