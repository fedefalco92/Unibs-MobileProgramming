package it.unibs.appwow.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import it.unibs.appwow.views.adapters.DebtsAdapter;

/**
 * A fragment representing a list of Items.
 * <p />
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DebtsFragment extends Fragment implements AdapterView.OnItemLongClickListener{

    private static final String TAG_LOG = DebtsFragment.class.getSimpleName();
    private GroupModel mGroup;
    private DebtsAdapter mAdapter;
    private ListView mYourDebtsList;
    private boolean mShowOnlyYourDebts;
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mShowOnlyYourDebts = prefs.getBoolean("pref_key_show_debts", false);
        mAdapter = new DebtsAdapter(getContext(), mGroup.getId(), mShowOnlyYourDebts);

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
        View view = inflater.inflate(R.layout.fragment_debts_list, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mYourDebtsList = (ListView) view.findViewById(R.id.transaction_list);
        mYourDebtsList.setEmptyView(view.findViewById(R.id.transaction_fragment_empty_view));
        mYourDebtsList.setOnItemLongClickListener(this);
        mYourDebtsList.setAdapter(mAdapter);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
        final Debt selectedItem = (Debt) mAdapter.getItem(position);
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
