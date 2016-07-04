package it.unibs.appwow.views.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.models.parc.GroupModel;

/**
 * Created by Massi on 05/05/2016.
 */
public class GroupAdapter extends BaseAdapter {
    private static final String TAG_LOG = GroupAdapter.class.getSimpleName();

    private List<GroupModel> mItems = new ArrayList<GroupModel>();
    private final LayoutInflater mInflater;
    private GroupDAO dao = new GroupDAO();

    private class Holder {
        ImageView groupImageView;
        TextView groupName;
        TextView groupModified;
       /* TextView personalStatus;
        TextView groupModfiedIndicator;*/
    }

    public GroupAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        dao.open();
        mItems = dao.getAllGroups();
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
        /*mItems.add(new GroupModel(1,"primo gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),1));
        mItems.add(new GroupModel(2,"asd gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),2));
        mItems.add(new GroupModel(3,"sdasda gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),2));
        mItems.add(new GroupModel(4,"priasdasdasmo gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),1));
        mItems.add(new GroupModel(5,"primo gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),1));
        mItems.add(new GroupModel(6,"asd gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),2));
        mItems.add(new GroupModel(7,"sdasda gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),2));
        mItems.add(new GroupModel(8,"priasdasdasmo gruppo",R.drawable.ic_menu_camera,System.currentTimeMillis(),System.currentTimeMillis(),1));
        */

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

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.tile_group_layout,null);
            holder = new Holder();
            holder.groupImageView = (ImageView) view.findViewById(R.id.imageView_groupPhoto);
            holder.groupName = (TextView)view.findViewById(R.id.textView_groupName);
            holder.groupModified = (TextView) view.findViewById(R.id.group_modified_indicator);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final GroupModel itemGroup = (GroupModel) getItem(position);
        holder.groupName.setText(itemGroup.getGroupName());
        holder.groupImageView.setImageResource(getPhotoId(itemGroup.getPhotoFileName()));
        // FIXME: 03/06/2016 MODIFICARE IN IMAGEVIEW
        if(itemGroup.isHighlighted()){
            holder.groupModified.setText("NEW");
        } else {
            holder.groupModified.setText("UP TO DATE");
        }
       /* final LocalDataModel itemModel = (LocalDataModel)getItem(position);
        holder.dateTextView.setText(DATE_FORMAT.format(itemModel.entryDate));
        holder.loveVoteTextView.setText("Love: " + itemModel.loveVote);
        holder.healthVoteTextView.setText("Health: " + itemModel.healthVote);
        holder.workVoteTextView.setText("Work: " + itemModel.workVote);
        holder.luckVoteTextView.setText("Luck: " + itemModel.luckVote);*/
        return view;
    }

    private int getPhotoId(String photoURI){
        // TODO: 26/05/2016 GROUP ADAPTER implementare la trasformazione da photo uri (string) a resource ID (int)
        return R.drawable.ic_menu_send;
    }

}
