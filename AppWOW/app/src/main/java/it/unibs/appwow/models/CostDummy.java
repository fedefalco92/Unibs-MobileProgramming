package it.unibs.appwow.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Massi on 06/05/2016.
 */
public class CostDummy {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("E dd MMMM yyyy");
    public long id;
    public double amount;
    public String name;
    public String notes;
    public long createdAt;
    public long updatedAt;
    public long idGroup;
    public long idUser;
    public String position;
    public String amountDetails; // FIXME: 06/05/2016 da sostituire con un vector da riempire al momento dell'importazione dal DB
    public long archivedAt;

    public CostDummy(long id, String name, double amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.createdAt = System.currentTimeMillis();
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name + ": " + amount);
        return builder.toString();
    }
}
