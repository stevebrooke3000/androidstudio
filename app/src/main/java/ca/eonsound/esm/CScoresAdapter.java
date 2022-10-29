package ca.eonsound.esm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CScoresAdapter extends RecyclerView.Adapter<CScoresAdapter.ViewHolder> {
    private List<CScore> listScores;
    private static IClickListener onclicklistener;

    public interface IClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
        void onSwipe(int position/*, View v*/);
    }

    public void setOnItemClickListener(IClickListener onclicklistener) {
        CScoresAdapter.onclicklistener = onclicklistener;
    }

    // Pass in the contact array into the constructor
    public CScoresAdapter(List<CScore> contacts) {
        listScores = contacts;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, View.OnLongClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView viewScore;
        TextView viewTstamp;
        TextView viewPlaytime;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        private ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            viewScore = itemView.findViewById(R.id.tvScoreValue);
            viewTstamp = itemView.findViewById(R.id.tvScoreTimestamp);
            viewPlaytime = itemView.findViewById(R.id.tvScorePlaytime);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onclicklistener.onItemClick(getLayoutPosition(), v);
         }

        @Override
        public boolean onLongClick(View v) {
            onclicklistener.onItemLongClick(getLayoutPosition(), v);
            return true;
        }

        public void vSetLocked(View v, boolean bLock) {
            if (bLock)
                v.setBackgroundColor(v.getContext().getResources().getColor(android.R.color.holo_orange_light, v.getContext().getTheme()));
            else
                v.setBackgroundColor(v.getContext().getResources().getColor(android.R.color.background_light, v.getContext().getTheme()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
// Create a new view, which defines the UI of the list item
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.listitem_score, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        CScore score = listScores.get(position);

        // Set item views based on your views and data model
        holder.viewScore.setText(score.strScore);
        holder.viewTstamp.setText(score.strTstamp);
        holder.viewPlaytime.setText(score.strPlaytime);

        View v = holder.itemView;
        if (score.bIsLocked())
            v.setBackgroundColor(v.getContext().getResources().getColor(android.R.color.holo_orange_light, v.getContext().getTheme()));
        else
            v.setBackgroundColor(v.getContext().getResources().getColor(android.R.color.background_light, v.getContext().getTheme()));
    }

    @Override
    public int getItemCount() {
        return listScores.size();
    }

    /*
    public void deleteItem(int position) {
        listScores.remove(position);
        notifyItemRemoved(position);
    }

     */

    public void onSwiped(final int position) {
        onclicklistener.onSwipe(position);
    }

}

