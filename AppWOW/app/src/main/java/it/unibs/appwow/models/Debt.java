package it.unibs.appwow.models;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class Debt extends DebtModel {

    private String mFullNameFrom;
    private String mFullNameTo;

    public Debt(int id, int idBalancing, int idFrom, int idTo, double amount, String fullNameFrom, String fullNameTo) {
        super(id, idBalancing, idFrom, idTo, amount);
        this.mFullNameFrom = fullNameFrom;
        this.mFullNameTo = fullNameTo;
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
}
