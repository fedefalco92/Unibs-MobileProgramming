package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.models.parc.GroupModel;

/**
 * Created by Massi on 10/05/2016.
 */
public class OfflineGroupAdapter extends BaseAdapter {

    private static final String TAG_LOG = OfflineGroupAdapter.class.getSimpleName();

    private final List<GroupModel> mItems = new ArrayList<GroupModel>();
    private final LayoutInflater mInflater;

    private class Holder {
        ImageView groupImageView;
        TextView groupName;
       /* TextView personalStatus;
        TextView groupModfiedIndicator;*/
    }

    public OfflineGroupAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        //mItems.add(new GroupModel(1,"primo gruppo offline", R.drawable.ic_menu_send,System.currentTimeMillis(),System.currentTimeMillis(),1));
        //mItems.add(new GroupModel(2,"offline gruppo",R.drawable.ic_menu_send,System.currentTimeMillis(),System.currentTimeMillis(),2));
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
            holder.groupImageView = (ImageView) view.findViewById(R.id.group_tile_imageView);
            holder.groupName = (TextView)view.findViewById(R.id.group_tile_groupName);

            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final GroupModel itemGroup = (GroupModel) getItem(position);
        holder.groupName.setText(itemGroup.getGroupName());
        holder.groupImageView.setImageResource(getPhotoId(itemGroup.getPhotoFileName()));
       /* final LocalDataModel itemModel = (LocalDataModel)getItem(position);
        holder.dateTextView.setText(DATE_FORMAT.format(itemModel.entryDate));
        holder.loveVoteTextView.setText("Love: " + itemModel.loveVote);
        holder.healthVoteTextView.setText("Health: " + itemModel.healthVote);
        holder.workVoteTextView.setText("Work: " + itemModel.workVote);
        holder.luckVoteTextView.setText("Luck: " + itemModel.luckVote);*/
        return view;
    }

    private int getPhotoId(String photoURI){
        // TODO: 26/05/2016 OFFLINE GROUP ADAPTER implementare la trasformazione da photo uri (string) a resource ID (int)
        return R.drawable.ic_menu_send;
    }


}