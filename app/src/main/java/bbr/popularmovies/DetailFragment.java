package bbr.popularmovies;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import bbr.popularmovies.data.MovieContract;

public class DetailFragment extends Fragment {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    static final String DETAIL_MOVIE = "MOVIE";

    private Movie mMovie;
    private List<Trailer> trailers = new ArrayList<Trailer>();
    private TrailerAdapter trailerAdapter = new TrailerAdapter(trailers);
    private List<Review> reviews = new ArrayList<Review>();
    private ReviewAdapter reviewAdapter = new ReviewAdapter(reviews);

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();

        if (arguments != null) {
            mMovie = (Movie) arguments.getParcelable(DetailFragment.DETAIL_MOVIE);

            ((TextView) rootView.findViewById(R.id.detail_title))
                    .setText(mMovie.originalTitle);

            ((TextView) rootView.findViewById(R.id.detail_date))
                    .setText(mMovie.releaseDate);

            ((TextView) rootView.findViewById(R.id.detail_rating))
                    .setText(Double.toString(mMovie.userRating));

            ((TextView) rootView.findViewById(R.id.detail_synopsis))
                    .setText(mMovie.plotSynopsis);

            ImageView posterView = (ImageView) rootView.findViewById(R.id.detail_poster);
            final String MOVIE_BASE_URL = "https://image.tmdb.org/t/p/";
            final String SIZE = "w185";
            final String POSTER_PATH = mMovie.image.substring(1);

            Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(SIZE)
                    .appendEncodedPath(POSTER_PATH)
                    .build();

            Log.d(LOG_TAG, "Uri: " + uri);

            Picasso.with(getContext()).setLoggingEnabled(true);
            Picasso.with(getContext())
                    .load(uri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_placeholder)
                    .into(posterView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();
                        }
                    });

            CheckBox star = (CheckBox) rootView.findViewById(R.id.favorite);
            Cursor c = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{MovieContract.MovieEntry.COLUMN_TITLE},
                    MovieContract.MovieEntry.COLUMN_TITLE + "=?",
                    new String[]{mMovie.originalTitle},
                    null);
            if(c.getCount() != 0) {
                star.setChecked(true);
            }
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((CheckBox) view).isChecked()) {
                        insertData(mMovie);
                        Log.d(LOG_TAG, " insertedData");
                    }else {
                        deleteData(mMovie);
                        Log.d(LOG_TAG, " deletedData");
                    }
                }
            });
        }

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.trailers_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(trailerAdapter);

        RecyclerView rvReview = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
        rvReview.setLayoutManager(new LinearLayoutManager(rvReview.getContext()));
        rvReview.setAdapter(reviewAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(trailers.size() == 0 && mMovie != null) {
            FetchTrailerTask trailerTask = new FetchTrailerTask(trailers, trailerAdapter);
            trailerTask.execute(mMovie.movie_id);
        }
        if(reviews.size() == 0 && mMovie != null) {
            FetchReviewTask reviewTask = new FetchReviewTask(reviews, reviewAdapter);
            reviewTask.execute(mMovie.movie_id);
        }
    }

    private void insertData(Movie m) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, m.originalTitle);
        values.put(MovieContract.MovieEntry.COLUMN_IMAGE, m.image);
        values.put(MovieContract.MovieEntry.COLUMN_PLOT, m.plotSynopsis);
        values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, m.popularity);
        values.put(MovieContract.MovieEntry.COLUMN_RATING, m.userRating);
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, m.releaseDate);
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, m.movie_id);


        getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,values);
    }

    private void deleteData(Movie m) {
        String selection = MovieContract.MovieEntry.COLUMN_TITLE + " = ?";
        String[] selectionArgs = {m.originalTitle};
        getActivity().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, selection, selectionArgs);
    }


}
