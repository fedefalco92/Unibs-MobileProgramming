package it.unibs.appwow.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.views.adapters.GroupAdapter;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceUri;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG_LOG = GroupListFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER= "user";
    public static final String PASSING_GROUP_TAG = "group";

    //parameters
    private LocalUser mLocalUser;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridView mGridView;
    private GroupAdapter mAdapter;

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
        if (getArguments() != null) {
            mLocalUser = getArguments().getParcelable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_group, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new GroupAdapter(getContext());
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.groups_swipe_refresh_layout);
        mGridView = (GridView)view.findViewById(R.id.gridview_groups);
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(GroupActivity.this, "Posizione" + position,Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(getContext(), GroupDetailsActivity.class);
                GroupModel group = (GroupModel) mAdapter.getItem(position);

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

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                //finish();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Richiamo questo metodo per fare un refresh una volta aperta l'activity
        //fetchGroups();
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSwipeRefreshLayout.setRefreshing(true);
                                        fetchGroups();
                                    }
                                }
        );

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        mListener = null;
    }

    @Override
    public void onRefresh() {
        fetchGroups();
    }

    private void fetchGroups(){
        Log.d(TAG_LOG,"fetchGroups");
        // showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);

        if(mLocalUser != null){
            Log.d(TAG_LOG,"mLocalUser is not null");
            Uri user_uri = Uri.withAppendedPath(WebServiceUri.USERS_URI, String.valueOf(mLocalUser.getId()));
            Uri groups_uri = Uri.withAppendedPath(user_uri, "groups");
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

                            if (response.length() > 0) {
                                GroupDAO dao = new GroupDAO();
                                dao.open();
                                dao.resetAllGroups();
                                for(int i = 0; i < response.length(); i++){
                                    try {
                                        JSONObject groupJs = response.getJSONObject(i);
                                        int id = groupJs.getInt("id");
                                        String server_updated_at_string = groupJs.getString("updated_at");
                                        long server_updated_at = DateUtils.dateStringToLong(server_updated_at_string);
                                        long local_updated_at = dao.getUpdatedAt(id);
                                        //aggiorno il gruppo solo se ha subito modifiche, SE IL GRUPPO NON ESISTE local_updated_at = 0L
                                        //quindi il nuovo gruppo viene inserito
                                        Log.d(TAG_LOG, "Group id: " + id + " SERVER UPD: " + server_updated_at_string + " LOCAL UPD: " + DateUtils.dateLongToString(local_updated_at));
                                        if (server_updated_at > local_updated_at) {
                                            String name = groupJs.getString("name");
                                            int idAdmin = groupJs.getInt("idAdmin");
                                            String created_at_string = groupJs.getString("created_at");
                                            long created_at = DateUtils.dateStringToLong(created_at_string);
                                            //JSONObject pivot = groupJs.getJSONObject("pivot");
                                            GroupModel group = GroupModel.create(name).withId(id).withAdmin(idAdmin);
                                            group.setCreatedAt(created_at);
                                            //group.setUpdatedAt(server_updated_at);
                                            //NON AGGIORNO UPDATED AT per ora in modo che quando entro nei dettagli il gruppo venga riaggiornato
                                            group.setUpdatedAt(local_updated_at);
                                            group.highlight();
                                            dao.insertGroup(group);
                                        } else {
                                            //per ora non faccio niente
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                dao.close();
                               // mAdapter.notifyDataSetChanged();
                                mAdapter = new GroupAdapter(getActivity());
                                mGridView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_LONG).show();
                            }
                            // stopping swipe refresh
                            mSwipeRefreshLayout.setRefreshing(false);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());

                            Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_LONG).show();

                            // stopping swipe refresh
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });

            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(req);
        } else {
            // stopping swipe refresh
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), getString(R.string.toast_message_nothing_to_show), Toast.LENGTH_LONG).show();
        }
    }

/*
    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }
*/
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
        mAdapter = new GroupAdapter(MyApplication.getAppContext());
        mGridView.setAdapter(mAdapter);
        //Log.d(TAG_LOG, "on resume completed");
    }
}
