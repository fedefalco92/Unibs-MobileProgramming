package it.unibs.appwow.graphicTools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;
import it.unibs.appwow.model.Group;

/**
 * Created by Massi on 10/05/2016.
 */
public class OfflineGroupAdapter extends BaseAdapter {

    private final List<Group> mItems = new ArrayList<Group>();
    private final LayoutInflater mInflater;

    private class Holder {
        ImageView groupImageView;
        TextView groupName;
       /* TextView personalStatus;
        TextView groupModfiedIndicator;*/
    }

    public OfflineGroupAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        mItems.add(new Group(1,"primo gruppo offline", R.drawable.ic_menu_send,System.currentTimeMillis(),System.currentTimeMillis(),1));
        mItems.add(new Group(2,"offline gruppo",R.drawable.ic_menu_send,System.currentTimeMillis(),System.currentTimeMillis(),2));
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
        final Group itemGroup = (Group) getItem(position);
        return itemGroup.id;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.tile_group_layout,null);
            holder = new Holder();
            holder.groupImageView = (ImageView) view.findViewById(R.id.imageView_groupPhoto);
            holder.groupName = (TextView)view.findViewById(R.id.textView_groupName);

            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }
        final Group itemGroup = (Group) getItem(position);
        holder.groupName.setText(itemGroup.groupName);
        holder.groupImageView.setImageResource(itemGroup.photoUri);
       /* final LocalDataModel itemModel = (LocalDataModel)getItem(position);
        holder.dateTextView.setText(DATE_FORMAT.format(itemModel.entryDate));
        holder.loveVoteTextView.setText("Love: " + itemModel.loveVote);
        holder.healthVoteTextView.setText("Health: " + itemModel.healthVote);
        holder.workVoteTextView.setText("Work: " + itemModel.workVote);
        holder.luckVoteTextView.setText("Luck: " + itemModel.luckVote);*/
        return view;
    }


}