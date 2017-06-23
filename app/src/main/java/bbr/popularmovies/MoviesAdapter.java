package bbr.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends ArrayAdapter<Movie> {

    private final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate
     *
     * @param context The current context. Used to inflate the layout file.
     * @param Moives  A List of Moive objects to display
     */
    public MoviesAdapter(Activity context, List<Movie> Moives) {
        super(context, 0, Moives);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);
        final ViewHolder holder;

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_movie, parent, false);
            holder = new ViewHolder();
            holder.poster = (ImageView) convertView.findViewById(R.id.item_poster);
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //ImageView posterView = (ImageView) convertView.findViewById(R.id.item_poster);

        final String MOVIE_BASE_URL = "https://image.tmdb.org/t/p/";
        final String SIZE = "w185";
        final String POSTER_PATH = movie.image.substring(1);

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
                .into(holder.poster);

        //TextView titleView = (TextView) convertView.findViewById(R.id.item_title);
        holder.title.setText(movie.originalTitle);

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        ImageView poster;
    }
}
