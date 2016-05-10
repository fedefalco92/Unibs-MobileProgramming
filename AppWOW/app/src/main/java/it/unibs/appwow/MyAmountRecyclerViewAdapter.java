package it.unibs.appwow;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unibs.appwow.AmountsFragment.OnListFragmentInteractionListener;
import it.unibs.appwow.model.Amount;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Amount} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAmountRecyclerViewAdapter extends RecyclerView.Adapter<MyAmountRecyclerViewAdapter.ViewHolder> {

    private final List<Amount> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyAmountRecyclerViewAdapter(List<Amount> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_amount_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mUserFullname.setText(mValues.get(position).fullname);
        holder.mUserAmount.setText(String.valueOf(mValues.get(position).amount));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUserFullname;
        public final TextView mUserAmount;
        public Amount mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUserFullname = (TextView) view.findViewById(R.id.amount_fragment_user_fullname);
            mUserAmount = (TextView) view.findViewById(R.id.amount_fragment_user_amount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUserAmount.getText() + "'";
        }
    }
}
