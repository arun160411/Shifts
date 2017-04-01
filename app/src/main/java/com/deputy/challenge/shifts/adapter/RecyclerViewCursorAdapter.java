package com.deputy.challenge.shifts.adapter;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

/**
 *
 * @param <VH>
 */
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<VH> {
    private static final String TAG = RecyclerViewCursorAdapter.class.getSimpleName();
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;


    public RecyclerViewCursorAdapter(Cursor cursor) {
        setHasStableIds(true);
        swapCursor(cursor);
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindViewHolder(VH holder, Cursor cursor);

    /**
     * Converts recyclerview's bind method to cursor bind method.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            Log.e(TAG,"Data is not valid");
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            Log.e(TAG,"Cursor count not move to position "+position);
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        // call child class's bindViewHolder with cursor
        onBindViewHolder(holder, mCursor);
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIDColumn);
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    protected Cursor getCursor() {
        return mCursor;
    }



    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mDataSetObserver != null) {
                newCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }


    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    };
}