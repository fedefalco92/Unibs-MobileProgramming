package it.unibs.appwow.models;

/**
 * Created by Massi on 10/05/2016.
 */
public class Amount {
    public long id;
    public String fullname;
    public double amount;

    public Amount(long id, String fullname, double amount) {
        this.id = id;
        this.fullname = fullname;
        this.amount = amount;
    }
}
