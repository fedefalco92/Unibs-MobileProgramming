package it.unibs.appwow.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.acl.Group;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.utils.FileUtils;
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
    // TODO: 07/07/2016 TOGLIERE ADAPTER!!!!! IN QUESTO MODO E' POSSIBILE TENERE I RIFERIMENTI ALLE VIEWS E QUINDI GESTIRE L'AGGIORNAMENTO FOTO


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
        return inflater.inflate(R.layout.content_group, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_LOG,"onViewCreated");
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
        // Viene chiamato onResume
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
    public void onRefresh() {
        Log.d(TAG_LOG,"onRefresh");
        fetchGroups();
    }


    private void fetchGroups(){
        Log.d(TAG_LOG,"fetchGroups");
        // showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);

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
                                            // FIXME: 05/07/2016 la foto non si aggiorna subito
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                mAdapter = new GroupAdapter(getActivity());
                                mGridView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();

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
                                refreshGrid();
                            }
                            dao.close();

                            Log.d(TAG_LOG, "GRUPPI RICEVUTI: " + gruppiRicevuti);
                            Log.d(TAG_LOG, "GRUPPI LOCALI: " + gruppiLocali);
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

    private void fetchPhoto(final int idGroup, final long server_photo_updated_at) {
        Log.d(TAG_LOG,"fetchPhoto");
        // FIXME: 04/07/2016 COMMENTED FOR DEBUG
        Uri photoUri = WebServiceUri.getGroupPhotosUri(idGroup);
        //String url = "https://upload.wikimedia.org/wikipedia/commons/e/e8/Jessica_Chastain_by_Gage_Skidmore.jpg";
        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(photoUri.toString(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        boolean success = FileUtils.writeGroupImage(idGroup, bitmap, getActivity());
                        if(success) {
                            GroupDAO dao = new GroupDAO();
                            dao.open();
                            dao.setPhotoFileName(idGroup, FileUtils.getGroupImageFileName(idGroup));
                            dao.touchGroupPhoto(idGroup, server_photo_updated_at);
                            dao.close();
                        }
                        mAdapter.notifyDataSetChanged();
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
        MyApplication.getInstance().addToRequestQueue(request);
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
        //fetchGroups();
        refreshGrid();
        //Log.d(TAG_LOG, "on resume completed");
    }

    private void refreshGrid() {
        Log.d(TAG_LOG,"refreshGrid");
        mAdapter = new GroupAdapter(MyApplication.getAppContext());
        mGridView.setAdapter(mAdapter);
    }

    /*
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private boolean noGroups = false;
        private boolean mConnError = false;
        private LocalUser mLocalUser = null;

        UserLoginTask() {
            mLocalUser = LocalUser.load(getActivity());
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String response = "";
            Uri groups_uri = WebServiceUri.getGroupsUri(mLocalUser.getId());
            try {
                URL url = new URL(groups_uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.connect();
                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String line = "";
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                } else {
                    mConnError = true;
                    return true;
                }

                if(!response.isEmpty()){
                    //response = response.substring(1,response.length()-1);
                    mResjs = new JSONObject(response);
                    Log.d(TAG_LOG,"Risposta" + mResjs.toString(1));
                } else {
                    return false;
                }
                Log.d(TAG_LOG, "Risposta String: "+ response);

                // TODO: 19/05/2016 SALVARE SHARED
                int id = mResjs.getInt("id");
                String fullname = mResjs.getString("fullName");
                mLocalUser = LocalUser.create(id).withEmail(mEmail).withFullName(fullname);
                mLocalUser.save(MyApplication.getAppContext());

            } catch (MalformedURLException e){
                return false;
            } catch (IOException e){
                return false;
            } catch (JSONException e){
                e.printStackTrace();
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                if(mNewUser){
                    Intent ri = new Intent(LoginActivity.this, RegistrationActivity.class);
                    ri.putExtra(PASSING_USER_EXTRA, mLocalUser);
                    startActivity(ri);
                } else {
                    Intent i = new Intent(LoginActivity.this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                }
            } else {
                if(!mConnError){
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }

        private int checkUser(String email){
            String response = "";
            Uri uri = WebServiceUri.CHECK_USER_URI;
            try {
                URL url = new URL(uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("email", mEmail);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.i(TAG_LOG,"Response code = " + responseCode);
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String line = "";
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                } else {
                    return CONN_ERROR;
                }

                if(!response.isEmpty()){
                    Log.d(TAG_LOG,"RISPOSTA_CHECK_USER" + response);
                    return USER_EXISTS;
                } else {
                    return USER_NOT_EXISTS;
                }

            } catch (MalformedURLException e){
                return CONN_ERROR;
            } catch (IOException e){
                return CONN_ERROR;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/
}
