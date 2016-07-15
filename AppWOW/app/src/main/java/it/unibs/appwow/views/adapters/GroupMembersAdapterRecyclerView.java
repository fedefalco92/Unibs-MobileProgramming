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
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;

/**
 * Created by Alessandro on 14/07/2016.
 */
public class GroupMembersAdapterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG_LOG = GroupMembersAdapterRecyclerView.class.getSimpleName();

    private LocalUser mLocalUser;
    private final List<UserModel> mItems;
    private final LayoutInflater mInflater;

    // Listeners
    private OnItemLongClickListener mOnItemLongClickListener;

    public interface OnItemClickListener {
        public void onItemClicked(View v, int position);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(View v, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    private static final int VIEW_TYPE_NUMBER = 2;
    private static final int VIEW_TYPE_ADMIN = 1;
    private static final int VIEW_TYPE_USER = 2;

    public boolean minMemberNumberReached() {
        return mItems.size() >= 2;
    }

    public GroupMembersAdapterRecyclerView(Context context, List<UserModel> users){
        mInflater = LayoutInflater.from(context);
        this.mItems = users;
        mLocalUser = LocalUser.load(context);
    }

    public GroupMembersAdapterRecyclerView(Context context){
        mInflater = LayoutInflater.from(context);
        this.mItems = new ArrayList<UserModel>();
        mLocalUser = LocalUser.load(context);
    }


    public void add(UserModel item){
        mItems.add(item);
        this.notifyDataSetChanged();
    }

    public void remove(int position){
        mItems.remove(position);
        this.notifyDataSetChanged();
    }

    public void remove(UserModel item){
        mItems.remove(item);
        this.notifyDataSetChanged();
    }

    public boolean contains(UserModel user){
        return mItems.contains(user);
    }

    public List<UserModel> getItems() {
        return mItems;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position).getId()==mLocalUser.getId() ) {
            return VIEW_TYPE_ADMIN;
        } else {
            return VIEW_TYPE_USER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;

        switch (viewType) {
            case VIEW_TYPE_USER:
                View vClassic = mInflater.inflate(R.layout.listview_members_simpleitem_layout, parent, false);
                vh = new MemberViewHolder(vClassic);
                break;
            case VIEW_TYPE_ADMIN:
                View vSpecial = mInflater.inflate(R.layout.listview_members_admin_layout, parent, false);
                vh = new AdminMemberViewHolder(vSpecial);
                break;
            default:
                break;
        }
        return vh;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UserModel itemMember = mItems.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER:
                MemberViewHolder itemClassicHolder = (MemberViewHolder) holder;
                itemClassicHolder.fullnameUser.setText(itemMember.getFullName());
                itemClassicHolder.emailUser.setText(itemMember.getEmail());
                break;

            case VIEW_TYPE_ADMIN:
                AdminMemberViewHolder itemSpecialHolder = (AdminMemberViewHolder) holder;
                itemSpecialHolder.fullnameUser.setText(itemMember.getFullName());
                itemSpecialHolder.emailUser.setText(itemMember.getEmail());
                break;
            default:
                break;
        }
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView fullnameUser;
        public TextView emailUser;

        public MemberViewHolder(View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            fullnameUser = (TextView) itemView.findViewById(R.id.textView_userMember);
            emailUser = (TextView) itemView.findViewById(R.id.textView_email_member);
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG_LOG, "onLongClick position: " + getAdapterPosition());
            if(mOnItemLongClickListener != null){
                mOnItemLongClickListener.onItemLongClicked(v,getAdapterPosition());
            } else {
                Log.d(TAG_LOG, "mOnItemLongClickListener is null");
            }
            return true;
        }
    }

    public class AdminMemberViewHolder extends RecyclerView.ViewHolder {

        public TextView fullnameUser;
        public TextView emailUser;

        public AdminMemberViewHolder(View itemView) {
            super(itemView);
            fullnameUser = (TextView) itemView.findViewById(R.id.textView_userMember);
            emailUser = (TextView) itemView.findViewById(R.id.textView_email_member);
        }
    }
}
