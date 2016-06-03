package it.unibs.appwow.utils.dummy;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.models.Cost;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyCostContent {

    private static final String TAG_LOG = DummyCostContent.class.getSimpleName();

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Cost> ITEMS = new ArrayList<Cost>();

    /**
     * A map of sample (dummy) items, by ID.
     */
   // public static final Map<String, Cost> ITEM_MAP = new HashMap<String, Cost>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Cost item) {
        ITEMS.add(item);
        //ITEM_MAP.put(item.id, item);
    }

    private static Cost createDummyItem(int position) {
        return new Cost(position,"Item: " + position,Math.round(Math.random()*position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
