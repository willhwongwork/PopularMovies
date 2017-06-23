package bbr.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class FetchTrailerTask extends AsyncTask<String, Void, List<Trailer>> {

    private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();
    private List<Trailer> trailers;
    private TrailerAdapter trailerAdapter;

    public FetchTrailerTask (List<Trailer> t, TrailerAdapter adapter) {
        trailers = t;
        trailerAdapter = adapter;
    }

    private List<Trailer> getTrailerDataFromJson (String trailerJsonStr) throws JSONException {

        final String MDB_RESULTS = "results";
        final String MDB_KEY = "key";
        final String MDB_NAME = "name";
        final String MDB_SITE = "site";
        final String MDB_TYPE = "type";

        JSONObject trailerJson = new JSONObject(trailerJsonStr);
        JSONArray results = trailerJson.getJSONArray(MDB_RESULTS);

        final Trailer[] resultTrailers = new Trailer[results.length()];

        for(int i = 0; i < results.length(); i++) {

            JSONObject aTrailer = results.getJSONObject(i);
            String key = aTrailer.getString(MDB_KEY);
            String name = aTrailer.getString(MDB_NAME);
            String site = aTrailer.getString(MDB_SITE);
            String type = aTrailer.getString(MDB_TYPE);

            Trailer t = new Trailer(key, name, site, type);
            resultTrailers[i] = t;
        }

        List resultTrailersList = Arrays.asList(resultTrailers);

        return resultTrailersList;

    }


    @Override
    protected List<Trailer> doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String trailerJsonStr = null;

        String language = "en-US";

        try {
            final String TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/videos?";

            final String API_KEY_PARAM = "api_key";
            final String LANGUAGE_PARAM = "language";

            Uri builtUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .appendQueryParameter(LANGUAGE_PARAM, language)
                    .build();

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
            trailerJsonStr = buffer.toString();
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
            return getTrailerDataFromJson(trailerJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the data.
        return null;
    }

    @Override
    protected void onPostExecute(List<Trailer> result) {
        if(result != null) {
            int curSize = trailerAdapter.getItemCount();
            trailers.addAll(result);
            trailerAdapter.notifyItemRangeInserted(curSize, result.size());
        }
    }
}

