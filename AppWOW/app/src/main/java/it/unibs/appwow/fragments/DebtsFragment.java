package it.unibs.appwow.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import it.unibs.appwow.R;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.views.adapters.DebtsAdapter;

/**
 * A fragment representing a list of Items.
 * <p />
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DebtsFragment extends Fragment {

    private static final String TAG_LOG = DebtsFragment.class.getSimpleName();
    private GroupModel mGroup;
    private DebtsAdapter mAdapter;
    private ListView mYourDebtsList;
    // TODO: Customize parameters
    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    private OnListFragmentInteractionListener mListener;

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DebtsFragment newInstance(int columnCount, GroupModel group) {
        DebtsFragment fragment = new DebtsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putParcelable(GroupListFragment.PASSING_GROUP_TAG, group);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DebtsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mGroup = getArguments().getParcelable(GroupListFragment.PASSING_GROUP_TAG);
        }

        //per poter popolare l'action bar dell'activity
        //setHasOptionsMenu(true);
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
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        /*
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new TransactionRecyclerViewAdapter(DummyTransactionContent.ITEMS, mListener));
        }*/
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new DebtsAdapter(getContext(), mGroup.getId());
        mYourDebtsList = (ListView) view.findViewById(R.id.transaction_list);
        mYourDebtsList.setEmptyView(view.findViewById(R.id.transaction_fragment_empty_view));
        mYourDebtsList.setOnTouchListener(new OnSwipeTouchListener(getActivity(), mYourDebtsList){

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

        mYourDebtsList.setAdapter(mAdapter);

        // FIXME: 22/06/2016 e se facessimo un long click listener?
     /*   mYourDebtsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO: 20/06/2016  GESTIRE ON CLICK
                Toast.makeText(MyApplication.getAppContext(), "Posizione " + position,Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(getContext(), PaymentDetailsActivity.class);
                PaymentModel cost = (PaymentModel) mAdapter.getItem(position);

                i.putExtra(PASSING_PAYMENT_TAG, cost);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
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
        void onListFragmentInteraction(Debt item);
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
