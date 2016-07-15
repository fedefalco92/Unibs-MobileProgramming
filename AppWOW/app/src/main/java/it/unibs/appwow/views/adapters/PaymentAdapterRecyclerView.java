package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.PaymentDetailsActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.database.PaymentDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.utils.DateUtils;


public class PaymentAdapterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG_LOG = PaymentAdapterRecyclerView.class.getSimpleName();

    // View Type
    private static final int PAYMENT_CLASSIC_VIEW = 1;
    private static final int PAYMENT_SPECIAL_VIEW = 2;
    private static final int PAYMENT_EMPTY_VIEW = 3;

    private List<Payment> mItems = new ArrayList<Payment>();
    private LayoutInflater mInflater;
    private PaymentDAO dao;
    private int mIdGroup;

    // Listeners
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public PaymentAdapterRecyclerView(Context context, int idGroup){
        mIdGroup = idGroup;
        mInflater = LayoutInflater.from(context);
        dao = new PaymentDAO();
        dao.open();
        mItems = dao.getAllPayments(idGroup);
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    public PaymentAdapterRecyclerView(Context context, int idGroup, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener){
        mIdGroup = idGroup;
        mInflater = LayoutInflater.from(context);
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;

        dao = new PaymentDAO();
        dao.open();
        mItems = dao.getAllPayments(idGroup);
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    @Override
    public int getItemViewType(int position) {
        // Empty view
        if (mItems.size() == 0) {
            return PAYMENT_EMPTY_VIEW;
        } else {
            // Special payment
            if (mItems.get(position).isExchange()) {
                return PAYMENT_SPECIAL_VIEW;
            } else {
                return PAYMENT_CLASSIC_VIEW;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;

        switch (viewType) {
            case PAYMENT_CLASSIC_VIEW:
                View vClassic = mInflater.inflate(R.layout.fragment_payment_item, parent, false);
                vh = new PaymentViewHolder(vClassic);
                break;
            case PAYMENT_SPECIAL_VIEW:
                View vSpecial = mInflater.inflate(R.layout.fragment_payment_item_special, parent, false);
                vh = new PaymentSpecialViewHolder(vSpecial);
                break;
            case PAYMENT_EMPTY_VIEW:
                View vEmpty = mInflater.inflate(R.layout.fragment_payment_item,parent,false);
                vh = new PaymentViewHolder(vEmpty);
                break;
            default:
                break;
        }
        return vh;

        /*
        // qui puo' esserce anche un if per alcuni viewType e usare inflater con diversi layout
        View v = mInflater.inflate(R.layout.fragment_payment_item, parent, false);
        PaymentViewHolder vh = new PaymentViewHolder(v);
        return vh;*/
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Payment itemPayment = mItems.get(position);

        switch (holder.getItemViewType()) {
            case PAYMENT_CLASSIC_VIEW:
                PaymentViewHolder itemClassicHolder = (PaymentViewHolder) holder;
                itemClassicHolder.paymentName.setText(itemPayment.getName());
                itemClassicHolder.paymentAmount.setText(String.valueOf(itemPayment.getAmount()));
                itemClassicHolder.paymentDate.setText(DateUtils.dateReadableLongToString(itemPayment.getUpdatedAt()));
                /*
                itemClassicHolder.paymentUser.setText(itemPayment.getFullName());
                itemClassicHolder.paymentEmail.setText(itemPayment.getEmail());*/
                itemClassicHolder.paymentUser.setText(itemPayment.getFullName() + " (" + itemPayment.getEmail() + ")");
                break;

            case PAYMENT_SPECIAL_VIEW:
                PaymentSpecialViewHolder itemSpecialHolder = (PaymentSpecialViewHolder) holder;
                //NOTA: in questo caso (il cost è un pagamento di debito) l'id del "ricevente" è nelle note
                String userTo ="";
                UserDAO dao = new UserDAO();
                dao.open();
                String [] info = dao.getSingleUserInfo(new Integer(itemPayment.getNotes()));
                dao.close();
                /*
                itemSpecialHolder.paymentName.setText(itemPayment.getFullName() + " gave " + Amount.getAmountString(itemPayment.getAmount()) + " eur to " + info[0] );
                itemSpecialHolder.paymentAmount.setText("");
                itemSpecialHolder.paymentDate.setText(DateUtils.dateLongToString(itemPayment.getUpdatedAt()));*/
                itemSpecialHolder.paymentNameFrom.setText(itemPayment.getFullName() + " (" + itemPayment.getEmail() + ")");
                itemSpecialHolder.paymentNameTo.setText(info[0] + " (" + info[1] + ")");
                itemSpecialHolder.paymentAmount.setText(Amount.getAmountString(itemPayment.getAmount()));
                itemSpecialHolder.paymentDate.setText(DateUtils.dateReadableLongToString(itemPayment.getUpdatedAt()));
                break;
            case PAYMENT_EMPTY_VIEW:
                break;
            default:
                break;
        }


        /*
        if(holder instanceof PaymentViewHolder){
            Payment itemPayment = mItems.get(position);
            PaymentViewHolder itemHolder = (PaymentViewHolder) holder;
            if(!itemPayment.isExchange()){
                itemHolder.paymentName.setText(itemPayment.getName());
                itemHolder.paymentAmount.setText(String.valueOf(itemPayment.getAmount()));
                itemHolder.paymentDate.setText(DateUtils.dateLongToString(itemPayment.getUpdatedAt()));
                itemHolder.paymentUser.setText(itemPayment.getFullName());
                itemHolder.paymentEmail.setText(itemPayment.getEmail());
            } else {
                // FIXME: 11/07/2016 mettere altro layout, ovvero un viewType diverso...
                //NOTA: in questo caso (il cost è un pagamento di debito) l'id del "ricevente" è nelle note
                String userTo ="";
                UserDAO dao = new UserDAO();
                dao.open();
                String [] info = dao.getSingleUserInfo(new Integer(itemPayment.getNotes()));
                dao.close();
                itemHolder.paymentName.setText(itemPayment.getFullName() + " gave " + Amount.getAmountString(itemPayment.getAmount()) + " eur to " + info[0] );
                itemHolder.paymentAmount.setText("");
                itemHolder.paymentDate.setText(DateUtils.dateLongToString(itemPayment.getUpdatedAt()));
                itemHolder.paymentUser.setText("");
                itemHolder.paymentEmail.setText("");
            }
        }*/
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public void remove(int position){
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void remove(Payment item){
        int position = mItems.indexOf(item);
        if(position != -1){
            mItems.remove(item);
            notifyItemRemoved(position);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Payment> list) {
        mItems.addAll(list);
        notifyDataSetChanged();
    }

    public void reload(){
        mItems.clear();
        dao.open();
        mItems = dao.getAllPayments(mIdGroup);
        dao.close();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClicked(View v, int position);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(View v, int position);
    }

    public class PaymentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView paymentName;
        public TextView paymentAmount;
        public TextView paymentDate;
        public TextView paymentUser;
        //public TextView paymentEmail;

        public PaymentViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            paymentName = (TextView) itemView.findViewById(R.id.payment_fragment_item_costname);
            paymentAmount = (TextView)itemView.findViewById(R.id.payment_fragment_item_value);
            paymentDate = (TextView) itemView.findViewById(R.id.payment_fragment_item_date);
            paymentUser = (TextView) itemView.findViewById(R.id.payment_fragment_item_username);
            //paymentEmail = (TextView) itemView.findViewById(R.id.payment_fragment_item_email);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG_LOG, "onClick position: " + getAdapterPosition());
            mOnItemClickListener.onItemClicked(v,getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG_LOG, "onLongClick position: " + getAdapterPosition());
            mOnItemLongClickListener.onItemLongClicked(v,getAdapterPosition());
            return true;
        }
    }

    public class PaymentSpecialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView paymentNameFrom;
        public TextView paymentNameTo;
        public TextView paymentAmount;
        public TextView paymentDate;

        public PaymentSpecialViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            paymentNameFrom = (TextView) itemView.findViewById(R.id.payment_fragment_item_from);
            paymentNameTo = (TextView) itemView.findViewById(R.id.payment_fragment_item_to);
            paymentAmount = (TextView)itemView.findViewById(R.id.payment_fragment_item_value);
            paymentDate = (TextView) itemView.findViewById(R.id.payment_fragment_item_date);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG_LOG, "onClick position: " + getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG_LOG, "onLongClick position: " + getAdapterPosition());
            return true;
        }
    }
}