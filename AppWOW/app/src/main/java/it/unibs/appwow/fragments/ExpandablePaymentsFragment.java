package it.unibs.appwow.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import it.unibs.appwow.R;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.views.adapters.ExpandablePaymentAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ExpandablePaymentsFragment extends Fragment implements AdapterView.OnItemLongClickListener /*,ActionMode.Callback */{

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
    private Payment mSelectedItem;

    private ActionMode mActionMode;

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
         mPaymentsListView.setAdapter(mAdapter);

         mPaymentsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mPaymentsListView.setOnItemLongClickListener(this);
        mPaymentsListView.setSelector(R.drawable.add_group_member_list_item_background);

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

        /*
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu_selection, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()){
            case R.id.context_delete:
                Log.d(TAG_LOG, "mPaymentsList size before: " + mPaymentsList.size());
                mPaymentsList.remove(mSelectedItem);
                Log.d(TAG_LOG, "mPaymentsList size after: " + mPaymentsList.size());
                getActivity().invalidateOptionsMenu();

                //mAdapter.notifyDataSetChanged();
                mode.finish();
                return true;
            default:
                mode.finish();
                return true;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mSelectedItem = null;
        mActionMode = null;
        mAdapter.notifyDataSetChanged();
    }

*/
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectedItem = mAdapter.getItem(position);
        view.setSelected(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete payment?");

        builder.setMessage(String.format("Do you want to delete the payment %s?", mSelectedItem.getName()));
        final String [] items  = {"Delete", "Cancel"};
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getActivity(),"You clicked yes button",Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
            }
        });
        builder.show();
        return true;
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

}
