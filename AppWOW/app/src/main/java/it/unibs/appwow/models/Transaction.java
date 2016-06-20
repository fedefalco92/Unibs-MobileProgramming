package it.unibs.appwow.models;

/**
 * Created by Alessandro on 20/06/2016.
 */
public class Transaction extends TransactionModel {

    private String mFullName;

    public Transaction(int id, int idBalancing, int idFrom, int idTo, double amount, long payedAt) {
        super(id, idBalancing, idFrom, idTo, amount, payedAt);
    }

    public Transaction(int id, int idBalancing, int idFrom, int idTo, double amount, long payedAt, String fullName) {
        super(id, idBalancing, idFrom, idTo, amount, payedAt);
        this.mFullName = fullName;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }
}
