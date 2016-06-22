package it.unibs.appwow.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import it.unibs.appwow.AddCostActivity;
import it.unibs.appwow.CostDetailsActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.models.parc.CostModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.views.adapters.CostAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CostsFragment extends Fragment {

    private static final String TAG_LOG = CostsFragment.class.getSimpleName();
    public static final String PASSING_GROUP_TAG = "group";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String PASSING_COST_TAG = "cost";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private CostAdapter mAdapter;
    private GroupModel mGroup;
    private ListView mCostList;
    //private List<CostModel> mCostList; //da riempire

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CostsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CostsFragment newInstance(int columnCount, GroupModel group) {
        CostsFragment fragment = new CostsFragment();
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

        //per poter popolare l'action bar dell'activity
        setHasOptionsMenu(true);

        //mAdapter =  new CostAdapter(getActivity(), mGroup.getId());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.costs_fragment_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.add_new_cost:
                //Toast.makeText(getContext(), "Add item", Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(getContext(), AddCostActivity.class);
                i.putExtra(PASSING_GROUP_TAG, mGroup);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cost_list, container, false);
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
            recyclerView.setAdapter(new CostRecyclerViewAdapter(DummyCostContent.ITEMS, mListener)); // FIXME: 06/05/2016 da mettere contenuto giusto (listCost)
            //recyclerView.setAdapter(new CostRecyclerViewAdapter(mListener));
            //recyclerView.setAdapter(mAdapter);
        }*/
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new CostAdapter(getContext(), mGroup.getId());
        mCostList = (ListView) view.findViewById(R.id.cost_list);
        mCostList.setAdapter(mAdapter);

        mCostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(GroupActivity.this, "Posizione" + position,Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(getContext(), CostDetailsActivity.class);
                CostModel cost = (CostModel) mAdapter.getItem(position);

                i.putExtra(PASSING_COST_TAG, cost);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
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
        void onListFragmentInteraction(CostModel item);
    }
}
