package it.unibs.appwow.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alessandro on 23/06/2016.
 */
public class DecimalDigitsInputFilter implements InputFilter {

    Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
        mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        /*String pattern = "[0-9]{0," + (digitsBeforeZero-1) + "}";
        if(digitsAfterZero>0){
            pattern.concat("+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }
        mPattern=Pattern.compile(pattern);*/

    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        Matcher matcher=mPattern.matcher(dest);
        if(!matcher.matches())
            return "";
        return null;
    }

}