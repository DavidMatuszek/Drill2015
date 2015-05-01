package drill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides a lookup capability for finding all the items containing
 * a given stimulus or a given response.
 * 
 * @author David Matuszek
 */
public class ItemLookup {
    final int INTIIAL_SIZE = 2500;
    
    private HashMap<String, ArrayList<Item>> itemsContainingStimulus;
    private HashMap<String, ArrayList<Item>> itemsContainingResponse;

    /**
     * Constructor.
     */
    public ItemLookup() {
        itemsContainingStimulus = new HashMap<String, ArrayList<Item>>(INTIIAL_SIZE);
        itemsContainingResponse = new HashMap<String, ArrayList<Item>>(INTIIAL_SIZE);
    }
    
    /**
     * Adds an item to this ItemLookup object.
     * @param item The item to be added.
     */
    public void addItem(Item item) {
        Set<String> stimuli = Item.getParts(item.getStimulus());
        for (String stimulus : stimuli) {
            addToHashMap(stimulus, item, itemsContainingStimulus);
        }
        
        Set<String> responses = Item.getParts(item.getResponse());
        for (String response : responses) {
            addToHashMap(response, item, itemsContainingResponse);
        }
    }
    
    /**
     * Puts an Item in the HashMap with the given key.
     */
    private void addToHashMap(String key, Item item,
                              HashMap<String, ArrayList<Item>> map) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<Item>(1));
        }
        map.get(key).add(item);
    }
    
    public List<Item> getItemsWithStimulus(String stimulus) {
        if (itemsContainingStimulus.containsKey(stimulus)) {
            return itemsContainingStimulus.get(stimulus);
        }
        return new ArrayList<Item>(0);
    }
    
    public List<Item> getItemsWithResponse(String response) {
        if (itemsContainingResponse.containsKey(response)) {
            return itemsContainingResponse.get(response);
        }
        return new ArrayList<Item>(0);
    }
    
    public Set<String> extractResponsesFrom(List<Item> items) {
        Set<String> responses = new HashSet<String>(2);
        for (Item item : items) {
            responses.add(item.getResponse());
        }
        return responses;
    }
    
    public Set<String> extractStimuliFrom(List<Item> items) {
        Set<String> stimuli = new HashSet<String>(2);
        for (Item item : items) {
            stimuli.add(item.getStimulus());
        }
        return stimuli;
    }
    
    public boolean isValidPair(String stimulus, String response) {
        return extractResponsesFrom(getItemsWithStimulus(stimulus)).contains(response);
    }

}
