package it.unibs.appwow.graphicTools;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.model.Group;

/**
 * Created by Massi on 05/05/2016.
 */
public class GroupAdapter extends BaseAdapter {

    private final List<Group> mItems = new ArrayList<Group>();
    private final LayoutInflater mInflater;

    private class Holder {
        ImageView groupImageView;
        TextView groupName;
        TextView personalStatus;
        TextView groupModfiedIndicator;
    }

    public GroupAdapter(Context context){
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Holder holder = null;
        if(view==null) {
            view = mInflater.inflate(R.layout.tile_group_layout,null);
            /*holder = new Holder();
            holder.dateTextView = (TextView)view.findViewById(R.id.list_item_date);
            holder.loveVoteTextView = (TextView)view.findViewById(R.id.list_item_love_vote);
            holder.healthVoteTextView = (TextView)view.findViewById(R.id.list_item_health_vote);
            holder.workVoteTextView = (TextView)view.findViewById(R.id.list_item_work_vote);
            holder.luckVoteTextView = (TextView)view.findViewById(R.id.list_item_luck_vote);*/
            //view.setTag(holder);
        } else {
           // holder = (Holder)view.getTag();
        }
       /* final LocalDataModel itemModel = (LocalDataModel)getItem(position);
        holder.dateTextView.setText(DATE_FORMAT.format(itemModel.entryDate));
        holder.loveVoteTextView.setText("Love: " + itemModel.loveVote);
        holder.healthVoteTextView.setText("Health: " + itemModel.healthVote);
        holder.workVoteTextView.setText("Work: " + itemModel.workVote);
        holder.luckVoteTextView.setText("Luck: " + itemModel.luckVote);*/
        return view;
    }
}
