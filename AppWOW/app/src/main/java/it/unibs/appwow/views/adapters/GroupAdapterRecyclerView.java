package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.FileUtils;

/**
 * Created by Massi on 05/05/2016.
 */

public class GroupAdapterRecyclerView extends RecyclerView.Adapter<GroupAdapterRecyclerView.GroupViewHolder>{

    private static final String TAG_LOG = GroupAdapterRecyclerView.class.getSimpleName();

    private List<GroupModel> mItems; //= new ArrayList<GroupModel>();
    private GroupDAO dao = new GroupDAO();
    private Context mContext;
    private LocalUser mLocalUser;


    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        public ImageView groupImageView;
        public TextView groupName;
        public TextView groupModified;
        public TextView personalStatus;

        public GroupViewHolder(View itemView) {
            super(itemView);
            groupImageView = (ImageView) itemView.findViewById(R.id.group_tile_imageView);
            groupName = (TextView)itemView.findViewById(R.id.group_tile_groupName);
            groupModified = (TextView) itemView.findViewById(R.id.group_tile_modified_indicator);
            personalStatus = (TextView) itemView.findViewById(R.id.group_tile_personalStatus);
        }

    }

    public GroupAdapterRecyclerView(Context context){
        mContext = context;
        mLocalUser = LocalUser.load(mContext);
        dao.open();
        mItems = dao.getAllGroups();
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tile_group_layout_recycler, parent, false);

        GroupViewHolder vh = new GroupViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        GroupModel itemGroup = mItems.get(position);
        holder.groupName.setText(itemGroup.getGroupName());

        if(itemGroup.getPhotoFileName().isEmpty()){
            holder.groupImageView.setImageResource(getPhotoId(itemGroup.getPhotoFileName()));
            // TODO: 04/07/2016 scommentare, commentato per debug
            //holder.groupImageView.setImageBitmap(FileUtils.readBitmap("photo_1467633848847.png", mContext));
        } else {
            holder.groupImageView.setImageBitmap(FileUtils.readGroupImage(itemGroup.getId(), mContext));
        }

        if(itemGroup.isHighlighted()){
            holder.groupModified.setText("NEW");
        } else {
            holder.groupModified.setText("UP TO DATE");
        }

        GroupDAO dao = new GroupDAO();
        dao.open();
        Double userAmount = dao.getAmount(itemGroup.getId(), mLocalUser.getId());
        dao.close();
        if (userAmount!=null){
            holder.personalStatus.setText(Amount.getAmountString(userAmount));
            if(userAmount>0){
                holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green));
            } else if(userAmount <0){
                holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            } else{
                holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            }
        } else {
            holder.personalStatus.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Custom method.
     * @param idGroup
     * @return
     */
    public int getGroupPosition(int idGroup){
        for(GroupModel g:mItems){
            if(g.getId()==idGroup)
                return mItems.indexOf(g);
        }
        return -1;
    }

    private int getPhotoId(String photoURI){
        // TODO: 26/05/2016 GROUP ADAPTER implementare la trasformazione da photo uri (string) a resource ID (int)
        return R.drawable.ic_group_black_24dp;
    }
}


/*
public class GroupAdapterRecyclerView extends BaseAdapter {
    private static final String TAG_LOG = GroupAdapterRecyclerView.class.getSimpleName();

    private List<GroupModel> mItems = new ArrayList<GroupModel>();
    private final LayoutInflater mInflater;
    private GroupDAO dao = new GroupDAO();
    private Context mContext;
    private LocalUser mLocalUser;

    private class Holder {
        ImageView groupImageView;
        TextView groupName;
        TextView groupModified;
        TextView personalStatus;
    }

    public GroupAdapterRecyclerView(Context context){
        mContext = context;
        mLocalUser = LocalUser.load(mContext);
        mInflater = LayoutInflater.from(context);
        dao.open();
        mItems = dao.getAllGroups();
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
        final GroupModel itemGroup = (GroupModel) getItem(position);
        return itemGroup.getId();
    }

    public int getGroupPosition(int idGroup){
        for(GroupModel g:mItems){
            if(g.getId()==idGroup)
                return mItems.indexOf(g);
        }
        return -1;
    }



    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.tile_group_layout,null);
            holder = new Holder();
            holder.groupImageView = (ImageView) view.findViewById(R.id.group_tile_imageView);
            holder.groupName = (TextView)view.findViewById(R.id.group_tile_groupName);
            holder.groupModified = (TextView) view.findViewById(R.id.group_tile_modified_indicator);
            holder.personalStatus = (TextView) view.findViewById(R.id.group_tile_personalStatus);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final GroupModel itemGroup = (GroupModel) getItem(position);
        holder.groupName.setText(itemGroup.getGroupName());
        if(itemGroup.getPhotoFileName().isEmpty()){
            holder.groupImageView.setImageResource(getPhotoId(itemGroup.getPhotoFileName()));
            // TODO: 04/07/2016 scommentare, commentato per debug
            //holder.groupImageView.setImageBitmap(FileUtils.readBitmap("photo_1467633848847.png", mContext));
        } else {
            holder.groupImageView.setImageBitmap(FileUtils.readGroupImage(itemGroup.getId(), mContext));
        }

        if(itemGroup.isHighlighted()){
            holder.groupModified.setText("NEW");
        } else {
            holder.groupModified.setText("UP TO DATE");
        }

        GroupDAO dao = new GroupDAO();
        dao.open();
        Double userAmount = dao.getAmount(itemGroup.getId(), mLocalUser.getId());
        dao.close();
        if (userAmount!=null){
            holder.personalStatus.setText(Amount.getAmountString(userAmount));
            if(userAmount>0){
                holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green));
            } else if(userAmount <0){
                holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            } else{
                holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            }
        } else {
            holder.personalStatus.setText("");
        }
        return view;
    }

    private int getPhotoId(String photoURI){
        // TODO: 26/05/2016 GROUP ADAPTER implementare la trasformazione da photo uri (string) a resource ID (int)
        return R.drawable.ic_menu_send;
    }

}*/
