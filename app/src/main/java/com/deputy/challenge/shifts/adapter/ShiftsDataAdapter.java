package com.deputy.challenge.shifts.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deputy.challenge.shifts.R;
import com.deputy.challenge.shifts.data.model.Shift;
import com.deputy.challenge.shifts.util.CursorUtils;
import com.deputy.challenge.shifts.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import static android.provider.BaseColumns._ID;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_TIMESTAMP;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_IMAGE_URL;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_TIMESTAMP;

/**
 * Created by akatta on 3/30/17.
 */
public final class ShiftsDataAdapter extends RecyclerViewCursorAdapter<ShiftsDataAdapter.ShiftViewHolder> {
    private static final String TAG = ShiftsDataAdapter.class.getSimpleName();
    private final Context mContext;
    // Sort order:  Descending, by date.
    private final WeakReference<ShiftsItemClickListener> mItemClickListener;

    public ShiftsDataAdapter(Context context, ShiftsItemClickListener shiftsItemClickListener) {
        // super will null. will be swapping cursor later.
        super(null);
        mContext = context;
        mItemClickListener = new WeakReference<ShiftsItemClickListener>(shiftsItemClickListener);
    }

    @Override
    public ShiftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shift_item, parent, false);
        return new ShiftViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(ShiftViewHolder holder, Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(_ID));
        String imagePath = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
        long fromTimestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_START_TIMESTAMP));
        long endTimestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_END_TIMESTAMP));

        if(endTimestamp==0){
            // if Shift is in progress, make it standout.
            holder.mTitleView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            holder.mTitleView.setText("Shift in progress...\nclick on \"check\" icon to finish.");
        }else {
            // for better readability
            long millis = endTimestamp - fromTimestamp;
            String duration = String.format("%d min",
                    TimeUnit.MILLISECONDS.toMinutes(millis));
            holder.mTitleView.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            holder.mTitleView.setText(TimeUtil.convertTimeStampToDayString(fromTimestamp)+" for "+duration);
        }
        Picasso.with(mContext)
                .load(imagePath + "#" + id) // for better caching
                .resize((int) mContext.getResources().getDimension(R.dimen.image_width), (int) mContext.getResources().getDimension(R.dimen.image_height))
                .error(R.drawable.deputy)
                .placeholder(R.drawable.deputy)
                .into(holder.mImageView);
    }

    public Shift getItemAt(int position){
        if(getCursor().moveToPosition(position)){
            return CursorUtils.getShift(getCursor());
        }else{
            return null;
        }
    }



    public class ShiftViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTitleView;

        public ShiftViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.shift_icon);
            mTitleView = (TextView) view.findViewById(R.id.shift_title);
            view.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            getCursor().moveToPosition(adapterPosition);
            ShiftsItemClickListener shiftsItemClickListener = mItemClickListener.get();
            if (shiftsItemClickListener != null) {
                Shift clickedShift = CursorUtils.getShift(getCursor());
                shiftsItemClickListener.onListItemClick(clickedShift);
            }
        }
    }


    public interface ShiftsItemClickListener {
        void onListItemClick(Shift shift);
    }


}