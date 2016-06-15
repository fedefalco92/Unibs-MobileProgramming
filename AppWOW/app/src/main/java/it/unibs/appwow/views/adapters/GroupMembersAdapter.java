package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.models.UserModel;

/**
 * Created by Massi on 24/05/2016.
 */
public class GroupMembersAdapter extends BaseAdapter {
    private static final String TAG_LOG = GroupMembersAdapter.class.getSimpleName();

    private final List<UserModel> mItems;
    private final LayoutInflater mInflater;

    private int VIEW_TYPE_NUMBER = 2;

    private class Holder {
        TextView fullnameUser;
        TextView emailUser;
    }

    public GroupMembersAdapter(Context context,List<UserModel> users){
        mInflater = LayoutInflater.from(context);
        this.mItems = users;
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
    public int getViewTypeCount() {
        return VIEW_TYPE_NUMBER;
    }

    public void add(UserModel item){
        mItems.add(item);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        /*final UserModel itemUser = (UserModel) getItem(position);
        if(itemUser.isGroupAdmin())
            return 1;
        else
            return 0;*/
        return (position>0)?0:1;
    }

    @Override
    public long getItemId(int position) {
        final UserModel userItem = (UserModel) getItem(position);
        return userItem.getId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            if(getItemViewType(position) == 0) {
                view = mInflater.inflate(R.layout.listview_members_simpleitem_layout,null);
            } else {
                view = mInflater.inflate(R.layout.listview_members_admin_layout,null);
                view.setOnClickListener(null); //disabilita la selezione per l'utente admin
            }
            holder = new Holder();
            holder.fullnameUser = (TextView) view.findViewById(R.id.textView_userMember);
            holder.emailUser = (TextView) view.findViewById(R.id.textView_email_member);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final UserModel itemUser = (UserModel) getItem(position);
        holder.emailUser.setText(itemUser.getEmail());
        holder.fullnameUser.setText(itemUser.getFullName());

        return view;
    }
}
