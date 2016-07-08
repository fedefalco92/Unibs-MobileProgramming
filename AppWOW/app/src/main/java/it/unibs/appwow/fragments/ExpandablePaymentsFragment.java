package it.unibs.appwow.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.unibs.appwow.PaymentDetailsActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.views.adapters.ExpandablePaymentAdapter;
import it.unibs.appwow.views.adapters.PaymentAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ExpandablePaymentsFragment extends Fragment {

    private static final String TAG_LOG = ExpandablePaymentsFragment.class.getSimpleName();
    public static final String PASSING_GROUP_TAG = "group";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String PASSING_PAYMENT_TAG = "cost";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ExpandablePaymentAdapter mAdapter;
    private GroupModel mGroup;
    private ExpandableListView  mPaymentsListView;

    private List<Payment> mPaymentsList; //da riempire
    //private Set<Payment> mSelectedItems;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ExpandablePaymentsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ExpandablePaymentsFragment newInstance(int columnCount, GroupModel group) {
        ExpandablePaymentsFragment fragment = new ExpandablePaymentsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putParcelable(PASSING_GROUP_TAG, group);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mGroup = getArguments().getParcelable(PASSING_GROUP_TAG);
        }
        mPaymentsList= new ArrayList<Payment>();

        //per poter popolare l'action bar dell'activity
        //setHasOptionsMenu(true);

        //mAdapter =  new PaymentAdapter(getActivity(), mGroup.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expandable_payment_list, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new ExpandablePaymentAdapter(getContext(), mGroup.getId(), mPaymentsList);
         mPaymentsListView = (ExpandableListView) view.findViewById(R.id.expandable_payment_list);
         mPaymentsListView.setEmptyView(view.findViewById(R.id.expandable_payment_list_empty_view));
         mPaymentsListView.setOnTouchListener(new OnSwipeTouchListener(getActivity(),  mPaymentsListView){

            @Override
            public void onSwipeRight(int pos) {

                Toast.makeText(getActivity(), "right", Toast.LENGTH_LONG).show();
                //showDeleteButton(pos);
            }

            @Override
            public void onSwipeLeft() {
                Toast.makeText(getActivity(), "left", Toast.LENGTH_LONG).show();
            }
        });
         mPaymentsListView.setAdapter(mAdapter);
        /*
         mPaymentsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
         mPaymentsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB

                String title = "";
                if(checked){
                    mSelectedItems.add(mAdapter.getItem(position));
                }
                else{
                    mSelectedItems.remove(mAdapter.getItem(position));
                }

                if(mSelectedItems.size() == 1){
                    title = "1 selected item";
                }
                else{
                    title = mSelectedItems.size()+" selected items";
                }
                mode.setTitle(title);
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu_selection, menu);
                // toolbar.setVisibility(View.GONE); // FIXME: 24/05/16 trovare soluzione piu' furba?
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()){
                    case R.id.context_delete:
                        Iterator iterator = mSelectedItems.iterator();
                        while(iterator.hasNext()){
                            Payment toRemove = (Payment) iterator.next();
                            //mDisplayedUsers.remove(toRemove);
                            //mAdapter.remove(toRemove);
                            mPaymentsList.remove(toRemove);
                            //mGroup.removeUser(toRemove);
                            //Log.d(TAG_LOG,"UTENTE RIMOSSO: " + toRemove + "; mGroup.size = " + mGroup.getUsersCount());
                            //AGGIORNO IL MENU
                            getActivity().invalidateOptionsMenu();
                        }
                        mAdapter.notifyDataSetChanged();
                        mode.finish();
                        return true;
                    default:
                        mode.finish();
                        return true;
                }
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                // toolbar.setVisibility(View.);
                //GroupMembersAdapter adapter = (GroupMembersAdapter)membersList.getAdapter();
                mSelectedItems.clear();
                //adapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
            }

        });*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(PaymentModel item);
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        ListView list;
        private GestureDetector gestureDetector;
        private Context context;

        public OnSwipeTouchListener(Context ctx, ListView list) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            context = ctx;
            this.list = list;
        }

        public OnSwipeTouchListener() {
            super();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public void onSwipeRight(int pos) {

        }

        public void onSwipeLeft() {

        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            private int getPostion(MotionEvent e1) {
                return list.pointToPosition((int) e1.getX(), (int) e1.getY());
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX)  > Math.abs(distanceY) && Math.abs(distanceX)  > SWIPE_THRESHOLD && Math.abs(velocityX)  > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX  > 0)
                    onSwipeRight(getPostion(e1));
                    else
                    onSwipeLeft();
                    return true;
                }
                return false;
            }

        }
    }
}
