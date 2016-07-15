package it.unibs.appwow.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.graphicTools.DividerItemDecoration;
import it.unibs.appwow.views.adapters.DebtsAdapter;

/**
 * A fragment representing a list of Items.
 * <p />
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DebtsFragment extends Fragment implements DebtsAdapter.OnItemClickListener, DebtsAdapter.OnItemLongClickListener{

    private static final String TAG_LOG = DebtsFragment.class.getSimpleName();
    private GroupModel mGroup;
    private boolean mShowOnlyYourDebts;
    // TODO: Customize parameters
    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    private OnListFragmentInteractionListener mListener;

    // Nuove variabili per recycler view.
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private DebtsAdapter mAdapter;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mShowOnlyYourDebts = prefs.getBoolean("pref_key_show_debts", false);
        //per poter popolare l'action bar dell'activity
        //setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG_LOG, "onResume()");
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
            //mAdapter.reloadItems();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts_list_recycler, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_LOG,"onViewCreated");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.debts_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setSmoothScrollbarEnabled(true);

        // specify an adapter
        mAdapter = new DebtsAdapter(getContext(), mGroup.getId(), mShowOnlyYourDebts);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        //mAdapter.reloadItems();
        //mAdapter.notifyDataSetChanged();

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));


        TextView emptyTextView = (TextView) view.findViewById(R.id.debt_fragment_empty_view);
        // If there is no payments
        if (mAdapter.getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
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
    public boolean onItemLongClick(final View view, int position) {
        final Debt selectedItem = (Debt) mAdapter.getItem(position);
        Log.d(TAG_LOG, "onItemLongClick position: " + position);

        final int pos = position;
        view.setSelected(true);
        Resources res = getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(res.getString(R.string.debt_settle_title));
        builder.setMessage(String.format(res.getString(R.string.debt_settle_message), selectedItem.getFullNameFrom(), Amount.getAmountString(selectedItem.getAmount())));
        builder.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                showProgressDialog(selectedItem);
            }
        });
        builder.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                view.setSelected(false);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                view.setSelected(false);
            }
        });
        builder.show();
        return true;
    }*/

    private void showProgressDialog(Debt selectedItem) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.debt_settling));
        progressDialog.setCancelable(false);
        progressDialog.show();
        sendSettleRequest(selectedItem, progressDialog);
    }

    private void sendSettleRequest(final Debt selectedItem, final ProgressDialog dialog) {
        URL url = WebServiceUri.uriToUrl(WebServiceUri.getGroupDebtsUri(mGroup.getId()));
        String [] keys = {"id"};
        String [] values = {String.valueOf(selectedItem.getId())};

        StringRequest req = WebServiceRequest.stringRequest(Request.Method.POST, url.toString(), WebServiceRequest.createParametersMap(keys, values), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG_LOG,"SettleRequest Response:" + response);
                PaymentModel p = null;
                try {
                    JSONObject obj = new JSONObject(response);
                    p = PaymentModel.create(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(p!=null){
                    dialog.dismiss();
                    ((GroupDetailsActivity) getActivity()).onRefresh();
                } else {
                    dialog.dismiss();
                    showUnableToRemoveSnackbar(selectedItem, WebServiceUri.SERVER_ERROR);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_LOG, "VOLLEY ERROR: " + error);
                dialog.dismiss();
                showUnableToRemoveSnackbar(selectedItem, WebServiceUri.NETWORK_ERROR);
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private void showUnableToRemoveSnackbar(final Debt selectedItem, int errorType){
        String msg = "";
        switch (errorType){
            case WebServiceUri.SERVER_ERROR:
                msg = String.format(getResources().getString(R.string.debt_settle_unsuccess_server_error), selectedItem.getFullNameFrom());
                break;
            case WebServiceUri.NETWORK_ERROR:
                msg = String.format(getResources().getString(R.string.debt_settle_unsuccess_network_error), selectedItem.getFullNameFrom());
        }
        final Snackbar snackbar = Snackbar.make(getView(), msg , Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.retry, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               showProgressDialog(selectedItem);
            }
        });
        snackbar.show();
    }

    @Override
    public void onItemClicked(View v, int position) {
        Log.d(TAG_LOG,"onItemClicked position: " + position);
    }

    @Override
    public boolean onItemLongClicked(final View view, int position) {
        final Debt selectedItem = (Debt) mAdapter.getItem(position);
        Log.d(TAG_LOG, "onItemLongClick position: " + position);
        Vibrator vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if(vib.hasVibrator()) vib.vibrate(50);

        final int pos = position;
        view.setSelected(true);
        Resources res = getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(res.getString(R.string.debt_settle_title));
        builder.setMessage(String.format(res.getString(R.string.debt_settle_message), selectedItem.getFullNameFrom(), Amount.getAmountString(selectedItem.getAmount())));
        builder.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                showProgressDialog(selectedItem);
            }
        });
        builder.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                view.setSelected(false);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                view.setSelected(false);
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
        void onListFragmentInteraction(Debt item);
    }

}
