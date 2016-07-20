package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.FileUtils;

/**
 * Created by Massi on 05/05/2016.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    private static final String TAG_LOG = GroupAdapter.class.getSimpleName();
    public static final String PASSING_GROUP_TAG = "group";

    private List<GroupModel> mItems = Collections.emptyList();
    private GroupDAO dao;
    private LayoutInflater mInflater;
    private Context mContext;
    private LocalUser mLocalUser;

    // Listeners
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public GroupAdapter(Context context){
        this.mContext = context;
        mInflater = LayoutInflater.from(context);

        this.mLocalUser = LocalUser.load(mContext);
        dao = new GroupDAO();
        dao.open();
        mItems = dao.getAllGroups();
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    public GroupAdapter(Context context, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener){
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;

        this.mLocalUser = LocalUser.load(mContext);
        dao.open();
        mItems = dao.getAllGroups();
        dao.close();
        Log.d(TAG_LOG, "Size mItems = "+ mItems.size());
    }

    public void removeItem(GroupModel item){
        int position = mItems.indexOf(item);
        if (position != -1) {
            mItems.remove(item);
            notifyItemRemoved(position);
        }
    }

    public void removeItem(int position){
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItem(int idGroup){
        int position = getGroupPosition(idGroup);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // qui puo' esserce anche un if per alcuni viewType e usare inflater con diversi layout
        View v = mInflater.inflate(R.layout.tile_group_layout_recycler, parent, false);
        GroupViewHolder vh = new GroupViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {
        // anche qui
        GroupModel itemGroup = mItems.get(position);
        holder.groupName.setText(itemGroup.getGroupName());

        /*
        if(itemGroup.getPhotoFileName().isEmpty()){
            holder.groupImageView.setImageResource(R.drawable.ic_group_black_24dp);
        } else {
            holder.groupImageView.setImageBitmap(FileUtils.readGroupImage(itemGroup.getId(), mContext));
        }*/

        Bitmap bm = FileUtils.readGroupImage(itemGroup.getId(), mContext);
        if(bm!=null){
            holder.groupImageView.setImageBitmap(bm);
        } else {
            holder.groupImageView.setImageResource(R.drawable.ic_group_black_48dp);
        }

        // FIXME: 20/07/16 Sistemare PAlette
        /*Palette.from(bm).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                holder.layout.setBackgroundColor(p.getVibrantColor(mContext.getResources().getColor(R.color.md_grey_700)));
            }
        });*/


        if(itemGroup.isHighlighted()){
            //holder.groupModified.setText("N");
            holder.groupImageNew.setVisibility(View.VISIBLE);
        } else {
            //holder.groupModified.setText("U");
            holder.groupImageNew.setVisibility(View.INVISIBLE);
        }

        GroupDAO dao = new GroupDAO();
        dao.open();
        Double userAmount = dao.getAmount(itemGroup.getId(), mLocalUser.getId());
        dao.close();
        if (userAmount!=null){
            //// FIXME: 20/07/16 Da sistemare currency...
            Currency curr = Currency.getInstance(Locale.getDefault());
            holder.personalStatus.setText(curr.getSymbol() + " " + Amount.getAmountString(userAmount));
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
            //Log.d(TAG_LOG, "getGroupPosition(): id:" + g.getId());
            if(g.getId()==idGroup)
                return mItems.indexOf(g);
        }
        return -1;
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }


    public void reload(){
        mItems.clear();
        dao.open();
        mItems = dao.getAllGroups();
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

    public class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView groupImageView;
        private TextView groupName;
        //private TextView groupModified;
        private TextView personalStatus;
        private ImageView groupImageNew;
        private ImageView groupImageFavorite;

        private LinearLayout layout;


        public GroupViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            groupImageView = (ImageView) itemView.findViewById(R.id.group_tile_imageView);
            groupName = (TextView)itemView.findViewById(R.id.group_tile_groupName);
            personalStatus = (TextView) itemView.findViewById(R.id.group_tile_personalStatus);

            //groupModified = (TextView) itemView.findViewById(R.id.group_tile_modified_indicator);
            groupImageNew = (ImageView) itemView.findViewById(R.id.group_tile_image_new);
            layout = (LinearLayout) itemView.findViewById(R.id.group_details);

            groupImageFavorite = (ImageView) itemView.findViewById(R.id.group_tile_image_favorite);
            groupImageFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 20/07/16 completare
                    groupImageFavorite.setImageResource(R.drawable.ic_favorite_white_24dp);
                }
            });
        }

        // todo Da sistemare con implementazione piu' efficiente, anche se per ora funziona
        @Override
        public void onClick(View v) {
            Log.d(TAG_LOG, "onClick position: " + getAdapterPosition());
            if(mOnItemClickListener != null){
                mOnItemClickListener.onItemClicked(v,getAdapterPosition());
            } else {
                Log.d(TAG_LOG, "mOnItemClickListener is null");
            }


            /*
            Log.d(TAG_LOG, "click on " + getAdapterPosition());
            GroupModel group = mItems.get(getAdapterPosition());
            Log.d(TAG_LOG, "group: " + group.getGroupName());
            final Intent i = new Intent(mContext, GroupDetailsActivity.class);

            /**
             * il gruppo che sto passando è highlighted.
             * Userò questa informazione per aggiornare l'intero gruppo in GroupDetailsActivity
             *//*
            i.putExtra(PASSING_GROUP_TAG, group);

            //tolgo l'highlight dal gruppo NEL DB LOCALE, non nell'oggetto passato
            GroupDAO dao = new GroupDAO();
            dao.open();
            dao.unHighlightGroup(group.getId());
            dao.close();

            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);*/
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

}


/*
public class GroupAdapter extends BaseAdapter {
    private static final String TAG_LOG = GroupAdapter.class.getSimpleName();

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

    public GroupAdapter(Context context){
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
