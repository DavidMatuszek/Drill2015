package drill;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Represents a stimulus-response pair, but also contains student
 * information regarding how well the Item has been learned.
 * 
 * @version March 2, 2015
 */
public class Item implements Comparable<Item> {
    
    /** Difference in display dates should be at least this much */
    static int minInterval = 2;
    
    /** What is presented to the user */
    private String stimulus;
    
    /** What the user should respond */
    private String response;
    
    /** Number of correct responses */
    private int timesCorrect;
    
    /** Number of wrong responses */
    private int timesIncorrect;
    
    /** Length of current sequence of correct responses */
    private int consecutiveTimesCorrect; 
    
    /** The ideal amount of time between presentations */
    private int interval;
    
    /** The next date at which to present this Item */
    private int displayDate;
    
//  TODO Implement review mode; see also code in ItemList  
  /**
   * An item is considered "learned" and available for review if it has
   * been answered correctly this many times consecutively.
   */
  static int learnedThreshhold = 5;
  
  /**
   * An item is considered "overlearned" and not worth reviewing it it
   * has been answered this many times consecutively.
   */
  static int overlearnedThreshhold = 10;
    
//    TODO Implement review mode and move this method further down on page
    /**
     * @return true if this Item is a learned item that should be reviewed.
     */
    boolean isReviewItem() {
        return consecutiveTimesCorrect >= learnedThreshhold &&
                consecutiveTimesCorrect < overlearnedThreshhold;
    }

// -------------------- Constructors --------------------

    /**
     * Construct Item as "virgin".
     * @param stimulus The part of an Item usually used as the stimulus.
     * @param response The part of an Item usually used as the response.
     */
    public Item(String stimulus, String response) {
        // Maximum display date indicates this is virgin
        this(stimulus, response, 0, 0, 0, minInterval, Integer.MAX_VALUE);
    }
    
    /**
     * Construct an Item with the given fields.
     * Used for older files with no "consecutiveTimesCorrect" field.
     * 
     * @param stimulus The stimulus.
     * @param response The response.
     * @param timesCorrect Number of correct answers.
     * @param timesIncorrect Number of incorrect answers.
     * @param interval How long it should be between presentations of this Item.
     * @param displayDate The next date this Item should be displayed.
     * @throws IllegalArgumentException if any parameter is invalid.
     */
    public Item(String stimulus, String response,
                int timesCorrect, int timesIncorrect,
                int interval, int displayDate) {
        this(stimulus, response,  timesCorrect, timesIncorrect,
             Math.max(0,  timesCorrect - 2 * timesIncorrect), // Guess an appropriate value for "consecutiveTimesCorrect" 
             interval, displayDate);
    }
    
    /**
     * Construct an Item with the given fields.
     * 
     * @param stimulus The stimulus.
     * @param response The response.
     * @param timesCorrect Number of correct answers.
     * @param timesIncorrect Number of incorrect answers.
     * @param consecutiveTimesCorrect Length of current sequence of correct responses
     * @param interval How long it should be between presentations of this Item.
     * @param displayDate The next date this Item should be displayed.
     * @throws IllegalArgumentException if any parameter is invalid.
     */
    public Item(String stimulus, String response,
                int timesCorrect, int timesIncorrect, int consecutiveTimesCorrect,
                int interval, int displayDate) {
        testValidity(stimulus, response,
                     timesCorrect, timesIncorrect, consecutiveTimesCorrect, 
                     interval, displayDate);
        this.setStimulus(stimulus);
        this.setResponse(response);
        this.timesCorrect = timesCorrect;
        this.timesIncorrect = timesIncorrect;
        this.consecutiveTimesCorrect = consecutiveTimesCorrect;
        this.interval = reasonable(interval);
        this.displayDate = displayDate;
        if (displayDate != Integer.MAX_VALUE) this.displayDate = reasonable(displayDate);
    }
    
    /**
     * For some reason, older versions of Drill may set the item interval and/or the
     * item display date to unreasonably large numbers, causing integer overflow.
     * This method reduces numbers over a million to something more reasonable.
     * @param n The number to be adjusted.
     * @return The number given, or reduced if unreasonably large.
     */
    static int reasonable(int n) {
        while (n > 1000000) n = n / 2;
        return n;
    }
    
// -------------------- Getters and setters --------------------


