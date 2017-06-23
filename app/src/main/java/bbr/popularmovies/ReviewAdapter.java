package bbr.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> r) {
        reviews = r;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.reviewNameView.setText(reviews.get(position).author);
        holder.reviewView.setText(reviews.get(position).content);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView reviewNameView;
        private final TextView reviewView;

        public ViewHolder (View view) {
            super(view);
            mView = view;
            reviewNameView = (TextView) view.findViewById(R.id.item_review_name);
            reviewView = (TextView) view.findViewById(R.id.item_review);
        }
    }
}
