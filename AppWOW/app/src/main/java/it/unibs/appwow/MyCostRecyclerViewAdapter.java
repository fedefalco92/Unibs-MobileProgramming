package it.unibs.appwow;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unibs.appwow.CostsFragment.OnListFragmentInteractionListener;
import it.unibs.appwow.dummy.DummyContent.Cost;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Cost} and makes a call to the
 * specified {@link OnCostListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyCostRecyclerViewAdapter extends RecyclerView.Adapter<MyCostRecyclerViewAdapter.ViewHolder> {

    private final List<Cost> mValues;
    private final OnListFragmentInteractionListener mListener; // FIXME: 06/05/2016  DA IMPLEMENTARE 

    public MyCostRecyclerViewAdapter(List<Cost> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_cost, parent, false);
                // FIXME: 07/05/2016 PRIMA qui sopra c'era R.layout.fragment_cost_list...Why?
        //Log.d("AAA","here");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //Log.d("MyCostRecyclerViewAdapter","Position " + position);

        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);

        holder.mContentView.setText(mValues.get(position).content);

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
        public final TextView mIdView;
        public final TextView mContentView;
        public Cost mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            //Log.d("AAA",mIdView.getId()+"");
            //Log.d("AAA2",mContentView.getId()+"");
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
/*
    public class OnCostListFragmentInteractionListener implements OnListFragmentInteractionListener{
        @Override
        public void onListFragmentInteraction(Cost item) {

        }
    }*/
}
