package it.unibs.appwow.fragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.PositionUtils;
import it.unibs.appwow.views.adapters.GroupAdapterRecyclerView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupListFragmentRecyclerView.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupListFragmentRecyclerView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupListFragmentRecyclerView extends Fragment implements SwipeRefreshLayout.OnRefreshListener, GroupAdapterRecyclerView.OnItemClickListener, GroupAdapterRecyclerView.OnItemLongClickListener{

    private static final String TAG_LOG = GroupListFragmentRecyclerView.class.getSimpleName();
    private final String TAG_REQUEST_GROUP_LIST = "GROUP_LIST";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER= "user";
    public static final String PASSING_GROUP_TAG = "group";
    public static final int LAYOUT_MANAGER_GRID_NUMBER = 2;

    //parameters
    private LocalUser mLocalUser;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    // Nuove variabili per recycler view.
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private GroupAdapterRecyclerView mAdapter;

    private OnFragmentInteractionListener mListener;

    public GroupListFragmentRecyclerView() {
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
    public static GroupListFragmentRecyclerView newInstance(LocalUser localUser) {
        GroupListFragmentRecyclerView fragment = new GroupListFragmentRecyclerView();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, localUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_LOG,"onCreate");
        if (getArguments() != null) {
            mLocalUser = getArguments().getParcelable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG_LOG,"onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_group_recycler, container, false);
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
        mAdapter = new GroupAdapterRecyclerView(getContext());
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.groups_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mProgressBar = (ProgressBar) view.findViewById(R.id.self_reload_progress_bar);
        // Richiamo questo metodo per fare un refresh una volta aperta l'activity
        //fetchGroups();
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        // Viene chiamato onResume
        /*
        mSwipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSwipeRefreshLayout.setRefreshing(true);
                                        fetchGroups();
                                    }
                                }
        );*/

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG_LOG,"onActivityCreated");
        getActivity().setTitle(R.string.online_groups_string);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onRefresh() {
        Log.d(TAG_LOG,"onRefresh");
        mSwipeRefreshLayout.setRefreshing(true);
        fetchGroups();
    }


    private void fetchGroups(){
        Log.d(TAG_LOG,"fetchGroups");
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
                            // TODO: 04/07/2016  METODO CREATE(jSON) IN GROUPMODEL
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

                                                UserGroupModel ugm = UserGroupModel.create(groupJs.getJSONObject("pivot"));
                                                UserGroupDAO ugdao = new UserGroupDAO();
                                                ugdao.open();
                                                ugdao.insertUserGroup(ugm);
                                                ugdao.close();
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
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                mAdapter.reload();
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.toast_message_nothing_to_show), Toast.LENGTH_LONG).show();
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
                            dao.close();

                            Log.d(TAG_LOG, "GRUPPI RICEVUTI: " + gruppiRicevuti);
                            Log.d(TAG_LOG, "GRUPPI LOCALI: " + gruppiLocali);
                            // stopping swipe refresh
                            mSwipeRefreshLayout.setRefreshing(false);
                            showProgress(false);

                        }
                    }, errorResponseListener());

            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(req,TAG_REQUEST_GROUP_LIST);

        } else {
            // stopping swipe refresh
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), getString(R.string.toast_message_nothing_to_show), Toast.LENGTH_LONG).show();
        }
    }

    private Response.ErrorListener errorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());

                Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_LONG).show();

                // stopping swipe refresh
                mSwipeRefreshLayout.setRefreshing(false);
                showProgress(false);
            }
        };
    }

    private void fetchPhoto(final int idGroup, final long server_photo_updated_at) {
        Log.d(TAG_LOG, "fetchPhoto");
        // FIXME: 04/07/2016 COMMENTED FOR DEBUG
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
                        // TODO: 04/07/2016 NOTIFICARE ALL'ADAPTER........COME?
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG_LOG, "VOLLEY ERROR: " + error);
                    }
                });
        // FIXME: 05/07/2016  è possibile sistemare sta cosa?
       /* int posizioneGruppo = mAdapter.getGroupPosition(idGroup);
        Log.d(TAG_LOG, "posizione gruppo: " + posizioneGruppo);
        View v = mAdapter.getView(posizioneGruppo,null,mGridView);
        ImageView iv = (ImageView) v.findViewById(R.id.group_tile_imageView);
        iv.setImageResource(R.drawable.ic_menu_send);
*/
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
        Log.d(TAG_LOG,"onResume");
        //fetchGroups();
        //refreshGrid();
        //Log.d(TAG_LOG, "on resume completed");
        showProgress(true);
        fetchGroups();
        super.onResume();
    }


    private void showProgress(boolean show){
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }


}
