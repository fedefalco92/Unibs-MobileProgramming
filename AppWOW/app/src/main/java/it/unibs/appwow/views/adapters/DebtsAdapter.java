package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.database.AppDB;
import it.unibs.appwow.database.DebtDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.parc.LocalUser;


public class DebtsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG_LOG = DebtsAdapter.class.getSimpleName();

    // View Type
    private static final int PAYMENT_CLASSIC_VIEW = 1;
    private static final int PAYMENT_SPECIAL_VIEW = 2;
    private static final int PAYMENT_EMPTY_VIEW = 3;

    private Context mContext;
    private LocalUser mUser;
    private List<Debt> mItems = new ArrayList<Debt>();
    private DebtDAO dao;
    private int mIdGroup;
    private LayoutInflater mInflater;

    // Listeners
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public DebtsAdapter(Context context, int idGroup){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mUser = LocalUser.load(context);
        mIdGroup = idGroup;
        dao = new DebtDAO();
        dao.open();
        mItems = dao.getAllDebtsExtra(mIdGroup);
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    public DebtsAdapter(Context context, int idGroup, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener){
        mInflater = LayoutInflater.from(context);
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;

        mUser = LocalUser.load(context);
        mIdGroup = idGroup;
        dao = new DebtDAO();
        dao.open();
        mItems = dao.getAllDebtsExtra(mIdGroup);
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.fragment_debt_item, parent, false);
        DebtViewHolder vh = new DebtViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Debt itemDebt = mItems.get(position);
        DebtViewHolder itemHolder = (DebtViewHolder) holder;
        LocalUser user = LocalUser.load(mContext);
        if(itemDebt.getFullNameFrom().equals(user.getFullName())){
            itemHolder.debtNameUserFrom.setText(mContext.getResources().getString(R.string.you));
        } else {
            itemHolder.debtNameUserFrom.setText(itemDebt.getFullNameFrom());
        }

        if(itemDebt.getFullNameTo().equals(user.getFullName())){
            itemHolder.debtNameUserTo.setText(mContext.getResources().getString(R.string.you));
        } else {
            itemHolder.debtNameUserTo.setText(itemDebt.getFullNameTo());
        }

        itemHolder.debtNameMailFrom.setText(itemDebt.getEmailFrom());
        itemHolder.debtNameMailTo.setText(itemDebt.getEmailTo());

        //Currency curr = Currency.getInstance("EUR");
        //itemHolder.debtAmount.setText(curr.getSymbol() + " " + Amount.getAmountString(itemDebt.getAmount()));
        itemHolder.debtAmount.setText(Amount.getAmountStringCurrency(itemDebt.getAmount(),"EUR"));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public void removeItem(int position){
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(Debt item){
        int position = mItems.indexOf(item);
        if(position != -1){
            mItems.remove(item);
            notifyItemRemoved(position);
        }
    }

    public void addItem(int position, Debt model) {
        mItems.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Debt model = mItems.remove(fromPosition);
        mItems.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<Debt> items) {
        applyAndAnimateRemovals(items);
        applyAndAnimateAdditions(items);
        applyAndAnimateMovedItems(items);
    }

    private void applyAndAnimateRemovals(List<Debt> newItems) {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final Debt item = mItems.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Debt> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final Debt item = newItems.get(i);
            if (!mItems.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Debt> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final Debt model = newItems.get(toPosition);
            final int fromPosition = mItems.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }


    public void reloadItems(){
        Log.d(TAG_LOG, "RELOADING ITEMS...");
        dao.open();
        //mUser = LocalUser.load(MyApplication.getAppContext());
        mItems = dao.getAllDebtsExtra(mIdGroup);
        dao.close();
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

    public class DebtViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView debtNameUserFrom;
        public TextView debtNameUserTo;
        public TextView debtNameMailFrom;
        public TextView debtNameMailTo;
        public TextView debtAmount;

        public DebtViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            debtNameUserFrom = (TextView) itemView.findViewById(R.id.fragment_debt_item_user_from);
            debtNameUserTo = (TextView)itemView.findViewById(R.id.fragment_debt_item_user_to);
            debtNameMailFrom = (TextView) itemView.findViewById(R.id.fragment_debt_item_email_from);
            debtNameMailTo = (TextView) itemView.findViewById(R.id.fragment_debt_item_email_to);
            debtAmount = (TextView) itemView.findViewById(R.id.fragment_item_debt_amount);
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
}