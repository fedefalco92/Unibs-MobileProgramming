package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.PaymentDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.IdEncodingUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class ExpandablePaymentAdapter extends BaseExpandableListAdapter {
    private static final String TAG_LOG = ExpandablePaymentAdapter.class.getSimpleName();

    private Context mContext;

    private List<Payment> mItems;
    private final LayoutInflater mInflater;
    private PaymentDAO dao = new PaymentDAO();

    @Override
    public int getGroupCount() {
        return mItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Payment p = (Payment) getGroup(groupPosition);
        return p.hasDetails()?1:0;
    }

    @Override
    public Object getGroup(int position) {
        return mItems.get(position);
    }

    public Payment getItem(int position){
        Payment item = (Payment) getGroup(position);
        return item;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int position) {
        final Payment itemPayment = (Payment) getGroup(position);
        return itemPayment.getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getGroupId(groupPosition);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        PaymentHolder paymentHolder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.fragment_expandable_payment_item,null);
            paymentHolder = new PaymentHolder();
            paymentHolder.costName = (TextView) view.findViewById(R.id.payment_expandable_fragment_item_costname);
            paymentHolder.costAmount = (TextView)view.findViewById(R.id.payment_expandable_fragment_item_value);
            paymentHolder.costDate = (TextView) view.findViewById(R.id.payment_expandable_fragment_item_date);
            paymentHolder.costUser = (TextView) view.findViewById(R.id.payment_expandable_fragment_item_username);
            paymentHolder.costEmail = (TextView) view.findViewById(R.id.payment_expandable_fragment_item_email);
            view.setTag(paymentHolder);
        } else {
            paymentHolder = (PaymentHolder)view.getTag();
        }
        final Payment itemCost = (Payment) getGroup(groupPosition);
        paymentHolder.costName.setText(itemCost.getName());
        paymentHolder.costAmount.setText(""+itemCost.getAmount());
        paymentHolder.costDate.setText(DateUtils.dateLongToString(itemCost.getUpdatedAt()));
        paymentHolder.costUser.setText(itemCost.getFullName());
        paymentHolder.costEmail.setText(itemCost.getEmail());
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        PaymentDetailsHolder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.fragment_expandable_payment_child,null);
            holder = new PaymentDetailsHolder();
            holder.notes = (TextView) view.findViewById(R.id.payment_detail_notes_text);
            holder.notesLabel = (TextView) view.findViewById(R.id.payment_detail_notes_label);
            holder.position = (TextView)view.findViewById(R.id.payment_detail_position_text);
            holder.positionLabel = (TextView) view.findViewById(R.id.payment_detail_position_label);
            holder.mapButton = (ImageButton) view.findViewById(R.id.payment_detail_map_button);
            holder.amountDetailContainer = (LinearLayout) view.findViewById(R.id.payment_detail_amount_details_container);
            view.setTag(holder);
        } else {
            holder = (PaymentDetailsHolder)view.getTag();
        }
        final Payment itemCost = (Payment) getGroup(groupPosition);
        String notes = itemCost.getNotes();
        if(!notes.isEmpty()){
            holder.notes.setText(notes);
        } else {
            holder.notes.setVisibility(View.GONE);
            holder.notesLabel.setVisibility(View.GONE);
        }

        String position = itemCost.getPosition();
        if(!position.isEmpty()){
            holder.position.setText(position);
        } else {
            holder.position.setVisibility(View.GONE);
            holder.positionLabel.setVisibility(View.GONE);
        }

        final String position_id = itemCost.getPositionId();
        if(!position_id.isEmpty()){
            holder.mapButton.setVisibility(View.VISIBLE);
            holder.mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://maps.google.com/?cid=" + position_id);
                    Intent gmaps = new Intent(Intent.ACTION_VIEW, uri);
                    gmaps.setPackage("com.google.android.apps.maps");
                    mContext.startActivity(gmaps);
                }
            });
        } else {
            holder.mapButton.setVisibility(View.INVISIBLE);
        }



        String ad = itemCost.getAmountDetails();
        List<Amount> amounts = IdEncodingUtils.decodeAmountDetails(ad, itemCost.getIdUser(), itemCost.getAmount());
        holder.amountDetailContainer.removeAllViews();
        for(Amount a: amounts){
            TextView tv = new TextView(mContext, null);
            tv.setText(a.getFormattedString());
            holder.amountDetailContainer.addView(tv);
        }





        return view;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private class PaymentHolder {
        TextView costName;
        TextView costAmount;
        TextView costDate;
        TextView costUser;
        TextView costEmail;
    }

    private class PaymentDetailsHolder{
        TextView notes;
        TextView notesLabel;
        TextView position;
        TextView positionLabel;
        ImageButton mapButton;
        LinearLayout amountDetailContainer;
    }

    public ExpandablePaymentAdapter(Context context, int idGroup, List<Payment> items){
        mItems = items;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        dao.open();
        mItems = dao.getAllPayments(idGroup);
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

}
