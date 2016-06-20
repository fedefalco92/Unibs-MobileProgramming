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

import it.unibs.appwow.R;
import it.unibs.appwow.database.AppDB;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.AmountsFragment;
import it.unibs.appwow.models.Amount;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class AmountItemAdapter extends BaseAdapter {
    private static final String TAG_LOG = AmountItemAdapter.class.getSimpleName();

    private List<Amount> mItems = new ArrayList<Amount>();
    private final LayoutInflater mInflater;
    private GroupDAO dao;

    private class Holder {
        TextView fullName;
        TextView amount;
    }

    public AmountItemAdapter(Context context, int idGroup){
        mInflater = LayoutInflater.from(context);
        dao = new GroupDAO();
        dao.open();
        mItems = dao.getAllAmounts(idGroup);
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
        return item.id;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.fragment_amount_item,null);
            holder = new Holder();
            holder.fullName = (TextView) view.findViewById(R.id.amount_fragment_user_fullname);
            holder.amount = (TextView)view.findViewById(R.id.amount_fragment_user_amount);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }

        final Amount item = (Amount) getItem(position);
        holder.fullName.setText(item.fullName);
        holder.amount.setText(""+item.amount);

        return view;
    }


}
