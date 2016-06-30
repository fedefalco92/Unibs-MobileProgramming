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
import it.unibs.appwow.database.PaymentDAO;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class PaymentAdapter extends BaseAdapter {
    private static final String TAG_LOG = PaymentAdapter.class.getSimpleName();

    private List<PaymentModel> mItems = new ArrayList<PaymentModel>();
    private final LayoutInflater mInflater;
    private PaymentDAO dao = new PaymentDAO();

    private class Holder {
        TextView costName;
        TextView costAmount;
        TextView costDate;
        TextView costUser;
    }

    public PaymentAdapter(Context context, int idGroup){
        mInflater = LayoutInflater.from(context);
        dao.open();
        mItems = dao.getAllCosts(idGroup);
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
        final PaymentModel itemCost = (PaymentModel) getItem(position);
        return itemCost.getId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.fragment_payment_item,null);
            holder = new Holder();
            holder.costName = (TextView) view.findViewById(R.id.payment_fragment_item_costname);
            holder.costAmount = (TextView)view.findViewById(R.id.payment_fragment_item_value);
            holder.costDate = (TextView) view.findViewById(R.id.payment_fragment_item_date);
            holder.costUser = (TextView) view.findViewById(R.id.payment_fragment_item_username);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final PaymentModel itemCost = (PaymentModel) getItem(position);
        holder.costName.setText(itemCost.getName());
        holder.costAmount.setText(""+itemCost.getAmount());
        holder.costDate.setText(""+ DateUtils.dateLongToString(itemCost.getUpdatedAt()));
        holder.costUser.setText("ID_USER: " + itemCost.getIdUser());
        return view;
    }

}
