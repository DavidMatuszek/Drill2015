package drill;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class maintains a list of Items to be learned, and when and in what
 * order Items should be presented for future study. An overall difficulty
 * level is maintained.
 * 
 * @author David Matuszek
 * @version Jan 17, 2017
 */
public class ItemList extends ArrayList<Item> {
    private PriorityQueue<Item> queue; // Non-virgin items, in priority order    
    private Preferences userPrefs;
    private File itemFile;
    private static int minInterval = 5;
    
    /** 
     * Difficulty level set by user.
     * 2.0 is very easy, 3.0 is moderate, 4.0 is very difficult
     */
    static double difficulty = 3.0;
    
    /** 
     * When a virgin item is gotten correct, pretend it has been answered
     * correctly several times. 
     */
    static final int virginPromotion = 6;

    /**
     * When true, only items considered "learned" should be presented.
     */
    boolean reviewMode = false;
    
    /**
     * When true, only unlearned items should be presented.
     */
    boolean preferNewItems = false;
    
    /**
     * In "review only" mode, this list holds the items that have been
     * presented and gotten correct, so should not be presented again.
     */
    HashSet<Item> reviewItemsAnsweredCorrectly = new HashSet<Item>();
    
    /**
     * In "review only" mode, this list holds the items that have been
     * presented and gotten incorrect, so should be displayed again.
     */
    HashSet<Item> reviewItemsAnsweredIncorrectly = new HashSet<Item>();
    
    /**
     * Constructor for ItemList objects. Creates a new, empty ItemList and
     * a new, empty PriorityQueue of Items to be presented.
     */
    ItemList() {
        super(500);
        itemFile = null;
        Time.now = 0;
        queue = new PriorityQueue<Item>(200);
    }
    
    /**
     * @return The current difficulty level set for this ItemList.
     */
    static double getDifficulty() {
        return difficulty;
    }
    
    /**
     * When entering review only mode, clear lists of review items.
     * @param reviewOnly true if only learned items should be presented.
     */
    void setReviewOnly(boolean reviewOnly) {
        reviewMode = reviewOnly;
        if (reviewMode) {
            reviewItemsAnsweredCorrectly = new HashSet<Item>();
            reviewItemsAnsweredIncorrectly = new HashSet<Item>();
        }
    }
    
    /**
     * @param prefer Avoid presenting well-learned items.
     */
    void setPreferNew(boolean prefer) {
        preferNewItems = prefer;
    }
    
    /**
     * Returns the queue of items, for testing purposes only!
     * @return The queue of items.
     */
    PriorityQueue<Item> getQueue() {
        return queue;
    }
    
    /**
     * Asks the user for a new file in which to begin creating a new ItemList,
     * and empties the current ItemList and PriorityQueue.
     * 
     * @throws IOException If the file could not be created.
     */
    void newFile() throws IOException {
        userPrefs = Preferences.userNodeForPackage(Drill.class);
        String parent = userPrefs.get("BuildDirectory", null);
        itemFile = IO.getOutputFile(parent);
        if (itemFile == null) {
            throw new IOException("File was not created.");
        }
        clear();
        queue.clear();
        userPrefs.put("BuildDirectory", itemFile.getParent());
    }

    /**
     * Asks the user to choose a data file, then reads it in.
     * @param c The actual class (e.g. Drill) to load
     * @throws IOException If the file can't be read.
     * @throws IllegalArgumentException If there is an error in the input file.
     */
    void load(Class<?> c) throws IOException, IllegalArgumentException {
        userPrefs = Preferences.userNodeForPackage(Drill.class);
        String key = c.getSimpleName() + "Directory";
        String parent = userPrefs.get(key, null);
        itemFile = IO.getInputFile(parent);
        if (itemFile == null) {
            throw new IOException("No file loaded.");
        }
        load(itemFile);
        userPrefs.put(key, itemFile.getParent());
    }

