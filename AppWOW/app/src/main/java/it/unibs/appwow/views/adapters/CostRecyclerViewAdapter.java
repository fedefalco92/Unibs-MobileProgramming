package it.unibs.appwow.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unibs.appwow.fragments.CostsFragment.OnListFragmentInteractionListener;
import it.unibs.appwow.R;
import it.unibs.appwow.models.CostDummy;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CostDummy} and makes a call to the
 * specified {@link OnCostListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CostRecyclerViewAdapter extends RecyclerView.Adapter<CostRecyclerViewAdapter.ViewHolder> {
    private static final String TAG_LOG = CostRecyclerViewAdapter.class.getSimpleName();

    private final List<CostDummy> mValues;
    private final OnListFragmentInteractionListener mListener; // FIXME: 06/05/2016  DA IMPLEMENTARE 

    public CostRecyclerViewAdapter(List<CostDummy> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_cost_item, parent, false);
                // FIXME: 07/05/2016 PRIMA qui sopra c'era R.layout.fragment_cost_list...Why?
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d(TAG_LOG,"Position " + position);

        holder.mItem = mValues.get(position);
        holder.mUsername.setText(Long.toString(mValues.get(position).idUser));
        holder.mCostName.setText(mValues.get(position).name);
        holder.mDate.setText(CostDummy.DATE_FORMAT.format(mValues.get(position).createdAt));
        holder.mItemValue.setText(Double.toString(mValues.get(position).amount));


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
        public final TextView mUsername;
        public final TextView mCostName;
        public final TextView mItemValue;
        public final TextView mDate;
        public CostDummy mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsername = (TextView) view.findViewById(R.id.cost_fragment_item_username);
            mCostName = (TextView) view.findViewById(R.id.cost_fragment_item_costname);
            mItemValue = (TextView) view.findViewById(R.id.cost_fragment_item_value);
            mDate = (TextView) view.findViewById(R.id.cost_fragment_item_date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsername.getText() + "'";
        }
    }
/*
    public class OnCostListFragmentInteractionListener implements OnListFragmentInteractionListener{
        @Override
        public void onListFragmentInteraction(CostDummy item) {

        }
    }*/
}
