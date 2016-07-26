package it.unibs.appwow.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.graphicTools.Messages;
import it.unibs.appwow.views.adapters.GroupAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, GroupAdapter.OnItemClickListener, GroupAdapter.OnItemLongClickListener, SearchView.OnQueryTextListener{

    private static final String TAG_LOG = GroupListFragment.class.getSimpleName();
    private final String TAG_REQUEST_GROUP_LIST = "GROUP_LIST";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER= "user";
    public static final String PASSING_GROUP_TAG = "group";
    public static final int LAYOUT_MANAGER_GRID_NUMBER = 2;

    //parameters
    private LocalUser mLocalUser;
    private List<GroupModel> mItems;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    // Variables for recyler view
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private GroupAdapter mAdapter;

    // UI
    private TextView mEmptyTextView;

    private OnFragmentInteractionListener mListener;

    public GroupListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param localUser
     * @return A new instance of fragment GroupListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupListFragment newInstance(LocalUser localUser) {
        GroupListFragment fragment = new GroupListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, localUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.d(TAG_LOG,"onCreate");
        if (getArguments() != null) {
            mLocalUser = getArguments().getParcelable(ARG_USER);
        }

        // Vengono caricati gli elementi dal dao per il filtraggio.
        GroupDAO dao = new GroupDAO();
        dao.open();
        mItems = dao.getAllGroups();
        dao.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG_LOG,"onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_LOG,"onViewCreated");

        mRecyclerView = (RecyclerView) view.findViewById(R.id.groups_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        mLayoutManager = new GridLayoutManager(getContext(),LAYOUT_MANAGER_GRID_NUMBER);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new GroupAdapter(getContext());
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.groups_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mProgressBar = (ProgressBar) view.findViewById(R.id.self_reload_progress_bar);

        mEmptyTextView = (TextView) view.findViewById(R.id.group_fragment_empty_view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG_LOG,"onActivityCreated");
        getActivity().setTitle(R.string.groups_string);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG_LOG,"onAttach");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG_LOG,"onDetach");
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().cancelPendingRequests(TAG_REQUEST_GROUP_LIST);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_groups, menu);

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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG_LOG,"onRefresh");
        mSwipeRefreshLayout.setRefreshing(true);
        fetchGroups();
    }


    private void fetchGroups(){
        Log.d(TAG_LOG,"fetchGroups");
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(getView(),R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    fetchGroups();
                }
            });
            mSwipeRefreshLayout.setRefreshing(false);
            showProgress(false);
            return;
        }

        // showing refresh animation before making http call
        if(mLocalUser != null){
            Log.d(TAG_LOG,"mLocalUser is not null");
            Uri groups_uri = WebServiceUri.getGroupsUri(mLocalUser.getId());
            URL url = null;
            try {
                url = new URL(groups_uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // Volley's json array request object
            JsonArrayRequest req = new JsonArrayRequest(url.toString(),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG_LOG, "Response = " + response.toString());

                            GroupDAO dao = new GroupDAO();
                            dao.open();
                            Set<Integer> gruppiRicevuti = new HashSet<Integer>();
                            Set<Integer> gruppiLocali = dao.getLocalGroupsIds();
                            if (response.length() > 0) {
                                //dao.resetAllGroups();
                                for(int i = 0; i < response.length(); i++){
                                    try {
                                        JSONObject groupJs = response.getJSONObject(i);
                                        GroupModel gserver = GroupModel.create(groupJs);
                                        GroupModel glocal = dao.getSingleGroup(gserver.getId());
                                        gruppiRicevuti.add(gserver.getId());
                                        long server_updated_at = gserver.getUpdatedAt();
                                        long server_photo_updated_at = gserver.getPhotoUpdatedAt();
                                        if(glocal!=null){
                                            long local_updated_at = glocal.getUpdatedAt();
                                            long local_photo_updated_at = glocal.getPhotoUpdatedAt();
                                            if (server_updated_at > local_updated_at) {

                                                //eseguo l'update
                                                int id = glocal.getId();
                                                int idAdmin = gserver.getIdAdmin();
                                                String groupName = gserver.getGroupName();
                                                String photoFileName = glocal.getPhotoFileName();
                                                long photoUpdatedAt = glocal.getPhotoUpdatedAt();
                                                long createdAt = gserver.getCreatedAt();
                                                long updatedAt = glocal.getUpdatedAt();
                                                int highlighted = GroupModel.HIGHLIGHTED;

                                                dao.updateSingleGroup(id,idAdmin, groupName, photoFileName, photoUpdatedAt, createdAt, updatedAt, highlighted);
                                            }

                                            if (server_photo_updated_at > local_photo_updated_at) {
                                                fetchPhoto(gserver.getId(),server_photo_updated_at);
                                            }
                                        } else {
                                            gserver.setUpdatedAt(0L);
                                            gserver.setHighlighted(GroupModel.HIGHLIGHTED);
                                            dao.insertGroup(gserver);
                                            fetchPhoto(gserver.getId(),server_photo_updated_at);
                                        }

                                        // Aggiorno usergroup
                                        UserGroupModel ugm = UserGroupModel.create(groupJs.getJSONObject("pivot"));
                                        UserGroupDAO ugdao = new UserGroupDAO();
                                        ugdao.open();
                                        ugdao.insertUserGroup(ugm);
                                        ugdao.close();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                mAdapter.reload();
                            } else {
                                //Toast.makeText(getActivity(), getString(R.string.message_nothing_to_show), Toast.LENGTH_LONG).show();
                                Messages.showSnackbar(getView(),R.string.message_nothing_to_show);
                            }
                            gruppiLocali.removeAll(gruppiRicevuti);
                            if(!gruppiLocali.isEmpty()){
                                int size = gruppiLocali.size();
                                Iterator i = gruppiLocali.iterator();
                                while(i.hasNext()){
                                    int id = (int) i.next();
                                    //Toast.makeText(getActivity(), "GRUPPO " + id + " ELIMINATO", Toast.LENGTH_SHORT).show();
                                    //alertGroupGone(id, dao);
                                    dao.deleteSingleLGroup(id);
                                }
                                Snackbar snackbar = Snackbar.make(getView(), getResources().getQuantityString(R.plurals.dialog_group_deleted, size, size), Snackbar.LENGTH_INDEFINITE);
                                snackbar.setAction(R.string.ok, new View.OnClickListener(){
                                    @Override
                                    public void onClick(View v) {
                                        //Snackbar snackbar1 = Snackbar.make(getView(), R.string.dialog_group_deleted_confirm, Snackbar.LENGTH_SHORT);
                                        //snackbar1.show();
                                    }
                                });
                                snackbar.show();
                                mAdapter.reload();
                            }
                            mItems = dao.getAllGroups();
                            dao.close();

                            Log.d(TAG_LOG, "GRUPPI RICEVUTI: " + gruppiRicevuti);
                            Log.d(TAG_LOG, "GRUPPI LOCALI: " + gruppiLocali);

                            // stopping swipe refresh
                            mSwipeRefreshLayout.setRefreshing(false);
                            showProgress(false);
                            showEmptyTextView();

                        }
                    }, errorResponseListener());

            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(req,TAG_REQUEST_GROUP_LIST);

        } else {
            // stopping swipe refresh
            mSwipeRefreshLayout.setRefreshing(false);
            showProgress(false);
            showEmptyTextView();
            //Toast.makeText(getActivity(), getString(R.string.toast_message_nothing_to_show), Toast.LENGTH_LONG).show();
            Messages.showSnackbar(getView(),R.string.message_nothing_to_show);
        }
    }

    private Response.ErrorListener errorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());

                GroupDAO dao = new GroupDAO();
                dao.open();
                mItems = dao.getAllGroups();
                dao.close();

                //Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_LONG).show();
                Messages.showSnackbar(getView(),R.string.message_nothing_to_show);

                // stopping swipe refresh
                mSwipeRefreshLayout.setRefreshing(false);
                showProgress(false);
                showEmptyTextView();
            }
        };
    }

    private void fetchPhoto(final int idGroup, final long server_photo_updated_at) {
        Log.d(TAG_LOG, "fetchPhoto");
        Uri photoUri = WebServiceUri.getGroupPhotosUri(idGroup);
        //String url = "https://upload.wikimedia.org/wikipedia/commons/e/e8/Jessica_Chastain_by_Gage_Skidmore.jpg";
        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(photoUri.toString(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        boolean success = FileUtils.writeGroupImage(idGroup, bitmap, MyApplication.getAppContext());
                        if (success) {
                            GroupDAO dao = new GroupDAO();
                            dao.open();
                            dao.setPhotoFileName(idGroup, FileUtils.getGroupImageFileName(idGroup));
                            dao.touchGroupPhoto(idGroup, server_photo_updated_at);
                            dao.close();
                        }
                        mAdapter.updateItem(idGroup);
                        Log.d(TAG_LOG, "FOTO SCARICATA!!");
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG_LOG, "VOLLEY ERROR: " + error);
                    }
                });

        request.setShouldCache(false);
        MyApplication.getInstance().addToRequestQueue(request,TAG_REQUEST_GROUP_LIST);
    }

    @Override
    public void onItemClicked(View v, int position) {
        Log.d(TAG_LOG, "click on " + position);
        GroupModel group = (GroupModel) mAdapter.getItem(position);
        Log.d(TAG_LOG, "group: " + group.getGroupName());
        final Intent i = new Intent(MyApplication.getAppContext(), GroupDetailsActivity.class);

        /**
         * il gruppo che sto passando è highlighted.
         * Userò questa informazione per aggiornare l'intero gruppo in GroupDetailsActivity
         */
        i.putExtra(PASSING_GROUP_TAG, group);

        //tolgo l'highlight dal gruppo NEL DB LOCALE, non nell'oggetto passato
        GroupDAO dao = new GroupDAO();
        dao.open();
        dao.unHighlightGroup(group.getId());
        dao.close();

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getAppContext().startActivity(i);
    }

    @Override
    public boolean onItemLongClicked(View v, int position) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG_LOG,"onQueryTextChange");
        final List<GroupModel> filteredModelList = filter(mItems, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<GroupModel> filter(List<GroupModel> models, String query) {
        query = query.toLowerCase();

        final List<GroupModel> filteredModelList = new ArrayList<>();
        for (GroupModel model : models) {
            final String text = model.getGroupName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    /*
    private void alertGroupGone(int idGroup, GroupDAO dao) {

        /*final String groupName = dao.getGroupName(idGroup);
        final String groupAdmin = dao.getGroupAdminName(idGroup);
        Snackbar snackbar = Snackbar.make(getView(), String.format(getString(R.string.dialog_group_deleted),groupName, groupAdmin), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.ok, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Snackbar snackbar1 = Snackbar.make(getView(), R.string.dialog_group_deleted_confirm, Snackbar.LENGTH_SHORT);
                //snackbar1.show();
            }
        });
        snackbar.show();*/
        /*
        builder.setTitle("Deleted group");
        builder.setMessage(String.format(getString(R.string.dialog_group_deleted),groupName, groupAdmin));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();*//*
    }*/

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG_LOG,"onResume");

        showProgress(true);
        fetchGroups();
    }

    private void showEmptyTextView(){
        Log.d(TAG_LOG,"showEmptyTextView " + mAdapter.getItemCount());
        // If there are no groups
        if(mAdapter != null){
            if (mAdapter.getItemCount() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyTextView.setVisibility(View.GONE);
            }
        }
    }

    private void showProgress(boolean show){
        /*
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);*/
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


}