    /**
     * Reads in the data file.
     * @param file The file to be read.
     * @throws IOException If the file can't be read.
     * @throws IllegalArgumentException If there is an error in the input file.
     */
    void load(File file) throws IOException, IllegalArgumentException {
        load(new BufferedReader(new FileReader(file)));
        itemFile = file;
    }

    /**
     * Reads in the data file. Each line should be of the form:<br><br>
     * <b>stimulus || response</b><br>
     * or<br>
     * <b>stimulus || response || timesCorrect || interval || displayDate</b>
     * or<br>
     * <b>stimulus || response || timesCorrect || timesIncorrect || interval || displayDate</b>
     * or<br>
     * <b>stimulus || response || timesCorrect || timesIncorrect || consecutiveTimesCorrect || interval || displayDate</b>
     * Blank lines and comment lines (beginning with //) are allowed and ignored.
     * 3/26/2014 File may begin with one or more "//category" lines of statistics
     * 
     * @param in A reader for the file.
     * @throws IOException If the file can't be read.
     * @throws IllegalArgumentException If there is an error in the input file.
     */
    void load(BufferedReader in) throws IOException {
        String line;
        Time.now = 0;
        this.clear();
        queue.clear();
        
        Item item = null;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;
            if (line.startsWith("//")) {
                handlePossibleParameterLine(line);
                continue;
            }
            item = readItem(line);
            // Put all Items into the ArrayList
            add(item);
            // Put non-virgin items into the PriorityQueue
            if (!item.isVirgin()) queue.offer(item);
//            // Initialize Time.now to lowest displayDate of all non-virgin items
//            if (item.getDisplayDate() < Time.now) {
//                Time.now = item.getDisplayDate();
//            }
        }
        // If all items are virgin, we still need something in the queue
        if (queue.isEmpty()) queue.add(item);
//        if (Time.now == Integer.MAX_VALUE) { // all items are virgin
//            Time.now = 0;
//        }
//        else {
//            Time.now--; // we will advance time before showing first item
//        }
        in.close();
    }

    /**
     * Given a line beginning with //, handle if it is a parameter setting,
     * or ignore it if it is just a comment. Currently the only parameter
     * used is 'difficulty'.
     * @param line A line beginning with "//".
     */
    private void handlePossibleParameterLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts[1].equals("difficulty")) {
            setDifficulty(new Double(parts[2]).doubleValue());
        }
    }

    /**
     * Returns the name of the file from which this ItemList
     * was constructed.
     *
     * @return The name of the file being used.
     */
    String getFileName() {
        return itemFile.getName();
    }

    /**
     * Parses a string into an Item. Expected syntax is:<br><br>
     * <b>stimulus || response</b><br>
     * or<br>
     * <b>stimulus || response || timesCorrect || timesIncorrect || interval || displayDate</b>
     * @param line The line to be parsed.
     * @return The parsed item.
     * @throws IllegalArgumentException If there is an error in the input file.
     */
    private static Item readItem(String line) throws IllegalArgumentException {
        String[] parts = line.split("\\s+\\|\\|\\s+");
        if (parts.length == 2) {
            return new Item(parts[0], parts[1]);
        }
        else if (parts.length == 6) {
            int timesCorrect = Integer.parseInt(parts[2]);
            int timesIncorrect = Integer.parseInt(parts[3]);
            int level = timesCorrect - timesIncorrect;
            if (level < 0) level = 0;
            int interval = Integer.parseInt(parts[4]);
            int date = Integer.parseInt(parts[5]);
            return new Item(parts[0], 
                            parts[1],
                            timesCorrect,
                            timesIncorrect,
                            level,
                            interval,
                            date);
        }
        else if (parts.length == 7) {
            int timesCorrect = Integer.parseInt(parts[2]);
            int timesIncorrect = Integer.parseInt(parts[3]);
            int consecutiveTimesCorrect = Integer.parseInt(parts[4]);
            int interval = Integer.parseInt(parts[5]);
            int date = Integer.parseInt(parts[6]);
            return new Item(parts[0], 
                            parts[1],
                            timesCorrect,
                            timesIncorrect,
                            consecutiveTimesCorrect,
                            interval,
                            date);
        }
        else {
            throw new IllegalArgumentException("Should be 2, 6, or 7 parts, not " +
                                               parts.length + ":\n" + line);
        }
    }

    /**
     * Saves this updated ItemList back onto the same file that it was
     * originally read from (or onto a new file, if this is a newly
     * created ItemList).
     * 
     * @throws IOException If the ItemList cannot be saved on the file.
     */
    void save() throws IOException {
        if (itemFile == null) {
            saveAs();
        }
        else {
            saveOnFile(itemFile);
        }
    }

    /**
     * Saves this updated ItemList onto a new file chosen by the user.
     * @throws IOException If the ItemList cannot be saved on the chosen file.
     * 
     */
    void saveAs() throws IOException {
        File newFile = IO.getOutputFile();
        saveOnFile(newFile);
        itemFile = newFile;
    }

    /**
     * Saves this updated ItemList.
     * @param file The file on which to save this ItemList.
     * @throws IOException If the ItemList cannot be saved on the given File.
     * 
     */
    void saveOnFile(File file) throws IOException {
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.println("// difficulty " + difficulty);
            resetItemDisplayTimes();
            Iterator<Item> iter = iterator();
            while (iter.hasNext()) {
                Item item = iter.next();
                if (item.getStimulus().equals("") || item.getResponse().equals("")) {
                    System.err.println("Illegal item: " + item);
                    continue;
                }
                writer.println(item);
            }
            writer.close();
            itemFile = file;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * If there are NO virgin items in this ItemList, normalize the display dates.
     */
    private void resetItemDisplayTimes() {
        int leastDate = Integer.MAX_VALUE;
        Iterator<Item> iter = iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            int date = item.getDisplayDate();
            if (date == Integer.MAX_VALUE) return;
            if (date < leastDate)
                leastDate = item.getDisplayDate();
        }
        iter = iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            if (! item.isVirgin()) {
                item.setDisplayDate(item.getDisplayDate() - leastDate);
            }
        }
    }

    /**
     * Saves this ItemList onto a new file chosen by the user, stripping it of
     * all data regarding an individual's progress.
     * 
     * @throws IOException If the ItemList cannot be saved on the given File.
     */
    void saveAsVirgin() throws IOException {
        File newFile = IO.getOutputFile();
        try {
            PrintWriter writer = new PrintWriter(newFile);
            Iterator<Item> iter = iterator();
            while (iter.hasNext()) {
                Item item = iter.next();
                writer.println(item.toVirginString());
            }
            writer.close();
            virginizeList();
            itemFile = newFile;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make every item on this ItemList into a virgin.
     */
    void virginizeList() {
        Iterator<Item> iter = iterator();
        while (iter.hasNext()) {
            iter.next().setVirgin(true);
        }
    }

    /**
     * Gets the next item in the PriorityQueue whose appointment date has
     * arrived, or (if all nonvirgin display dates are in the future), fill
     * in with a virgin (if available), or just take the next item in the
     * PriorityQueue.
     * 3/22/2015 Added the ability to force a virgin item to be chosen.
     * 2/6/2016 Added the ability to choose only review items.
     * @param forceVirgin If true, choose a virgin item (if possible).
     * @param reviewOnly If true, try to return only review items.
     * @return Some Item to use.
     */
    Item OLD_chooseNextItemToDisplay(boolean forceVirgin) {
        // TODO Clean up this mess!
        Time.now++;
        Item candidate = queue.peek();
        int date = candidate.getDisplayDate();
        if (!queue.isEmpty() && !forceVirgin && !reviewMode && ! preferNewItems) {
            // Return the element at the head of the queue, if its time has come
            if (date <= Time.now) {
                Item nextItem = queue.poll();
                return nextItem;
            }
            Item nextItem = getVirgin();
            if (nextItem != null) return nextItem;
        }
        if (reviewMode) {
            while (!queue.isEmpty()) {
                if (date <= Time.now) {
                    Item nextItem = queue.poll();
                    if (nextItem.isReviewItem() &&
                        ! reviewItemsAnsweredCorrectly.contains(nextItem)) {
                        return nextItem;
                    }
                }
            }
            // Out of review items, but we have to return something!
            reviewItemsAnsweredCorrectly = new HashSet<Item>();
            reviewItemsAnsweredIncorrectly = new HashSet<Item>();
            return new Item("No more review items.", "Do again");
        }
        if (preferNewItems) {
            Item nextItem = queue.poll();
            if (nextItem.isVirgin() ||
                nextItem.getLevel() < Item.learnedThreshhold) {
                return candidate;
            }
        }
        // If no queue element is ready, return a virgin element
        Item nextItem = getVirgin();
        if (nextItem != null) return nextItem;
        // If no virgins left, return head of queue anyway
        return queue.poll();
    }
    
    Item previousItem = null;
    
    /**
     * Decides which item to display next.
     * @param forceVirgin If true, choose a virgin item.
     * @return The item to display next.
     */
    Item chooseNextItemToDisplay(boolean forceVirgin) {
        Item nextItem = chooseNextItemToDisplayHelper(forceVirgin);
        if (nextItem.equals(previousItem) && !queue.isEmpty()) {
            Item anotherItem = chooseNextItemToDisplayHelper(forceVirgin);
            if (anotherItem == null) return nextItem;
            nextItem.setDisplayDate(minInterval + nextItem.getDisplayDate());
            queue.add(nextItem);
            previousItem = anotherItem;
            return anotherItem;
        }
        previousItem = nextItem;
        return nextItem;
    }
    
    Item chooseNextItemToDisplayHelper(boolean forceVirgin) {
        Item nextItem;
        if (forceVirgin) {
            nextItem = getVirgin();
            if (nextItem != null) return nextItem;
        }
        if (reviewMode) {
            nextItem = getReviewItem();
            if (nextItem != null) return nextItem;
        }
        if (preferNewItems) {
            nextItem = getNewItem(100);
            if (nextItem != null) return nextItem;  
        }
        // Normal case
        if (!queue.isEmpty()) {
            int date = queue.peek().getDisplayDate();
            if (date <= Time.now) return queue.poll();
        } else {
            nextItem = getVirgin();
            if (nextItem != null) return nextItem;
        }
        if (!queue.isEmpty()) return queue.poll();
        throw new RuntimeException("No items of any kind!");
    }

    /**
     * @return
     */
    private Item getReviewItem() {
        if (queue.isEmpty()) return null;
        for (int i = 0; i < 100; i++) { // avoid possible infinite loop
            Item nextItem = queue.poll();
            if (nextItem.isReviewItem() &&
                    ! reviewItemsAnsweredCorrectly.contains(nextItem)) {
                return nextItem;
            }
            nextItem.setDisplayDate(Time.now + nextItem.getInterval());
            queue.add(nextItem);
        }
        reviewItemsAnsweredCorrectly = new HashSet<Item>();
        reviewItemsAnsweredIncorrectly = new HashSet<Item>();
        return new Item("No more review items.", "Do again"); 
    }

    /**
     * @return
     */
    private Item getNewItem(int remainingAttempts) {
        if (remainingAttempts <= 0) return null;
        Item nextItem = queue.peek();
        int date = nextItem.getDisplayDate();
        if (date > Time.now) {
            nextItem = getVirgin();
            if (nextItem != null) return nextItem; 
        }
        nextItem = queue.poll();
        if (! nextItem.isReviewItem()) {
            return nextItem;
        }
        nextItem.setDisplayDate(Time.now + nextItem.getInterval());
        queue.add(nextItem);
        return getNewItem(remainingAttempts - 1);
    }

    /**
     * Starting just after the position <code>from</code>, searches this ItemList
     * forward (end-around) for a stimulus or response that matches the given
     * regular expression <code>regex</code>.
     * 
     * @param startFrom The given position, after which the search is to begin.
     * @param regex The regular expression to be matched.
     * @return The location of the found item in this ItemList, or -1 if none found.
     */
    int searchForward(int startFrom, String regex) {
        int from = startFrom;
        Pattern p = Pattern.compile(regex);
        for (int i = 0; i < size(); i++) {
            from = adjust(from + 1);
            
            Item item = get(from);
            Matcher stimulusMatcher = p.matcher(item.getStimulus());
            Matcher responseMatcher = p.matcher(item.getResponse());
            
            if (stimulusMatcher.find() || responseMatcher.find()) {
                return from;
            }
        }
        return -1;
    }

    /**
     * Starting just before the position <code>from</code>, searches this ItemList
     * backward (end-around) for a stimulus or response that matches the given
     * regular expression <code>regex</code>.
     * 
     * @param startFrom The given position, before which the search is to begin.
     * @param regex The regular expression to be matched.
     * @return The location of the found item in this ItemList, or -1 if none found.
     */
    int searchBackward(int startFrom, String regex) {
        int from = startFrom;
        Pattern p = Pattern.compile(regex);
        for (int i = 0; i < size(); i++) {
            from = adjust(from - 1);
            
            Item item = get(from);
            Matcher stimulusMatcher = p.matcher(item.getStimulus());
            Matcher responseMatcher = p.matcher(item.getResponse());
            
            if (stimulusMatcher.find() || responseMatcher.find()) {
                return from;
            }
        }
        return -1;
    }

    /**
     * Given a stimulus to be added to this ItemList, search
     * for another Item with the same stimulus.
     *
     * @param stimulus The stimulus of a proposed new Item.
     * @return The location of a different item with the same stimulus,
     * or -1 if there is no such item.
     */
    int searchForStimulus(String stimulus) {
        for (int i = 0; i < size(); i++) {
            if (stimulus.equals(get(i).getStimulus())) return i;
        }
        return -1; // not found
    }

    /**
     * Search this ItemList for an item with the given response.
     * 
     * @param response
     *        A possible response.
     * @return The stimulus of an item with the given response, or null
     *         if there is no such item.
     */
    String searchForResponse(String response) {
        for (int i = 0; i < size(); i++) {
            Set<String> responses = Item.getParts(get(i).getResponse());
            if (responses.contains(response)) return get(i).getStimulus();
//            if (response.equals(get(i).getResponse())) return get(i).getStimulus();
        }
        return null; // not found
    }

    /**
     * Move the Item currently at index location itemNumber in this ItemList
     * to the index location newPosition.
     * 
     * @param itemNumber The position from which an Item is to be moved.
     * @param newPosition The position to which an Item is to be moved.
     */
    public void moveItem(int itemNumber, int newPosition) {
        if (itemNumber == newPosition) return;
        Item item = remove(itemNumber);
        add(newPosition, item);
    }

    /**
     * Gets the first virgin in the ArrayList <code>items</code>.
     * @return The first virgin, or <code>null</code> if none are left.
     */
    private Item getVirgin() {
        for (int i = 0; i < size(); i++) {
            if (get(i).isVirgin()) {
                return get(i);
            }
        }
        return null;
    }

    /**
     * Puts the item back into the priority queue.
     * <br />Incorrect adjustments to the displayDate removed 2/13/2016.
     * <br />Fix to the effectiveness of minInterval,  1/17/2017
     * @param item The item to be re-inserted into the priority queue.
     */
    void put(Item item) {
        // Try to ensure that minInterval is observed
        Item[] temp = queue.toArray(new Item[0]);
        if (queue.size() <= minInterval) {
            int lastTime = temp[temp.length - 1].getDisplayDate();
            item.setDisplayDate(lastTime + 1);
        } else if (temp[minInterval].getDisplayDate() > item.getDisplayDate() ) {
            item.setDisplayDate(temp[minInterval].getDisplayDate() + 1);
        }
        
        queue.offer(item);
    }
    
    /**
     * Treats this ItemList as circular, by adjusting an index that
     * is slightly out of bounds to one that is within bounds.
     * @param index The proposed index.
     * @return The adjusted index.
     */
    int adjust(int index) {
        return (index + size()) % size();
    }
    
    /**
     * Returns the number of different items that have been presented to this
     * user at least once.
     * 
     * @return A count of the number of different items seen.
     */
    int getNumberOfItemsSeen() {
        int count = 0;
        for (Item item : this) {
            if (!item.isVirgin()) count++;
        }
        return count;
    }
    
    /**
     * Returns the number of items that were answered correctly the last
     * time they were shown to this user.
     * 
     * @return A count of items most recently answered correctly.
     */
    int getNumberOfItemsCorrect() {
        int count = 0;
        for (Item item : this) {
            if (item.isVirgin()) continue;
            if (item.getTimesCorrect() > 0) count++;
        }
        return count;
    }
    
//    /**
//     * Returns the number of different items that have been learned by this
//     * user. This is, of course, an estimate, as the exact number is
//     * impossible to determine. The actual figure returned is the accumulated
//     * probability of a correct answer times the number of items seen.
//     * 
//     * @return A count of the number of different items learned.
//     */
//    int getNumberOfItemsLearned() {
//        int numberOfRightAnswers = 0;
//        int numberOfWrongAnswers = 0;
//        for (Item item : this) {
//            numberOfRightAnswers += item.getTimesCorrect();
//            numberOfWrongAnswers += item.getTimesIncorrect();
//        }
//        double numberOfAnswers = numberOfRightAnswers + numberOfWrongAnswers;
//        double probability = numberOfRightAnswers / numberOfAnswers;
//        return (int)(probability * getNumberOfItemsSeen());
//    }
    
    /**
     * Returns the number of different items that have been learned by this
     * user. This is, of course, an estimate, as the exact number is
     * impossible to determine. The actual figure returned is the sum of
     * a measure for each item.
     * 
     * @return An estimate of the number of different items learned.
     */
    double getNumberOfItemsLearned() {
        double itemsLearned = 0.0;
        for (Item item : this) {
            int level = item.getConsecutiveTimesCorrect();
            switch(level) {
                case 0: break;
                case 1: itemsLearned += 0.1; break;
                case 2: itemsLearned += 0.3; break;
                case 3: itemsLearned += 0.5; break;
                case 4: itemsLearned += 0.8; break;
                case 5: itemsLearned += 1.0; break;
                default: itemsLearned += (level < 0 ? 0.0 : 1.0); break;
            }
        }
        return itemsLearned;
    }
    
    /**
     * Change the difficulty level. This involves updating every item in
     * the queue with a new interval and new display date.
     * <br />Modified to also adjust minInterval 1/17/2017
     * @param newDifficulty 2.0 is very easy, 4.0 is very hard.
     */
    void setDifficulty(double newDifficulty) {
        // If no change, do nothing
        if (Math.abs(newDifficulty - difficulty) < 0.01) return;
        // Reschedule all items into a new priority queue
        PriorityQueue<Item> newQueue = new PriorityQueue<Item>();
        for (Item item : queue) {
            if (!item.isVirgin()) {
                int oldInterval = item.getInterval();
                int oldDisplayDate = item.getDisplayDate();
                int itemLevel = item.getLevel();
                int newInterval = getInterval(newDifficulty, itemLevel);
                int newDisplayDate = oldDisplayDate - oldInterval + newInterval; // ok if negative
                item.setInterval(newInterval);
                item.setDisplayDate(newDisplayDate);
            }
            newQueue.add(item);
        }
        difficulty = newDifficulty;
        minInterval = (int) (difficulty * difficulty);
        queue = newQueue;
    }

    /**
     * Determines the display level for an item.
     * @param itemLevel The number of (correct - incorrect) for the item.
     * @param newDifficulty The provided difficulty level.
     * @return The desired interval, as an exponential function of the difficulty.
     */
    static int intervalForLevel(int itemLevel, double newDifficulty) {
        return getInterval(newDifficulty, itemLevel);
    }

    /**
     * Determine the interval for a correct item.
     * @param newDifficulty The overall difficulty of the ItemList.
     * @param level The level of this item.
     * @return An interval for this item.
     */
    static int getInterval(double newDifficulty, int level) {
        int interval;
        int maxLevel = 5;
        int maxStep = (int)(Math.pow(newDifficulty, maxLevel));
        if (level <= maxLevel) {
            interval = (int)(Math.pow(newDifficulty, level));
        } else {
            int excess = level - maxLevel;
            interval = (int)(Math.pow(newDifficulty, maxLevel) + (excess * maxStep));
        }
        if (interval < minInterval) interval = minInterval;
        return interval;
    }

    /**
     * For debugging purposes only.
     */
    void print() {
        System.out.println("ArrayList:");
            for (int i = 0; i < size(); i++) {
            System.out.println("    " + get(i));
        }
    }

    /**
     * For debugging purposes only.
     */
    void printQueue() {
        System.out.println("PriorityQueue at time " + Time.now + ":");
        Iterator<Item> iter = queue.iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            System.out.println("    " + item);
        }
        System.out.println();
    }
    
    /**
     * Returns the first part of this ItemList in a printable form.
     * Added 1/17/2017.
     * @return Some information about this ItemList.
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PriorityQueue at time " + Time.now + ":\n");
        sb.append("      key: stimulus || response || timesCorrect || timesIncorrect\n" +
                  "             || consecutiveTimesCorrect || interval || displayDate\n");
        Iterator<Item> iter = queue.iterator();
        int n = 0;
        int max = 10;
        Item item = null;
        while (iter.hasNext()) {
            n += 1;
            item = iter.next();
            if (n <= max) sb.append("  " + n + "  " + item + "\n");
        }
        if (n > max) {
            sb.append("...\n  " + n + "  " + item);
        }
        return sb.toString();
    }

    /**
     * @return
     */
    public String getPreviewItems() {
        ArrayList<String> al = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        Iterator<Item> iter = queue.iterator();
        
        // Iterator for PriorityQueue does not return items in priority order
        Item[] items = (queue.toArray(new Item[0]));
        java.util.Arrays.sort(items);
        
        int limit = Math.min(20, items.length);
        for (int i = 0; i < limit; i += 1) {
            Item item = items[i];
            String stim = item.getStimulus();
            String resp = item.getResponse();
            if (stim.length() < 25) stim = (stim + "                              ").substring(0, 25);
            al.add(stim + "  " + resp + "\n");           
        }
        al.sort(null);
        for (String s : al) sb.append(s );
        return sb.toString();
    }

    /**
     * In "Review Only" mode, update this item. If initially correct, it
     * is added to the reviewItemsAnsweredCorrectly set, so that it will
     * not be shown again. Otherwise it is added to the
     * reviewItemsAnsweredIncorrectly set, where it can be used again,
     * up to the number of times defined by learnedThreshhold.
     * @param currentItem The Item being reviewed.
     * @param correct Whether the Item was answered correctly.
     */
    public void updateReviewItem(Item currentItem, boolean correct) {
        if (correct) {
            if (reviewItemsAnsweredIncorrectly.contains(currentItem)) {
                if (currentItem.getLevel() >= Item.learnedThreshhold) {
                    reviewItemsAnsweredIncorrectly.remove(currentItem);
                    reviewItemsAnsweredCorrectly.add(currentItem);
                }
            } else {
                reviewItemsAnsweredCorrectly.add(currentItem);
            }
        } else {
            reviewItemsAnsweredIncorrectly.add(currentItem);
        }
    }
}
