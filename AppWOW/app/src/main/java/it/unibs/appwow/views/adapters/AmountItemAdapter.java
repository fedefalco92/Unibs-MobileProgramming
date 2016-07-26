package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.AmountComparator;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class AmountItemAdapter extends BaseAdapter {
    private static final String TAG_LOG = AmountItemAdapter.class.getSimpleName();

    private List<Amount> mItems = new ArrayList<Amount>();
    private final LayoutInflater mInflater;
    private GroupDAO dao;
    private int mLocalUserId;

    private class Holder {
        TextView fullName;
        TextView amount;
        TextView email;
    }

    public AmountItemAdapter(Context context, int idGroup, int localUserId){
        mInflater = LayoutInflater.from(context);
        mLocalUserId = localUserId;
        dao = new GroupDAO();
        dao.open();
        mItems = dao.getAllAmounts(idGroup);
        Collections.sort(mItems, new AmountComparator(mLocalUserId));
        Collections.reverse(mItems);
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
        final Amount item = (Amount) getItem(position);
        return item.getUserId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final Amount item = (Amount) getItem(position);
        boolean isLocalUser = ((item.getUserId()== mLocalUserId) ? true:false);
        Holder holder = null;
        if(view==null) {
            holder = new Holder();
            if(isLocalUser) {
                view = mInflater.inflate(R.layout.fragment_amount_item_local_user,null);
                holder.fullName = (TextView) view.findViewById(R.id.amount_fragment_local_user_fullname);
                holder.amount = (TextView)view.findViewById(R.id.amount_fragment_local_user_amount);
            } else {
                view = mInflater.inflate(R.layout.fragment_amount_item,null);
                holder.fullName = (TextView) view.findViewById(R.id.amount_fragment_user_fullname);
                holder.amount = (TextView)view.findViewById(R.id.amount_fragment_user_amount);
                holder.email = (TextView) view.findViewById(R.id.amount_fragment_user_email);
            }
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }

        if(!isLocalUser){
            holder.fullName.setText(item.getFullName());
            holder.email.setText(item.getEmail());
        }
        //Currency curr = Currency.getInstance("EUR");
        //holder.amount.setText(curr.getSymbol() + " " + item.getAmountString());
        holder.amount.setText(Amount.getAmountStringCurrency(item.getAmount(),"EUR"));

        //colore
        if(item.getAmount() >0){
            holder.amount.setTextColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.green));
        } else if(item.getAmount() <0){
            holder.amount.setTextColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.red));
        } else{
            holder.amount.setTextColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.black));
        }
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(mItems, new AmountComparator(mLocalUserId));
        Collections.reverse(mItems);
        super.notifyDataSetChanged();
    }
}