    /**
     * Returns the stimulus part of this Item.
     * @return The stimulus.
     */
    String getStimulus() {
        return stimulus;
    }

    /**
     * Changes the stimulus part of this Item.
     * @param stimulus The new stimulus.
     */
    void setStimulus(String stimulus) {
        if (stimulus.trim().length() == 0) {
            error("Empty stimulus");
        }
        this.stimulus = stimulus;
    }

    /**
     * Returns the response part of this Item.
     * @return The response.
     */
    String getResponse() {
        return response;
    }

    /**
     * Changes the response part of this Item.
     * @param response The new response.
     */
    void setResponse(String response) {
        if (response.trim().length() == 0) {
            error("Empty response");
        }
        this.response = response;
    }

    /**
     * @return the times correct
     */
    int getTimesCorrect() {
        return timesCorrect;
    }

    /**
     * Changes the number of times that this Item has been answered
     * correctly.
     * @param times The new number of correct responses.
     */
    public void setTimesCorrect(int times) {
        if (times < 0) {
            error("Negative times correct: " + times);
        }
        timesCorrect = times;
    }

    /**
     * Changes the number of times that this Item has been answered
     * correctly.
     * @param times The new number of correct responses.
     */
    public void setConsecutiveTimesCorrect(int times) {
        if (times < 0) {
            error("Negative consecutive times correct: " + times);
        }
        consecutiveTimesCorrect = times;
    }

    /**
     * @return the times incorrect
     */
    int getTimesIncorrect() {
        return timesIncorrect;
    }

    /**
     * Changes the number of times that this Item has been answered
     * incorrectly.
     * @param times The new number of correct responses.
     */
    public void setTimesIncorrect(int times) {
        if (times < 0) {
            error("Negative times incorrect: " + times);
        }
        timesIncorrect = times;
    }
    
    /**
     * If this item has never been responded to, or if the most recent response
     * was incorrect, then return zero; but if the most recent response was
     * correct, return a count of the number of consecutive correct responses
     * including the most recent response.
     * @return The consecutive times correct.
     */
    public int getConsecutiveTimesCorrect() {
        return consecutiveTimesCorrect;
    }
    
    /**
     * Returns a measure of how well this item is learned, currently defined
     * as simply times correct - times incorrect.
     * 1-23-2016 Changed the level to equal consecutiveTimesCorrect.
     * @return The "learned level" of this Item.
     */
    public int getLevel() {
        return getConsecutiveTimesCorrect();
    }
    /**
     * @return the interval
     */
    int getInterval() {
        return interval;
    }

    /**
     * Changes the interval between presentations of this Item.
     * @param interval The new interval.
     */
    public void setInterval(int interval) {
        if (interval < minInterval) {
            error("Interval less than " + minInterval + ": " + interval);
        }
        this.interval = interval;
    }

    /**
     * @return the displayDate
     */
    int getDisplayDate() {
        return displayDate;
    }

    /**
     * Changes the requested display date for this Item.
     * @param displayDate The new display date.
     */
    public void setDisplayDate(int displayDate) {
        if (displayDate < 0) throw new IllegalArgumentException("displayDate = " + displayDate);
        this.displayDate = displayDate;
    }


    /**
     * Tells whether this item is virgin.
     * @return <code>true</code> if virgin.
     */
    boolean isVirgin() {
        return displayDate == Integer.MAX_VALUE;
    }

    /**
     * Resets this item to its initial (unseen) state.
     * @param virgin If true, resets item.
     */
    void setVirgin(boolean virgin) {
        if (virgin) {
            this.timesCorrect = 0;
            this.timesIncorrect = 0;
            this.interval = minInterval;
            displayDate = Integer.MAX_VALUE;
        }
        else displayDate = Time.now;
    }
    
// -------------------- Other methods --------------------
    
