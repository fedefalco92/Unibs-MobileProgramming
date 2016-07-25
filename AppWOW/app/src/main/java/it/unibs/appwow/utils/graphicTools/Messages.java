package it.unibs.appwow.utils.graphicTools;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import it.unibs.appwow.R;

/**
 * Created by federicofalcone on 25/07/16.
 */
public class Messages {

    public static void showSnackbarWithAction(View view, int idStringMsg, int idStringAction, View.OnClickListener listener){
        Snackbar sn = Snackbar.make(view, idStringMsg, Snackbar.LENGTH_SHORT);
        sn.setAction(idStringAction, listener);
        sn.show();
    }

    public static void showSnackbar(View view, int idStringMsg){
        Snackbar sn = Snackbar.make(view, idStringMsg, Snackbar.LENGTH_SHORT);
        sn.show();
    }
}
