/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2014 skyfish.jy@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.techteam.decider.gui.adapters;
 
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
    private boolean mManualCursorMoving;

    public CursorRecyclerViewAdapter(Context context, Cursor cursor, boolean manualCursorMoving) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
        mManualCursorMoving = manualCursorMoving;
    }

    public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        this(context, cursor, false);
    }

    public CursorRecyclerViewAdapter(Cursor cursor) {
        this(null, cursor, false);
    }

    public CursorRecyclerViewAdapter(Cursor cursor, boolean manualCursorMoving) {
        this(null, cursor, manualCursorMoving);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public boolean isManualCursorMoving() {
        return mManualCursorMoving;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor, int position);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
//        if (!mDataValid) {
//            throw new IllegalStateException("this should only be called when the cursor is valid");
//        }
        if (mDataValid) {
            if (!mManualCursorMoving && !mCursor.moveToPosition(position)) {
            }
            onBindViewHolder(viewHolder, mCursor, position);
        }
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(android.database.Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        Cursor c = swapCursorInternal(newCursor);
        notifyChange(null);
        return c;
    }

    public Cursor swapCursor(Cursor newCursor, Integer position) {
        Cursor c = swapCursorInternal(newCursor);
        notifyChange(position);
        return c;
    }

    public Cursor swapCursor(Cursor newCursor, Integer positionStart, Integer count) {
        Cursor c = swapCursorInternal(newCursor);
        notifyChange(positionStart, count);
        return c;
    }

    private Cursor swapCursorInternal(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }

        return oldCursor;
    }

    private void notifyChange(Integer position) {
        if (position != null) {
            notifyItemChanged(position);
        } else {
            notifyDataSetChanged();
        }
    }

    private void notifyChange(Integer positionStart, Integer count) {
        notifyItemRangeInserted(positionStart, count);
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}