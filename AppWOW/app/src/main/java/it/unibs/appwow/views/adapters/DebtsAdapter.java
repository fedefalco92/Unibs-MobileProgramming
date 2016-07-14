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
import it.unibs.appwow.database.DebtDAO;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.DebtModel;
import it.unibs.appwow.models.parc.LocalUser;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class DebtsAdapter extends BaseAdapter {
    private static final String TAG_LOG = DebtsAdapter.class.getSimpleName();
    private LocalUser mUser;
    private List<Debt> mItems = new ArrayList<Debt>();
    private final LayoutInflater mInflater;
    private DebtDAO dao;
    private boolean mShowOnlyYourDebts;
    private int mIdGroup;

    private class Holder {
        TextView amount;
        TextView fullNameFrom;
        TextView fullNameTo;
        TextView emailFrom;
        TextView emailTo;
    }

    public DebtsAdapter(Context context, int idGroup, boolean showOnlyYourDebts){
        mInflater = LayoutInflater.from(context);
        mUser = LocalUser.load(context);
        mShowOnlyYourDebts = showOnlyYourDebts;
        mIdGroup = idGroup;
        dao = new DebtDAO();
        dao.open();
        //mUser = LocalUser.load(MyApplication.getAppContext());
        mItems = dao.getAllDebtsExtra(mIdGroup, mShowOnlyYourDebts, mUser.getId());
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
        final DebtModel item = (DebtModel) getItem(position);
        return item.getId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.fragment_debt_item, null);
            holder = new Holder();
            holder.fullNameFrom = (TextView) view.findViewById(R.id.fragment_debt_item_user_from);
            holder.fullNameTo = (TextView) view.findViewById(R.id.fragment_debt_item_user_to);
            holder.amount = (TextView)view.findViewById(R.id.fragment_item_debt_amount);
            holder.emailFrom = (TextView) view.findViewById(R.id.fragment_debt_item_email_from);
            holder.emailTo = (TextView) view.findViewById(R.id.fragment_debt_item_email_to);

            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }

        final Debt item = (Debt) getItem(position);
        holder.amount.setText(String.valueOf(item.getAmount()));
        holder.fullNameFrom.setText(item.getFullNameFrom());
        holder.fullNameTo.setText(item.getFullNameTo());
        holder.emailFrom.setText(item.getEmailFrom());
        holder.emailTo.setText(item.getEmailTo());
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        reloadItems();
        super.notifyDataSetChanged();
    }

    private void reloadItems(){
        Log.d(TAG_LOG, "RELOADING ITEMS...");
        dao.open();
        //mUser = LocalUser.load(MyApplication.getAppContext());
        mItems = dao.getAllDebtsExtra(mIdGroup, mShowOnlyYourDebts, mUser.getId());
        dao.close();
    }
}