    /**
     * Changes some or all of the fields of this Item.
     * 
     * @param newStimulus The stimulus.
     * @param newResponse The response.
     * @param newTimesCorrect Number of correct responses.
     * @param newTimesIncorrect Number of incorrect responses.
     * @param newConsecutiveTimesCorrect Length of current sequence of correct responses.
     * @param newInterval How long it should be between presentations of this Item.
     * @param newDisplayDate The next date this Item should be displayed.
     * @throws IllegalArgumentException if any parameter is invalid.
     * @return <code>true</code> if this Item was modified.
     */
    public boolean modify(String newStimulus,
                          String newResponse,
                          int newTimesCorrect,
                          int newTimesIncorrect,
                          int newConsecutiveTimesCorrect,
                          int newInterval,
                          int newDisplayDate) {
        testValidity(newStimulus, newResponse, newTimesCorrect,
                     newConsecutiveTimesCorrect, 
                     newTimesIncorrect, newInterval, newDisplayDate);
        if (newStimulus.equals(this.stimulus.trim())
                && newResponse.equals(this.response.trim())
                && newTimesCorrect == this.timesCorrect
                && newTimesIncorrect == this.timesIncorrect
                && newConsecutiveTimesCorrect == this.consecutiveTimesCorrect
                && newInterval == this.interval
                && newDisplayDate == this.displayDate) {
            return false;
        }
        this.setStimulus(newStimulus);
        this.setResponse(newResponse);
        this.timesCorrect = newTimesCorrect;
        this.timesIncorrect = newTimesIncorrect;
        this.consecutiveTimesCorrect = newConsecutiveTimesCorrect;
        this.interval = newInterval;
        this.displayDate = newDisplayDate;
        return true;
    }
    
//    /**
//     * Old version, without consecutiveTimesCorrect.
//     */
//    public boolean modify(String newStimulus,
//                          String newResponse,
//                          int newTimesCorrect,
//                          int newTimesIncorrect,
//                          int newInterval,
//                          int newDisplayDate) {
//        return modify(newStimulus, newResponse,
//                      newTimesCorrect, newTimesIncorrect,
//                      consecutiveTimesCorrect, // Used by editor; leave unchanged
//               newInterval, newDisplayDate);
//    }
    
    /**
     * Throws an IllegalArgumentException if any parameter is invalid.
     * 
     * @param newStimulus The stimulus.
     * @param newResponse The response.
     * @param newTimesCorrect Number of correct responses. 
     * @param newTimesIncorrect Number of incorrect responses.
     * @param newInterval How long it should be between presentations of this Item.
     * @param newDisplayDate The next date this Item should be displayed.
     * @throws IllegalArgumentException if any parameter is invalid.
     */
    static void testValidity(String newStimulus, String newResponse,
                      int newTimesCorrect, int newTimesIncorrect,
                      int newConsecutiveTimesCorrect,
                      int newInterval, int newDisplayDate) {
        errorIf(newStimulus.trim().length() == 0,
                "Stimulus field may not be blank.", newStimulus);
        errorIf(newResponse.trim().length() == 0,
                "Response field may not be blank.", newStimulus);
        errorIf(newTimesCorrect < 0,
                "\"Times correct\" may not be negative.", newStimulus);
        errorIf(newTimesIncorrect < 0,
                "\"Times incorrect\" may not be negative.", newStimulus);
        errorIf(newConsecutiveTimesCorrect < 0,
                "\"Consecutive times correct\" may not be negative.", newStimulus);
        errorIf(newInterval < minInterval,
                "\"Interval\" must be at least " + minInterval + ".", newStimulus);
        errorIf(newDisplayDate < 0,
                "\"Display date\" may not be negative.", newStimulus);
    }
    
    /**
     * Throws an IllegalArgumentException with the given message if the given
     * condition is true.
     * 
     * @param condition A condition that should not hold.
     * @param message The message to provide if the condition does hold.
     * @param info Information to include in the message.
     */
    private static void errorIf(boolean condition, String message, String info) {
        if (condition) error(message + ": " + info);
    }

    /**
     * A convenience method for throwing an IllegalArgumentException.
     * @param message The message to be included in the Exception.
     * @throws IllegalArgumentException with the given message.
     */
    private static void error(String message) {
        throw new IllegalArgumentException(message);
    }
    /**
     * Marks this Item as better learned than previously, and adjusts
     * information that controls when this Item will next be presented.
     */
    public void promote() {
        if (isVirgin() ) {
            timesCorrect = ItemList.virginPromotion;
            consecutiveTimesCorrect = ItemList.virginPromotion;
        } else {
            timesCorrect += 1;
            consecutiveTimesCorrect += 1;
        }
        interval = getNewInterval(true, ItemList.getDifficulty());
        displayDate = Time.now + interval;
    }

