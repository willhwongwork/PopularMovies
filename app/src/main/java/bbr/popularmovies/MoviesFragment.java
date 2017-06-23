package bbr.popularmovies;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bbr.popularmovies.data.MovieContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private MoviesAdapter mMoviesAdapter;
    private MovieCursorAdapter mCursorAdapter;
    private static final int LOADER_ID = 0;

    static final int COL_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_PLOT = 2;
    static final int COL_RATING = 3;
    static final int COL_POPULARITY = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_IMAGE = 6;
    static final int COL_MOVIE_ID = 7;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie m);
    }



    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie_posters);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_order = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));

        if(sort_order.equals(getString(R.string.pref_sort_favorite))) {
            mCursorAdapter = new MovieCursorAdapter(getActivity(), null, 0);
            gridView.setAdapter(mCursorAdapter);

            Log.d(LOG_TAG, "displaying favorite");

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // CursorAdapter returns a cursor at the correct position for getItem(), or null
                    // if it cannot seek to that position.
                    Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                    if (cursor != null) {
                        Movie m = new Movie(cursor.getString(COL_TITLE), cursor.getString(COL_PLOT),
                                cursor.getDouble(COL_RATING), cursor.getDouble(COL_POPULARITY),
                                cursor.getString(COL_RELEASE_DATE), cursor.getString(COL_IMAGE),
                                cursor.getString(COL_MOVIE_ID));

                        ((Callback)getActivity()).onItemSelected(m);
                    }
                }
            });

        } else{
            mMoviesAdapter = new MoviesAdapter(getActivity(), new ArrayList<Movie>());
            gridView.setAdapter(mMoviesAdapter);

            Log.d(LOG_TAG, sort_order + " is not favorite");

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Movie m = mMoviesAdapter.getItem(position);
                    ((Callback)getActivity()).onItemSelected(m);
                }
            });
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_order = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));

        if(sort_order.equals(getString(R.string.pref_sort_favorite))){
            //nothing happen
            return;

        }else{
            FetchPostersTask postersTask = new FetchPostersTask();
            Log.d(LOG_TAG, "Pref value: " + sort_order);
            postersTask.execute(sort_order);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_order = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));
        if(sort_order.equals(getString(R.string.pref_sort_favorite))) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
            Log.d(LOG_TAG, "init Loader");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }

    public class FetchPostersTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchPostersTask.class.getSimpleName();

        private List<Movie> getMovieDataFromJson(String moviesJsonStr) throws JSONException {

            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_PLOT = "overview";
            final String MDB_RATING = "vote_average";
            final String MDB_POPULARITY = "popularity";
            final String MDB_DATE = "release_date";
            final String MDB_IMAGE = "poster_path";
            final String MDB_MOVIE_ID = "id";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray results = moviesJson.getJSONArray(MDB_RESULTS);

            final Movie[] resultMovies = new Movie[results.length()];

            for (int i = 0; i < results.length(); i++) {
                String title;
                String plot;
                double rating;
                double popularity;
                String date;
                String image;
                String id;

                JSONObject aMovie = results.getJSONObject(i);
                title = aMovie.getString(MDB_TITLE);
                plot = aMovie.getString(MDB_PLOT);
                rating = aMovie.getDouble(MDB_RATING);
                popularity = aMovie.getDouble(MDB_POPULARITY);
                date = aMovie.getString(MDB_DATE);
                image = aMovie.getString(MDB_IMAGE);
                id = aMovie.getString(MDB_MOVIE_ID);

                Movie m = new Movie(title, plot, rating, popularity, date, image, id);
                resultMovies[i] = m;
            }

            List resultMoviesList = Arrays.asList(resultMovies);

            return resultMoviesList;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;

            try {
                final String MOVIES_BASE_URL;
                if (params[0].equals(getString(R.string.pref_sort_popularity))) {
                    MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
                } else {
                    MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
                }

                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();


                Log.d(LOG_TAG, builtUri.toString());
                URL url = new URL(builtUri.toString());


                // Create the request, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the data.
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            if (result != null) {
                mMoviesAdapter.clear();
                for (Movie m : result) {
                    mMoviesAdapter.add(m);
                }
            }
        }
    }
}

