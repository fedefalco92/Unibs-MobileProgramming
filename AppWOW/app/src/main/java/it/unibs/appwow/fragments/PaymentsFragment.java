package it.unibs.appwow.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import it.unibs.appwow.PaymentDetailsActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.views.adapters.PaymentAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PaymentsFragment extends Fragment implements AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener {

    private static final String TAG_LOG = PaymentsFragment.class.getSimpleName();

    public static final String PASSING_GROUP_TAG = "group";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String PASSING_PAYMENT_TAG = "cost";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private PaymentAdapter mAdapter;
    private GroupModel mGroup;
    private ListView mPaymentsListView;
    //private Payment mSelectedItem;
    //private List<PaymentModel> mPaymentsListView; //da riempire

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PaymentsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PaymentsFragment newInstance(int columnCount, GroupModel group) {
        PaymentsFragment fragment = new PaymentsFragment();
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

    /*
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
                final Intent i = new Intent(getContext(), AddPaymentActivity.class);
                i.putExtra(PASSING_GROUP_TAG, mGroup);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_list, container, false);
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
        mAdapter = new PaymentAdapter(getContext(), mGroup.getId());
        mPaymentsListView = (ListView) view.findViewById(R.id.payment_list);
        mPaymentsListView.setEmptyView(view.findViewById(R.id.payment_fragment_empty_view));
        mPaymentsListView.setAdapter(mAdapter);
        mPaymentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(GroupActivity.this, "Posizione" + position,Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(getContext(), PaymentDetailsActivity.class);
                Payment payment = (Payment) mAdapter.getItem(position);
                i.putExtra(PASSING_PAYMENT_TAG, payment);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        mPaymentsListView.setOnItemLongClickListener(this);
        mPaymentsListView.setOnScrollListener(this);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
        final Payment selectedItem = (Payment) mAdapter.getItem(position);
        final int pos = position;
        view.setSelected(true);
        Resources res = getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(res.getString(R.string.payment_delete_title));
        builder.setMessage(String.format(res.getString(R.string.payment_delete_message), selectedItem.getName()));
        builder.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                mAdapter.remove(pos);
                showUndoSnackbar(selectedItem);
                view.setSelected(false);
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

    private void showUndoSnackbar(final Payment selectedItem){
        final Snackbar snackbar = Snackbar.make(getView(), String.format(getResources().getString(R.string.payment_deleted), selectedItem.getName()) , Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mAdapter.reload();
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if(event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_SWIPE){
                    sendDeleteRequest(selectedItem);
                }
            }
        });
        snackbar.show();
    }

    private void sendDeleteRequest(final Payment selectedItem) {
        URL url = WebServiceUri.uriToUrl(WebServiceUri.getDeletePaymentUri(selectedItem.getId()));
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.DELETE, url.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                boolean result = false;
                try {
                    JSONObject obj = new JSONObject(response);
                    String stringresult = obj.getString("success");
                    result = Boolean.parseBoolean(stringresult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(result){
                    //eliminazione dal db locale
                    /*PaymentDAO dao = new PaymentDAO();
                    dao.open();
                    dao.removeSinglePayment(selectedItem.getId());
                    dao.close();*/
                    ((GroupDetailsActivity) getActivity()).onRefresh();
                } else {
                    mAdapter.reload();
                    showUnableToRemoveSnackbar(selectedItem, WebServiceUri.SERVER_ERROR);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_LOG, "VOLLEY ERROR: " + error);
                mAdapter.reload();
                showUnableToRemoveSnackbar(selectedItem, WebServiceUri.NETWORK_ERROR);
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private void showUnableToRemoveSnackbar(final Payment selectedItem, int errorType){
        String msg = "";
        switch (errorType){
            case WebServiceUri.SERVER_ERROR:
                msg = String.format(getResources().getString(R.string.payment_delete_unsuccess_server_error), selectedItem.getName());
                break;
            case WebServiceUri.NETWORK_ERROR:
                msg = String.format(getResources().getString(R.string.payment_delete_unsuccess_network_error), selectedItem.getName());
        }
        final Snackbar snackbar = Snackbar.make(getView(), msg , Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.retry, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mAdapter.remove(selectedItem);
                sendDeleteRequest(selectedItem);
            }
        });
        snackbar.show();
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
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int topRowVerticalPosition = (mPaymentsListView == null || mPaymentsListView.getChildCount() == 0) ?
                        0 : mPaymentsListView.getChildAt(0).getTop();
        ((GroupDetailsActivity) getActivity()).getSwipeRefreshLayout().setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
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