    /**
     * Marks this Item as not as well learned as previously thought, and adjusts
     * information that controls when this Item will next be presented.
     */
    public void demote() {
        timesIncorrect += 1;
        consecutiveTimesCorrect = 0;
        interval = getNewInterval(false, ItemList.getDifficulty());
        displayDate = Time.now + interval;
    }
    
    /**
     * Determines the length of time before this item should be presented again..
     * @param correct Whether this item is correct.
     * @return A number of time units.
     */
    int getNewInterval(boolean correct, double difficulty) {
        int level = getLevel();
//        double difficulty = ItemList.getDifficulty();
        
        // The following is a modification to temporarily reduce the level of
        // "learnedness" of an item. This is an attempt to reduce the problem
        // of getting a large number of items incorrect after not having used
        // the program for a few days.
        
        if (!correct) level = Math.max(level - 2, 1);
        
//        Base        1        2        3        4        5        6        7        8        9       10       11       12
//        ------------------------------------------------------------------------------------------------------------------
//          2.00        2        4        8       16       32       64      128      256      512     1024     2048     4096 
//          2.50        2        6       15       39       97      195      390      781     1562     3125     6250    12500 
//          3.00        3        9       27       81      243      486      972     1944     3888     7776    15552    31104 
//          3.50        3       12       42      150      525     1050     2100     4201     8403    16807    33614    67228 
//          4.00        4       16       64      256     1024     2048     4096     8192    16384    32768    65536   131072 
//        where maxlevel =  5

        int interval;
        int maxLevel = 5;
        if (level <= maxLevel) {
            interval = (int)(Math.pow(difficulty, level));
        } else {
            int excess = level - maxLevel;
            interval = (int)(Math.pow(difficulty, maxLevel) * Math.pow(2, excess));
        }
        return Math.min(interval, 1000000);
    }
    
    /**
     * Returns the String representation of this item that is used
     * for the student data file. The form is:<br>
     * <code>stimulus || response || times correct || consecutiveTimesCorrect || interval || date</code>
     * 
     * @return The String representation used in student data files.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String sep = " || ";
        if (displayDate == Integer.MAX_VALUE) {
            return stimulus + sep + response;
        }
        return stimulus + sep + response + sep +
        timesCorrect + sep + timesIncorrect + sep +
        consecutiveTimesCorrect + sep +
        interval + sep + displayDate;
    }

    /**
     * Returns the String representation of this item that is used
     * for new data files with <i>no</i> student information. The form is:<br>
     * <code>stimulus || response/code>
     * 
     * @return The String representation used in student data files.
     * @see java.lang.Object#toString()
     */
    public String toVirginString() {
        return getStimulus() +  " || " + response;
    }

    /**
     * Returns <code>true</code> if the parameter has the same
     * stimulus and response fields as this object.<br>
     * <i>This method is not consistent with compareTo!</i>
     *
     * @param o
     *        The object to be compared to this object.
     * @return <code>true</code> if the stimuli and responses are
     *         equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Item)) return false;
        Item that = (Item) o;
        return this.getStimulus().equals(that.getStimulus())
                && this.getResponse().equals(that.getResponse());
    }
    
    /**
     * Returns the hash code of this Item. This method is included
     * for completeness, but is not currently in use.
     * @return A hash code for this item.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getStimulus().hashCode() + getResponse().hashCode();
    }

    /**
     * Returns a negative number if this Item should be presented
     * before the parameter Item; zero if they are vying for the same
     * time slot; and positive if this Item should be presented after
     * the Item given as parameter.<br>
     * <i>This method is not consistent with equals!</i>
     *
     * @param that The Item that this Item is being compared to.
     * @return Negative, zero, or positive, depending on whether this
     * item should be presented before, at approximately the same time as,
     * or after the Item given as a parameter.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Item that) {
        return this.displayDate - that.displayDate;
    }

    /**
     * Returns <code>true</code> if the given text is an acceptable
     * response to this item. Currently this means "exactly equal,"
     * but in the future we might consider ignoring umlauts, or
     * alternate responses ("big | large"), etc.
     * <p>
     * In addition, the following instance variable is set:<br>
     * <code>unusedResponses</code> -- other correct answers.
     * 
     * 2/25/07 -- Now any of " | ", ", ", and "; " separate alternate
     * responses, and any may be considered correct.
     * @param usersResponse The user's response (<code>response</code> is the
     *     correct response)
     * @return <code>true</code> if text can be considered correct.
     */
    public boolean responseIsCorrect(String usersResponse) {
        boolean isCorrect = false;

        // simple tests
        if (response.equals(usersResponse.trim().replaceAll("  +", " "))) return true;
        if (usersResponse.trim().length() == 0) return false;
        
        Set<String> legalResponses = breakIntoAlternatives(response, " \\| |; ");
        Set<String> legalResponsesWithoutParens =
                breakIntoAlternatives(removeParenthesizedGroups(response), " \\| |, |; ");
        Set<String> usersResponses = breakIntoAlternatives(usersResponse, " \\| |, |; ");
        
        isCorrect = legalResponses.containsAll(usersResponses) ||
                legalResponsesWithoutParens.containsAll(usersResponses);
        if (isCorrect) return true;
        
        // Here we're going to look at each alternative correct response, to see if any
        // are very similar to the user's responses
        for (String found : usersResponses) {
            for (String expected : legalResponses) {
                if (!found.equals(expected) && similarity(found, expected) > 0.85) {
                    return askIfUserMeantThis(found, expected);
                }
            }
        }
        
        // TODO Check if unusedResponses is still ever used for anything
        if (isCorrect && usersResponses.size() < legalResponses.size()) {
            Set<String> unusedResponses = new HashSet<String>(legalResponses);
            unusedResponses.removeAll(usersResponses);
        }
        return isCorrect;
    }
    
