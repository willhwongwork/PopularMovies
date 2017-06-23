package bbr.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Me on 1/17/2017.
 */

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "bbr.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private MovieContract() {}

    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_TITLE = "original_title";

        public static final String COLUMN_PLOT = "plot_synopsis";

        public static final String COLUMN_RATING = "user_rating";

        public static final String COLUMN_POPULARITY = "popularity";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_IMAGE = "image";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        // create content uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();
        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        // for building URIs on insertion
        public static Uri buildMoviesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }




}
