package it.unibs.appwow.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unibs.appwow.R;
import it.unibs.appwow.fragments.TransactionsFragment.OnListFragmentInteractionListener;
import it.unibs.appwow.utils.dummy.DummyTransactionContent.Transaction;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Transaction} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder> {

    private static final String TAG_LOG = TransactionRecyclerViewAdapter.class.getSimpleName();

    private final List<Transaction> mValues;
    private final OnListFragmentInteractionListener mListener;

    public TransactionRecyclerViewAdapter(List<Transaction> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mAmountView.setText(String.valueOf(mValues.get(position).amount).substring(0,3));
        holder.mPrepositionView.setText(mValues.get(position).preposition);
        holder.mUserView.setText(mValues.get(position).user);

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
        public final TextView mAmountView;
        public final TextView mPrepositionView;
        public final TextView mUserView;
        public Transaction mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAmountView = (TextView) view.findViewById(R.id.fragmemt_transaction_amount);
            mPrepositionView = (TextView) view.findViewById(R.id.fragment_transaction_preposition);
            mUserView = (TextView) view.findViewById(R.id.fragment_transaction_user);
        }
    }
}
