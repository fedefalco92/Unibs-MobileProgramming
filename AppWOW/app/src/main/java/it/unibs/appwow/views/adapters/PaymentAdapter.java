package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.database.PaymentDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class PaymentAdapter extends BaseAdapter {
    private static final String TAG_LOG = PaymentAdapter.class.getSimpleName();

    private List<Payment> mItems = new ArrayList<Payment>();
    private final LayoutInflater mInflater;
    private PaymentDAO dao;
    private int mIdGroup;

    private class Holder {
        TextView costName;
        TextView costAmount;
        TextView costDate;
        TextView costUser;
        TextView costEmail;
    }

    public PaymentAdapter(Context context, int idGroup){
        mIdGroup = idGroup;
        mInflater = LayoutInflater.from(context);
        dao = new PaymentDAO();
        dao.open();
        mItems = dao.getAllPayments(idGroup);
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
        final Payment itemCost = (Payment) getItem(position);
        return itemCost.getId();
    }

    public void remove(int position){
        mItems.remove(position);
        notifyDataSetChanged();
    }

    public void remove(Payment item){
        for(Payment p: mItems){
            if(p.getId() == item.getId()){
                mItems.remove(p);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void reload(){
        mItems.clear();
        dao.open();
        mItems = dao.getAllPayments(mIdGroup);
        dao.close();
        notifyDataSetChanged();
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
            holder.costEmail = (TextView) view.findViewById(R.id.payment_fragment_item_email);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final Payment itemCost = (Payment) getItem(position);
        if(!itemCost.isExchange()) {
            holder.costName.setText(itemCost.getName());
            holder.costAmount.setText(String.valueOf(itemCost.getAmount()));
            holder.costDate.setText(DateUtils.dateLongToString(itemCost.getUpdatedAt()));
            holder.costUser.setText(itemCost.getFullName());
            holder.costEmail.setText(itemCost.getEmail());
        } else {
            // FIXME: 11/07/2016 mettere altro layout
            //NOTA: in questo caso (il cost è un pagamento di debito) l'id del "ricevente" è nelle note
            String userTo ="";
            UserDAO dao = new UserDAO();
            dao.open();
            String [] info = dao.getSingleUserInfo(new Integer(itemCost.getNotes()));
            dao.close();
            holder.costName.setText(itemCost.getFullName() + " gave " + Amount.getAmountString(itemCost.getAmount()) + " eur to " + info[0] );
            holder.costAmount.setText("");
            holder.costDate.setText(DateUtils.dateLongToString(itemCost.getUpdatedAt()));
            holder.costUser.setText("");
            holder.costEmail.setText("");
        }
        return view;
    }

}
