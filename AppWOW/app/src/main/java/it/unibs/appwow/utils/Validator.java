package it.unibs.appwow.utils;

/**
 * Created by federicofalcone on 03/06/16.
 */
public class Validator {

    private static final String TAG_LOG = Validator.class.getSimpleName();

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;// password.length() > 4;
    }

    public static boolean isGroupNameValid(String groupName) {
        if(!groupName.isEmpty()){
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
