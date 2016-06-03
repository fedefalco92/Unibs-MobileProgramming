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

}
