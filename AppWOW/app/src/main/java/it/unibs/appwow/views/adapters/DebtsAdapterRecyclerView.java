package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.database.DebtDAO;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.DebtModel;
import it.unibs.appwow.models.parc.LocalUser;


public class DebtsAdapterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG_LOG = DebtsAdapterRecyclerView.class.getSimpleName();

    // View Type
    private static final int PAYMENT_CLASSIC_VIEW = 1;
    private static final int PAYMENT_SPECIAL_VIEW = 2;
    private static final int PAYMENT_EMPTY_VIEW = 3;

    private LocalUser mUser;
    private List<Debt> mItems = new ArrayList<Debt>();
    private DebtDAO dao;
    private boolean mShowOnlyYourDebts;
    private int mIdGroup;
    private LayoutInflater mInflater;

    // Listeners
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public DebtsAdapterRecyclerView(Context context, int idGroup, boolean showOnlyYourDebts){
        mInflater = LayoutInflater.from(context);
        mUser = LocalUser.load(context);
        mShowOnlyYourDebts = showOnlyYourDebts;
        mIdGroup = idGroup;
        dao = new DebtDAO();
        dao.open();
        mItems = dao.getAllDebtsExtra(mIdGroup, mShowOnlyYourDebts, mUser.getId());
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    public DebtsAdapterRecyclerView(Context context, int idGroup, boolean showOnlyYourDebts, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener){
        mInflater = LayoutInflater.from(context);
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;

        mUser = LocalUser.load(context);
        mShowOnlyYourDebts = showOnlyYourDebts;
        mIdGroup = idGroup;
        dao = new DebtDAO();
        dao.open();
        mItems = dao.getAllDebtsExtra(mIdGroup, mShowOnlyYourDebts, mUser.getId());
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
        itemHolder.debtNameUserFrom.setText(itemDebt.getFullNameFrom());
        itemHolder.debtNameUserTo.setText(itemDebt.getFullNameTo());
        itemHolder.debtNameMailFrom.setText(itemDebt.getEmailFrom());
        itemHolder.debtNameMailTo.setText(itemDebt.getEmailTo());
        itemHolder.debtAmount.setText(String.valueOf(itemDebt.getAmount()));
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

    /*@Override CANNOT DO IT
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
    }*/

    public void reloadItems(){
        Log.d(TAG_LOG, "RELOADING ITEMS...");
        dao.open();
        //mUser = LocalUser.load(MyApplication.getAppContext());
        mItems = dao.getAllDebtsExtra(mIdGroup, mShowOnlyYourDebts, mUser.getId());
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

    // FIXME: 14/07/16 Controllare click perche a volte non funzionano molto bene...
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
/*
public class DebtsAdapterRecyclerView extends BaseAdapter {
    private static final String TAG_LOG = DebtsAdapterRecyclerView.class.getSimpleName();
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

    public DebtsAdapterRecyclerView(Context context, int idGroup, boolean showOnlyYourDebts){
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
*/