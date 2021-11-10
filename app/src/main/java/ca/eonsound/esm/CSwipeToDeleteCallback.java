package ca.eonsound.esm;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ca.eonsound.esm.CScoresAdapter;

public class CSwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private CScoresAdapter myAdapter;

    public CSwipeToDeleteCallback(CScoresAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        myAdapter = adapter;
    }

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        myAdapter.onSwiped(position);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
}
