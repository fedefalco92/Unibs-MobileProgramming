package it.unibs.appwow.utils;

import android.util.Patterns;

/**
 * Created by federicofalcone on 03/06/16.
 */
public class Validator {

    private static final String TAG_LOG = Validator.class.getSimpleName();

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    public static boolean isGroupNameValid(String groupName) {
        if(!groupName.isEmpty() && groupName.length()<=30){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCostNameValid(String costName) {
        if(!costName.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAmountValid(String amount) {
        if(!amount.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFullNameValid(String fullname){
        if(!fullname.isEmpty()){
            return true;
        } else {
            return false;
        }
    }
}
