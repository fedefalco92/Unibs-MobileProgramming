package it.unibs.appwow.models;

/**
 * Created by Massi on 10/05/2016.
 */
public class Amount {
    public int id;
    public String fullName;
    public double amount;

    public Amount(int id, String fullname, double amount) {
        this.id = id;
        this.fullName = fullname;
        this.amount = amount;
    }
}
