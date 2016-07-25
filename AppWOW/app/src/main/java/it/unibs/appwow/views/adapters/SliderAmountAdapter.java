package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.support.v7.widget.AppCompatSeekBar;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import it.unibs.appwow.AddEditPaymentActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.models.SliderAmount;

/**
 * Created by Alessandro on 06/07/2016.
 */
public class SliderAmountAdapter extends BaseAdapter {
    private static final String TAG_LOG = SliderAmountAdapter.class.getSimpleName();

    private List<SliderAmount> mItems;
    private final LayoutInflater mInflater;
    private AddEditPaymentActivity mContext;

    private class Holder {
        TextView fullName;
        EditText amount;
        TextView email;
        AppCompatSeekBar seekbar;
    }

    public SliderAmountAdapter(Context context, List<SliderAmount> list){
        mContext = (AddEditPaymentActivity) context;
        mInflater = LayoutInflater.from(context);
        mItems = list;
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
        final SliderAmount item = (SliderAmount) getItem(position);
        return item.getUserId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final SliderAmount item = (SliderAmount) getItem(position);
        Holder holder = null;
        if(view==null) {
            holder = new Holder();
            view = mInflater.inflate(R.layout.activity_add_edit_payment_item,null);
            holder.fullName = (TextView) view.findViewById(R.id.payment_slider_item_fullname);
            holder.email = (TextView) view.findViewById(R.id.payment_slider_item_email);
            holder.amount = (EditText) view.findViewById(R.id.payment_slider_item_amount);
            holder.amount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(TAG_LOG, "CHARSEQUENCE S: " + s.toString() + " posizione: " + position);

                    changeAmount();
                    notifyDataSetChanged();
                }
            });
            holder.seekbar = (AppCompatSeekBar) view.findViewById(R.id.payment_slider_item_slider);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }


        holder.amount.setText(item.getAmountString());
        holder.fullName.setText(item.getFullName());
        holder.email.setText(item.getEmail());
        // TODO: 06/07/2016 gestire slider
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void initializeAmount(double amount) {
        double each = amount/getCount();
        for(SliderAmount s:mItems){
            s.setAmount(each);
        }
        notifyDataSetChanged();
    }

    public void changeAmount(){
        double total = mContext.getPaymentAmount();
    }

}