    private boolean askIfUserMeantThis(String found, String expected) {
        String message = "You said:\n" + found + "\nDid you mean:\n" + expected + "\n?";
        int yesNo = JOptionPane.showConfirmDialog(Drill.thisGui, message);
        return yesNo == JOptionPane.YES_OPTION;
    }
    
    /**
     * Returns a rough measure of the similarity of two strings, where 1.0 means
     * identical and small or negative numbers mean not very similar. Arguments may
     * not be null.
     * @param s1 The first string.
     * @param s2 The second string.
     * @return Some measure between 0 (not similar) and 1 (identical).
     */
    static double similarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) return 0.0;
        double diff = Math.abs(len1 - len2);
        for (int i = 0; i < len1; i += 1) {
            if (s2.indexOf(s1.charAt(i)) == -1) diff += 1;
        }
        for (int i = 0; i < len2; i += 1) {
            if (s1.indexOf(s2.charAt(i)) == -1) diff += 1;
        }
        return 1.0 - diff / (len1 + len2);
    }

    private static int minimum(int i, int j, int k) {
        return Math.min(i, Math.min(j, k));
    }
    
    /**
     * Given a string consisting of one or more substrings separated by
     * semicolons, return a set of those substrings, with parenthesized
     * groups removed.
     * @param response The input string, possibly with more than one part.
     * @return A set of the parts.
     */
    public static Set<String> getParts(String response) {
        return breakIntoAlternatives(removeParenthesizedGroups(response), " *; *");
    }
    
    /**
     * Uses the regular expression to break the text into parts.
     * @param text The text to break.
     * @param regex The regular expression.
     * @return A set of parts.
     */
    static Set<String> breakIntoAlternatives(String text, String regex) {
        String[] alternatives = text.split(regex);
        for (int i = 0; i < alternatives.length; i++) {
            alternatives[i] = alternatives[i].trim();
        }
        return new HashSet<String>(Arrays.asList(alternatives));
    }
    
    /**
     * Text in parentheses is removed from the string, along with the
     * preceding blank (if any).
     * @param s The input string.
     * @return The string with the parenthesized text removed.
     */
    static String removeParenthesizedGroups(String s) {
        return s.replaceAll(" ?\\([^\\)]*\\)", "");
    }
    
//    /**
//     * Creates a <code>separator</code> separated list of <code>strings</code>.
//     * @param strings The Strings to be concatenated.
//     * @param separator The separator to use.
//     * @return The separated concatenated Strings.
//     */
//    private static String join(String[] strings, String separator) {
//        if (strings.length == 0) return "";
//        String result = strings[0];
//        for (int i = 1; i < strings.length; i++) {
//            result += separator + strings[i];
//        }
//        return result;
//    }
}
