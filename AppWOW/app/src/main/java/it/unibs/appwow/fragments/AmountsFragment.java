package it.unibs.appwow.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import it.unibs.appwow.R;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.views.adapters.AmountItemAdapter;
import it.unibs.appwow.models.Amount;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AmountsFragment extends Fragment {

    private static final String TAG_LOG = AmountsFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String PASSING_GROUP_TAG = "group";
    private static final String ARG_LOCAL_USER_ID = "local_usesr_id";

    private int mColumnCount = 1;
    private int mLocalUserId;
    private OnListFragmentInteractionListener mListener;
    private AmountItemAdapter mAdapter;
    private GroupModel mGroup;
    private ListView mAmountList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AmountsFragment() {
    }

    @SuppressWarnings("unused")
    public static AmountsFragment newInstance(int columnCount, GroupModel group, int localUserId) {
        AmountsFragment fragment = new AmountsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putParcelable(PASSING_GROUP_TAG, group);
        args.putInt(ARG_LOCAL_USER_ID, localUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mGroup = getArguments().getParcelable(PASSING_GROUP_TAG);
            mLocalUserId = getArguments().getInt(ARG_LOCAL_USER_ID);
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
        View view = inflater.inflate(R.layout.fragment_amount_list, container, false);
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
            recyclerView.setAdapter(new AmountRecyclerViewAdapter(DummyAmountContent.ITEMS, mListener));
        }*/
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_LOG,"onViewCreated");
        mAdapter = new AmountItemAdapter(getContext(), mGroup.getId(), mLocalUserId);
        mAmountList = (ListView) view.findViewById(R.id.amount_list);
        mAmountList.setAdapter(mAdapter);
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
        void onListFragmentInteraction(Amount item);
    }
}
