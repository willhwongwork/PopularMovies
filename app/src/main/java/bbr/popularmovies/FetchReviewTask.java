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

/**
 * Created by Me on 1/28/2017.
 */

public class FetchReviewTask extends AsyncTask<String, Void, List<Review>> {

    private final String LOG_TAG = FetchReviewTask.class.getSimpleName();
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;

    public FetchReviewTask (List<Review> r, ReviewAdapter adapter) {
        reviews = r;
        reviewAdapter = adapter;
    }

    private List<Review> getTrailerDataFromJson (String reviewJsonStr) throws JSONException {

        final String MDB_RESULTS = "results";
        final String MDB_AUTHOR = "author";
        final String MDB_CONTENT = "content";
        final String MDB_URL = "url";

        JSONObject reviewJson = new JSONObject(reviewJsonStr);
        JSONArray results = reviewJson.getJSONArray(MDB_RESULTS);

        final Review[] resultReviews = new Review[results.length()];

        for(int i = 0; i < results.length(); i++) {

            JSONObject aReview = results.getJSONObject(i);
            String author = aReview.getString(MDB_AUTHOR);
            String content = aReview.getString(MDB_CONTENT);
            String url = aReview.getString(MDB_URL);


            Review r = new Review(author, content, url);
            resultReviews[i] = r;
        }

        List resultReviewsList = Arrays.asList(resultReviews);

        return resultReviewsList;

    }


    @Override
    protected List<Review> doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String reviewJsonStr = null;

        String language = "en-US";
        int page = 1;

        try {
            final String TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/reviews?";

            final String API_KEY_PARAM = "api_key";
            final String LANGUAGE_PARAM = "language";
            final String PAGE_PARAM = "page";

            Uri builtUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .appendQueryParameter(LANGUAGE_PARAM, language)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
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
            reviewJsonStr = buffer.toString();
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
            return getTrailerDataFromJson(reviewJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the data.
        return null;
    }

    @Override
    protected void onPostExecute(List<Review> result) {
        if(result != null) {
            int curSize = reviewAdapter.getItemCount();
            reviews.addAll(result);
            reviewAdapter.notifyItemRangeInserted(curSize, result.size());
        }
    }
}
