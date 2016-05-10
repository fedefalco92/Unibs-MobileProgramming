package it.unibs.appwow.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibs.appwow.model.Amount;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyAmountContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Amount> ITEMS = new ArrayList<Amount>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Long, Amount> ITEM_MAP = new HashMap<Long, Amount>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Amount item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Amount createDummyItem(int position) {
        return new Amount((long) position, "Utente " + position, makeDouble(position));
    }

    private static double makeDouble(int position) {
        return Math.random();
    }

}
