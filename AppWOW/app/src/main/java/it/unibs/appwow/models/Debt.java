package it.unibs.appwow.models;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class Debt extends DebtModel {

    private String mFullNameFrom;
    private String mFullNameTo;
    private String mEmailFrom;
    private String mEmailTo;

    public Debt(int id, int idBalancing, int idFrom, int idTo, double amount, String fullNameFrom, String fullNameTo, String emailFrom, String emailTo) {
        super(id, idBalancing, idFrom, idTo, amount);
        this.mFullNameFrom = fullNameFrom;
        this.mFullNameTo = fullNameTo;
        this.mEmailFrom = emailFrom;
        this.mEmailTo = emailTo;
    }

    public String getFullNameFrom() {
        return mFullNameFrom;
    }

    public void setFullNameFrom(String fullNameFrom) {
        mFullNameFrom = fullNameFrom;
    }

    public String getFullNameTo() {
        return mFullNameTo;
    }

    public void setFullNameTo(String fullNameTo) {
        mFullNameTo = fullNameTo;
    }

    public String getEmailFrom() {
        return mEmailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        mEmailFrom = emailFrom;
    }

    public String getEmailTo() {
        return mEmailTo;
    }

    public void setEmailTo(String emailTo) {
        mEmailTo = emailTo;
    }
}
