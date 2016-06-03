package it.unibs.appwow.utils.dummy;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Transaction> ITEMS = new ArrayList<Transaction>();



    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Transaction item) {
        ITEMS.add(item);
    }

    private static Transaction createDummyItem(int position) {
        return new Transaction(Math.random()*100%50, "a/da" , "Marco");
    }


    /**
     * A dummy item representing a piece of content.
     */
    public static class Transaction {
        public double amount;
        public String preposition;
        public String user;

        public Transaction(double amount, String prep, String user) {
            this.amount = amount;
            this.preposition = prep;
            this.user = user;
        }
    }
}
