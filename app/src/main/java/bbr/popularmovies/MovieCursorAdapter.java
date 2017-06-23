package bbr.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import bbr.popularmovies.data.MovieContract;

import static bbr.popularmovies.data.MovieDbHelper.LOG_TAG;


public class MovieCursorAdapter extends CursorAdapter {

    private Context mContext;


    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        final ViewHolder holder = new MovieCursorAdapter.ViewHolder();
        holder.posterView = (ImageView) view.findViewById(R.id.item_poster);
        holder.titleView = (TextView) view.findViewById(R.id.item_title);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        int posterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE);
        String image = cursor.getString(posterIndex);

        final String MOVIE_BASE_URL = "https://image.tmdb.org/t/p/";
        final String SIZE = "w185";
        final String POSTER_PATH = image.substring(1);

        Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(SIZE)
                .appendEncodedPath(POSTER_PATH)
                .build();

        Log.d(LOG_TAG, "Uri: " + uri);


        Picasso.with(mContext).setLoggingEnabled(true);
        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_placeholder)
                .into(holder.posterView);

        int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        String title = cursor.getString(titleIndex);
        holder.titleView.setText(title);

    }

    static class ViewHolder {
        TextView titleView;
        ImageView posterView;
    }
}
