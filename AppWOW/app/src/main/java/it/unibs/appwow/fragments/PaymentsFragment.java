package it.unibs.appwow.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.MyApplication;
import it.unibs.appwow.PaymentDetailsActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.database.PaymentDAO;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.graphicTools.DividerItemDecoration;
import it.unibs.appwow.utils.graphicTools.Messages;
import it.unibs.appwow.views.adapters.PaymentAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PaymentsFragment extends Fragment implements PaymentAdapter.OnItemClickListener, PaymentAdapter.OnItemLongClickListener, SearchView.OnQueryTextListener{

    private static final String TAG_LOG = PaymentsFragment.class.getSimpleName();

    public static final String PASSING_GROUP_TAG = "group";
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String PASSING_PAYMENT_TAG = "cost";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private GroupModel mGroup;
    private LocalUser mLocalUser;
    private ListView mPaymentsListView;

    // Nuove variabili per recycler view.
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PaymentAdapter mAdapter;

    private List<Payment> mItems;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PaymentsFragment() {

    }

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
        //per poter popolare l'action bar dell'activity
        setHasOptionsMenu(true);

        mLocalUser = LocalUser.load(getActivity());

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mGroup = getArguments().getParcelable(PASSING_GROUP_TAG);
        }

        /*
        PaymentDAO dao;
        dao = new PaymentDAO();
        dao.open();
        mItems = dao.getAllPayments(mGroup.getId());
        dao.close();*/
    }

    @Override
    public void onResume() {
        super.onResume();

        // Serve per allineare il filtraggio degli elementi
        PaymentDAO dao;
        dao = new PaymentDAO();
        dao.open();
        mItems = dao.getAllPayments(mGroup.getId());
        dao.close();

        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_payments_fragment, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(this);
        }

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.filter:
                boolean isChecked = item.isChecked();
                if(isChecked){
                    Log.d(TAG_LOG,"isChecked");
                    item.setChecked(false);;
                    mAdapter.animateTo(mItems);
                } else {
                    Log.d(TAG_LOG,"isNotChecked");
                    item.setChecked(true);
                    final List<Payment> filteredModelList = filterMyPayments(mItems);
                    mAdapter.animateTo(filteredModelList);
                    mRecyclerView.scrollToPosition(0);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.menu_payments_fragment,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.add_new_cost:
                //Toast.makeText(getContext(), "Add item", Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(getContext(), AddEditPaymentActivity.class);
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
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_LOG,"onViewCreated");

        mRecyclerView = (RecyclerView) view.findViewById(R.id.payment_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setSmoothScrollbarEnabled(true);

        // specify an adapter
        mAdapter = new PaymentAdapter(getContext(),mGroup.getId());
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));


        TextView emptyTextView = (TextView) view.findViewById(R.id.payment_fragment_empty_view);
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

    private void showUndoSnackbar(final Payment selectedItem){
        final Snackbar snackbar = Snackbar.make(getView(), String.format(getResources().getString(R.string.message_payment_deleting_param), selectedItem.getName()) , Snackbar.LENGTH_SHORT);
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
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(getView(),R.string.error_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    sendDeleteRequest(selectedItem);
                }
            });
            return;
        }

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
                    mItems.remove(selectedItem);
                    //((GroupDetailsActivity) getActivity()).onRefresh();
                    ((GroupDetailsActivity) getActivity()).onUpdate();
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
                msg = String.format(getResources().getString(R.string.error_payment_delete_server_error), selectedItem.getName());
                break;
            case WebServiceUri.NETWORK_ERROR:
                msg = String.format(getResources().getString(R.string.error_payment_delete_network_error), selectedItem.getName());
        }
        final Snackbar snackbar = Snackbar.make(getView(), msg , Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.retry, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mAdapter.removeItem(selectedItem);
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
    public void onItemClicked(View v, int position) {
        Log.d(TAG_LOG, "onItemClick position: " + position);
        //Toast.makeText(GroupActivity.this, "Posizione" + position,Toast.LENGTH_SHORT).show();
        final Intent i = new Intent(MyApplication.getAppContext(), PaymentDetailsActivity.class);
        Payment payment = (Payment) mAdapter.getItem(position);
        i.putExtra(PASSING_PAYMENT_TAG, payment);
        i.putExtra(PASSING_GROUP_TAG, mGroup);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getAppContext().startActivity(i);

        /*
        TextView cname = (TextView) v.findViewById(payment_fragment_item_costname);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), cname, "payment_transaction");
        ActivityCompat.startActivity(getActivity(),i, options.toBundle());*/

    }

    @Override
    public boolean onItemLongClicked(final View v, int position) {
        Log.d(TAG_LOG, "onItemLongClick position: " + position);
        final Payment selectedItem = (Payment) mAdapter.getItem(position);

        if(mLocalUser.getId() == selectedItem.getIdUser()){
            Vibrator vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if(vib.hasVibrator()) vib.vibrate(50);

            final int pos = position;
            v.setSelected(true);
            Resources res = getResources();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(res.getString(R.string.message_payment_delete_title));
            builder.setMessage(String.format(res.getString(R.string.message_payment_delete), selectedItem.getName()));
            builder.setPositiveButton(res.getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    mAdapter.removeItem(pos);
                    showUndoSnackbar(selectedItem);
                    v.setSelected(false);
                }
            });
            builder.setNegativeButton(res.getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                    v.setSelected(false);
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    v.setSelected(false);
                }
            });
            builder.show();
        }
        return true;

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG_LOG,"onQueryTextChange");
        final List<Payment> filteredModelList = filter(mItems, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<Payment> filter(List<Payment> models, String query) {
        query = query.toLowerCase();

        final List<Payment> filteredModelList = new ArrayList<>();
        for (Payment model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private List<Payment> filterIsNotExchange(List<Payment> items){
        final List<Payment> filteredItemList = new ArrayList<>();
        for (Payment item: items){
            if(!item.isExchange()){
                filteredItemList.add(item);
            }
        }
        return filteredItemList;
    }

    private List<Payment> filterMyPayments(List<Payment> items){
        final List<Payment> filteredItemList = new ArrayList<>();
        for (Payment item: items){
            if(item.getIdUser() == mLocalUser.getId()){
                filteredItemList.add(item);
            }
        }
        return filteredItemList;
    }


    /*
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int topRowVerticalPosition = (mPaymentsListView == null || mPaymentsListView.getChildCount() == 0) ?
                        0 : mPaymentsListView.getChildAt(0).getTop();
        ((GroupDetailsActivity) getActivity()).getSwipeRefreshLayout().setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href="http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(PaymentModel item);
    }
}
