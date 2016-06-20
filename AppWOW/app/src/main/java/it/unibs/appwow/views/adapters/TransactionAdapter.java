package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.TransactionDAO;
import it.unibs.appwow.models.Transaction;
import it.unibs.appwow.models.parc.LocalUser;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class TransactionAdapter extends BaseAdapter {
    private static final String TAG_LOG = AmountItemAdapter.class.getSimpleName();
    private LocalUser mUser;
    private List<Transaction> mItems = new ArrayList<Transaction>();
    private final LayoutInflater mInflater;
    private TransactionDAO dao;

    private class Holder {
        TextView amount;
        TextView preposition;
        TextView fullName;
    }

    public TransactionAdapter(Context context, int idGroup){
        mInflater = LayoutInflater.from(context);
        dao = new TransactionDAO();
        dao.open();
        mUser = LocalUser.load(MyApplication.getAppContext());
        mItems = dao.getAllTransactionsFrom(idGroup, mUser.getId());
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        final Transaction item = (Transaction) getItem(position);
        return item.getId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.fragment_amount_item, null);
            holder = new Holder();
            holder.fullName = (TextView) view.findViewById(R.id.fragment_transaction_user);
            holder.amount = (TextView)view.findViewById(R.id.fragment_transaction_amount);
            holder.preposition = (TextView) view.findViewById(R.id.fragment_transaction_preposition);

            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }

        final Transaction item = (Transaction) getItem(position);
        holder.amount.setText(String.valueOf(item.getAmount()));
        holder.fullName.setText(item.getFullName());
        // FIXME: 20/06/2016 SOSTITUIRE CON STRING RESOURCES
        if(item.getIdFrom() == mUser.getId()){
            holder.preposition.setText("A");
        } else {
            holder.preposition.setText("DA");
        }


        return view;
    }


}
