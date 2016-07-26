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

import java.util.Collections;
import java.util.Currency;
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

    public void addItem(int position, GroupModel model) {
        mItems.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final GroupModel model = mItems.remove(fromPosition);
        mItems.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void updateItem(int idGroup){
        int position = getGroupPosition(idGroup);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    public void animateTo(List<GroupModel> items) {
        applyAndAnimateRemovals(items);
        applyAndAnimateAdditions(items);
        applyAndAnimateMovedItems(items);
    }

    private void applyAndAnimateRemovals(List<GroupModel> newItems) {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final GroupModel item = mItems.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<GroupModel> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final GroupModel item = newItems.get(i);
            if (!mItems.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<GroupModel> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final GroupModel model = newItems.get(toPosition);
            final int fromPosition = mItems.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // qui puo' esserce anche un if per alcuni viewType e usare inflater con diversi layout
        View v = mInflater.inflate(R.layout.fragment_group_item, parent, false);
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
            Palette.from(bm).generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette p) {
                    //holder.layout.setBackgroundColor(p.getVibrantColor(ContextCompat.getColor(mContext,R.color.md_grey_700)));
                    Palette.Swatch swatch = p.getVibrantSwatch();
                    if(swatch != null){
                        holder.layout.setBackgroundColor(swatch.getRgb());
                        //holder.groupName.setTextColor(swatch.getTitleTextColor());
                        //holder.personalStatus.setTextColor(swatch.getBodyTextColor());
                        //holder.layout.setBackgroundColor(mContext.getResources().getColor(R.color.md_amber_500));
                    } else {
                        holder.layout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.md_grey_700));
                    }
                }
            });
        } else {
            holder.groupImageView.setImageResource(R.drawable.ic_group_black_48dp);
            holder.layout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.md_grey_700));
        }


        if(itemGroup.isHighlighted()){
            //holder.groupModified.setText("N");
            holder.groupImageNew.setVisibility(View.VISIBLE);
        } else {
            //holder.groupModified.setText("U");
            holder.groupImageNew.setVisibility(View.GONE);
        }

        GroupDAO dao = new GroupDAO();
        dao.open();
        Double userAmount = dao.getAmount(itemGroup.getId(), mLocalUser.getId());
        dao.close();
        if (userAmount!=null){
            //Currency curr = Currency.getInstance(Locale.getDefault());
            //Currency curr = Currency.getInstance("EUR");
            //holder.personalStatus.setText(curr.getSymbol() + " " + Amount.getAmountString(userAmount));

            holder.personalStatus.setText(Amount.getAmountStringCurrency(userAmount,"EUR"));
            if(userAmount>0){
                //holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                holder.groupImageTrending.setImageResource(R.drawable.ic_trending_up_white_24dp);
            } else if(userAmount <0){
                //holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                holder.groupImageTrending.setImageResource(R.drawable.ic_trending_down_white_24dp);
            } else{
                //holder.personalStatus.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.groupImageTrending.setImageResource(R.drawable.ic_trending_flat_white_24dp);
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
        private TextView personalStatus;
        private ImageView groupImageNew;
        //private ImageView groupImageFavorite;
        private ImageView groupImageTrending;

        private LinearLayout layout;


        public GroupViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            groupImageView = (ImageView) itemView.findViewById(R.id.group_tile_imageView);
            groupName = (TextView)itemView.findViewById(R.id.group_tile_groupName);
            personalStatus = (TextView) itemView.findViewById(R.id.group_tile_personalStatus);
            groupImageTrending = (ImageView) itemView.findViewById(R.id.group_tile_image_trending);

            groupImageNew = (ImageView) itemView.findViewById(R.id.group_tile_image_new);
            layout = (LinearLayout) itemView.findViewById(R.id.group_details);

            /*
            groupImageFavorite = (ImageView) itemView.findViewById(R.id.group_tile_image_favorite);
            groupImageFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    groupImageFavorite.setImageResource(R.drawable.ic_favorite_white_24dp);
                }
            });*/
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG_LOG, "onClick position: " + getAdapterPosition());
            if(mOnItemClickListener != null){
                mOnItemClickListener.onItemClicked(v,getAdapterPosition());
            } else {
                Log.d(TAG_LOG, "mOnItemClickListener is null");
            }
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