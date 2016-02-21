package drill;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.prefs.Preferences;

/**
 * The main class for the Drill flash card program.
 *
 * @author Dave Matuszek
 */
public class Drill extends JFrame {

//    private transient final SpecialCharacters LANGUAGE = new Japanese();
    private transient final SpecialCharacters LANGUAGE = new German();

    private JLabel percentLabel = new JLabel("");
    private JLabel numberOfTrialsLabel = new JLabel("");
    private JTextField stimulusField = new JTextField(40);
    private JTextField correctResponseField = new JTextField(40);
    private JTextField responseField = new JTextField(40);
    private JLabel previousItemStimulus = new JLabel("");
    private JLabel previousItemResponse = new JLabel("");
    private JMenuBar menuBar = new JMenuBar();

    private JMenu fileMenu = new JMenu("File");
    private JMenuItem loadMenuItem = menuItem("Open...", KeyEvent.VK_O, false);
    private JMenuItem saveMenuItem = menuItem("Save", KeyEvent.VK_S, false);
    private JMenuItem saveAsMenuItem = menuItem("Save As...", KeyEvent.VK_S, true);
    private JMenuItem quitMenuItem = menuItem("Quit", KeyEvent.VK_Q, false);

    private JMenu specialMenu = new JMenu("Special");
    private JMenuItem dontScoreMenuItem = new JMenuItem("Don't score");
    private JMenuItem fixItemMenuItem = new JMenuItem("Fix item...");
    private JMenuItem typoMenuItem = menuItem("Typo", KeyEvent.VK_T, false);
    private JMenuItem fontSizeItem = new JMenuItem("Font size...");

    private JMenu difficultyMenu = new JMenu("Difficulty");
    private JRadioButtonMenuItem veryEasyMenuItem = new JRadioButtonMenuItem("Very Easy", false);
    private JRadioButtonMenuItem easyMenuItem = new JRadioButtonMenuItem("Easy", false);
    private JRadioButtonMenuItem mediumMenuItem = new JRadioButtonMenuItem("Medium", true);
    private JRadioButtonMenuItem difficultMenuItem = new JRadioButtonMenuItem("Difficult", false);
    private JRadioButtonMenuItem veryDifficultMenuItem = new JRadioButtonMenuItem("Very Difficult", false);
    private JCheckBoxMenuItem reviewOnlyMenuItem = new JCheckBoxMenuItem("Review items only");
    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem helpMenuItem = new JMenuItem("Help");
    
    private ButtonGroup group = new ButtonGroup();

    private int mainFontSize = 20;
    private int messageFontSize = 14;
    private Font mainFont = new Font("Serif", Font.PLAIN, mainFontSize);
    private Font messageFont = new Font("SansSerif", Font.PLAIN, messageFontSize); 
    private Preferences userPrefs;

    /** This Drill object; public so it can be used in Listeners. */
    public static Drill thisGui;
    
    /** ItemList being used; public in order to use TestUTF_8 to check umlauts. */
    public ItemList itemList;
    
    private boolean isDirty;
    private transient Item currentItem;
//    private transient String previousStimulus;
//    private transient String previousResponse;
    private boolean firstTry;
    private int numberCorrect = 0;
    private int numberIncorrect = 0;
//    private String correctResponse = "";
    private int itemsSeen = 0;
    private double itemsLearned = 0;
    private long timeOfLastAction = 0;
    private int itemsInARowCorrect = 0; 

    /**
     * Constructor for the Drill program.
     */
    public Drill() {
        buildGui();
        attachListeners();
        pack();
        setLocationRelativeTo(getParent());
        catchQuit();
    }

    /**
     * Creates the main GUI for the Drill program.
     */
    private void buildGui() {
        setTitle("Drill 2015");
        buildMenuBar();
        setLayout(new GridLayout(5, 1));
        setBackground(new Color(224, 224, 224));

        JPanel statisticsPanel = new JPanel();
        statisticsPanel.setLayout(new GridLayout(1, 2));
        statisticsPanel.add(numberOfTrialsLabel);
        numberOfTrialsLabel.setFont(messageFont);
        statisticsPanel.add(percentLabel);
        percentLabel.setFont(messageFont);
        add(statisticsPanel);

        JPanel stimulusPanel = new JPanel();
        stimulusPanel.setLayout(new FlowLayout());
        stimulusField.setFont(mainFont);
        stimulusField.setEditable(false);
        stimulusPanel.add(stimulusField);
        add(stimulusPanel);

        JPanel responsePanel = new JPanel();
        responsePanel.setLayout(new FlowLayout());
        responseField.setFont(mainFont);
        responsePanel.add(responseField);
        add(responsePanel);

        JPanel correctResponsePanel = new JPanel();
        correctResponsePanel.setLayout(new FlowLayout());
        correctResponseField.setFont(mainFont);
        correctResponseField.setEditable(false);
        correctResponsePanel.add(correctResponseField);
        add(correctResponsePanel);

        JPanel messagePanel = new JPanel();
        JPanel spacerPanel = new JPanel();
        spacerPanel.setLayout(new BorderLayout());
        spacerPanel.add(new JLabel(" "), BorderLayout.WEST);
        messagePanel.setLayout(new GridLayout(2, 1));
        messagePanel.add(previousItemStimulus);
        previousItemStimulus.setFont(messageFont);
        messagePanel.add(previousItemResponse);
        previousItemResponse.setFont(messageFont);
        spacerPanel.add(messagePanel, BorderLayout.CENTER);
        add(spacerPanel);

        correctResponseField.setEditable(false);
    }

    /**
     * Builds the menu bar and menus.
     */
    private void buildMenuBar() {
        setJMenuBar(menuBar);
        
        // File
        menuBar.add(fileMenu);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(quitMenuItem);
        
        // Special
        menuBar.add(specialMenu);
        specialMenu.add(dontScoreMenuItem);
        dontScoreMenuItem.setEnabled(false);
        specialMenu.add(fixItemMenuItem);
        specialMenu.add(typoMenuItem);
        typoMenuItem.setEnabled(false);
        specialMenu.add(fontSizeItem);
        
        // Difficulty
        menuBar.add(difficultyMenu);
        difficultyMenu.add(veryEasyMenuItem);
        difficultyMenu.add(easyMenuItem);
        difficultyMenu.add(mediumMenuItem);
        difficultyMenu.add(difficultMenuItem);
        difficultyMenu.add(veryDifficultMenuItem);
        difficultyMenu.addSeparator();
        difficultyMenu.add(reviewOnlyMenuItem);
        group.add(veryEasyMenuItem);
        group.add(easyMenuItem);
        group.add(mediumMenuItem);
        group.add(difficultMenuItem);
        group.add(veryDifficultMenuItem);
        
        // Help
        menuBar.add(helpMenu);
        helpMenu.add(helpMenuItem);
    }

    /**
     * Returns a menu item whose text and accelerator keys do not
     * depend on which operating system is being used. If no
     * accelerator key is desired, use zero for that parameter.
     *
     * @param words The menu text to use.
     * @param key The accelerator (shortcut) key to use.
     * @param shifted true if shift key is down.
     * @return The complete menu item.
     */
    private static JMenuItem menuItem(String words, int key, boolean shifted) {
        JMenuItem menuItem = new JMenuItem(words);
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(); // control
        if (shifted) mask |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        menuItem.setAccelerator(KeyStroke.getKeyStroke(key, mask));
        return menuItem;
    }

    /**
     * Displays a new item, after the user has finished
     * responding to the previous item.
     *
     * @param item The item to be displayed.
     */
    private void displayNewItem(Item item) {
        firstTry = true;
        currentItem = item;
        stimulusField.setText(item.getStimulus());
        correctResponseField.setText("");
        correctResponseField.setBackground(Color.WHITE);
        responseField.setText("");
        responseField.requestFocusInWindow();
        displayStatistics();
        timeOfLastAction = System.currentTimeMillis();
    }

    /**
     *
     * @param usersResponse
     */
    protected void processResponse(String usersResponse) {
        // Code to handle key bounces
        if (usersResponse.equals("")) {
            if (System.currentTimeMillis() - timeOfLastAction < 500) return;
        }
        boolean reviewing = reviewOnlyMenuItem.isSelected();
        dontScoreMenuItem.setEnabled(false);
        if (firstTry) {
//            correctResponse = LANGUAGE.addSpecialCharacters(currentItem.getResponse());
            // Got it right first try
            if (currentItem.responseIsCorrect(usersResponse)) {
                markAsCorrect();
                if (reviewing) itemList.updateReviewItem(currentItem, true);
                displayMessage(currentItem.getStimulus(), currentItem.getResponse());
                getNextItem();
            }
            // Got it wrong first try; don't score until next section of this method
            else {
                
                correctResponseField.setBackground(Color.PINK);
                String asciiResponse = currentItem.getResponse();
                correctResponseField.setText(LANGUAGE.addSpecialCharacters(asciiResponse));
                responseField.selectAll();
                firstTry = false;
                dontScoreMenuItem.setEnabled(true);
                typoMenuItem.setEnabled(true);
                possiblyDisplayMatchingStimulus(usersResponse);
            }
        } else {
            if (currentItem.responseIsCorrect(usersResponse)) {
                // Eventually user gets it right, and we get to here
                markAsIncorrect();
                itemsInARowCorrect = 0;
                if (reviewing) itemList.updateReviewItem(currentItem, false);
                displayMessage(currentItem.getStimulus(), currentItem.getResponse());
                displayNewItem(itemList.chooseNextItemToDisplay(false, reviewing));
            }
            else {
                // Still getting it wrong
                possiblyDisplayMatchingStimulus(usersResponse);
                responseField.selectAll();
            }
        }
        isDirty = true;
    }

    private void markAsCorrect() {
        numberCorrect++;
        itemsInARowCorrect++;
        currentItem.promote();
        itemList.put(currentItem);
    }

    private void markAsIncorrect() {
        numberIncorrect++;
        currentItem.demote();
        itemList.put(currentItem);
    }

    /**
     * Gets next item to display. If ten items in a row have been answered
     * correctly, and we are not in review only mode, then force a virgin
     * item to be chosen (if possible).
     */
    private void getNextItem() {
        boolean reviewing = reviewOnlyMenuItem.isSelected();
        if (itemsInARowCorrect == 10 && !reviewing) {
            displayNewItem(itemList.chooseNextItemToDisplay(true, reviewing));
            itemsInARowCorrect = 0;
        } else {
            displayNewItem(itemList.chooseNextItemToDisplay(false, reviewing));
        }
    }

    private void displayStatistics() {
        String messageString;
        int numberOfTrials = numberCorrect + numberIncorrect;
        if (numberOfTrials <= 0)
            return;

        itemsSeen = itemList.getNumberOfItemsSeen();
        itemsLearned = itemList.getNumberOfItemsLearned();
        if (currentItem.isVirgin()) {
            messageString = " New item";
        } else if (currentItem.getTimesCorrect() - currentItem.getTimesIncorrect() >= 10) {
            messageString = " Review item";
        } else {
            String learned = String.format("%3.1f", new Double(itemsLearned));
            messageString = " Learned " + learned + " of " + itemsSeen + " items.";
        }
        numberOfTrialsLabel.setText(messageString);

        int percent = (100 * numberCorrect) / numberOfTrials;
        messageString = numberOfTrials + " items presented, " + percent + "% correct";
        percentLabel.setText(messageString);
    }

//    private void displayPreviousItemInMessageArea() {
//        int numberOfTrials = numberCorrect + numberIncorrect;
//        if (numberOfTrials <= 0) return;
//        displayMessage(previousStimulus, previousResponse);
//    }

    private void possiblyDisplayMatchingStimulus(String usersResponse) {
        String foundStimulus = itemList.searchForResponse(usersResponse);
        if (foundStimulus == null) return;
        if (currentItem.getStimulus().equals(foundStimulus)) return;
        displayMessage("No, \"" + usersResponse + "\" is",
                       "\"" + foundStimulus + "\"");
    }

    private void attachListeners() {
        attachMenuListeners();
        responseField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processResponse(responseField.getText());
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }

    private void attachMenuListeners() {
        // Load...
        loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cancelQuitting()) return;
                load();
            }
        });
        // Save
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        // Save as...
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        // Quit
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitSafely();
            }
        });
        // Don't score
        dontScoreMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dontScore();
            }
        });
        // Fix item...
        fixItemMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FixItemDialog(thisGui, itemList, currentItem).showDialog();
                stimulusField.setText(currentItem.getStimulus());
                correctResponseField.setText(currentItem.getResponse());

            }
        });
        // Typo
        typoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAsCorrect();
                getNextItem();
            }
        });
        // Font Size...
        fontSizeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newSize =
                        JOptionPane.showInputDialog(thisGui,
                                                    "Enter font size, in points (8..36)",
                                                    mainFontSize);
                setFontSize(newSize);
            }
            
        });
        // Very Easy
        veryEasyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.setDifficulty(2.0);
            }
        });
        // Easy
        easyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.setDifficulty(2.5);
            }
        });
        // Medium
        mediumMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.setDifficulty(3.0);
            }
        });
        // Difficult
        difficultMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.setDifficulty(3.5);
            }
        });
        // Very Difficult
        veryDifficultMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.setDifficulty(4.0);
            }
        });
        // Review Only
        reviewOnlyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.setReviewOnly(reviewOnlyMenuItem.isSelected());
            }
        });
        // Help
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(thisGui,
                        helpMessage());
            }
            
        });
    }
    
    private static boolean almostEqual(double d1, double d2) {
        return Math.abs(d1 - d2) < 0.001;
    }
    
    private void setDifficultyRadioButtons(double d) {
        JRadioButtonMenuItem button = mediumMenuItem;
        if (almostEqual(d, 2.0)) button = veryEasyMenuItem;
        if (almostEqual(d, 2.5)) button = easyMenuItem;
        if (almostEqual(d, 3.0)) button = mediumMenuItem;
        if (almostEqual(d, 3.5)) button = difficultMenuItem;
        if (almostEqual(d, 4.0)) button = veryDifficultMenuItem;
        button.setSelected(true);
    }

    // -----------------------------------------------------------

    /**
     *  Causes current item to not be scored, either correct or incorrect.
     */
    protected void dontScore() {
        correctResponseField.setBackground(Color.WHITE);
        correctResponseField.setText("");
        responseField.selectAll();
        firstTry = true;
    }

    /**
     * Load new ItemList.
     */
    public void load() {
        try {
            itemList = new ItemList();
            itemList.load(Drill.class);
            setDifficultyRadioButtons(ItemList.getDifficulty());
            setTitle(itemList.getFileName() + "    (" + itemList.size() + " items)");
            isDirty = false;
            numberIncorrect = 0;
            numberCorrect = 0;
        }
        catch (Exception e) {
            if (tryAgain("load", e)) {
                load();
                displayNewItem(itemList.chooseNextItemToDisplay(false, itemList.reviewMode));
            }
            else {
                dispose();
                System.exit(0);
            }
        }
    }

    /**
     * Saves the ItemList; if it fails, allow another attempt.
     */
    protected void save() {
        try {
            itemList.save();
            isDirty = false;
        }
        catch (Exception e) {
            if (tryAgain("save", e)) {
                save();
            }
        }
    }

    /**
     * Saves the ItemList; may fail silently.
     */
    protected void saveIfPossible() {
        try {
            itemList.save();
        }
        catch (Exception e) { // do nothing
        }
    }

    /**
     * Saves the ItemList on a new file.
     */
    protected void saveAs() {
        try {
            itemList.saveAs();
            setTitle(itemList.getFileName());
            isDirty = false;
        }
        catch (Exception e) {
            if (tryAgain("save new", e)) {
                saveAs();
            }
        }
    }

    private boolean tryAgain(String msg, Exception e) {
        String problem = e.getMessage();
        String errorMessage = "Unable to " + msg + " file.\n"
                + (problem == null ? "" : problem + "\n")
                + "Would you like to try a different file?";
        int retry = JOptionPane.showConfirmDialog(this,
                                                  errorMessage,
                                                  "I/O Error",
                                                  JOptionPane.YES_NO_OPTION);
        return (retry == JOptionPane.YES_OPTION);
    }

    /**
     * Makes sure ItemList is saved before quitting; no longer needed.
     */
    void quitSafely() {
        if (cancelQuitting()) return;
        quit();
    }

    /**
     * Just quit.
     */
    protected void quit() {
        dispose();
    }

    /**
     * @return <code>true</code> if operation is cancelled.
     */
    private boolean cancelQuitting() {
        if (isDirty) {
            save();
//            int yesNoCancel = JOptionPane
//                .showConfirmDialog(thisGui, "Save the current file first?");
//            switch (yesNoCancel) {
//                case JOptionPane.YES_OPTION:
//                    save();
//                    return false;
//                case JOptionPane.NO_OPTION:
//                    return false;
//                case JOptionPane.CANCEL_OPTION:
//                    return true;
//            }
        }
        return false;
    }
    
    private void setFontSize(String newSize) {
        try {
            int size = new Integer(newSize).intValue();
            setFontSize(size);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(thisGui,
                    "Illegal font size: " + newSize);
        }
    }

    private void setFontSize(int size) {
        if (size < 8 || size > 36) throw new NumberFormatException(size + "");
        mainFontSize = size;
        userPrefs = Preferences.userNodeForPackage(Drill.class);
        userPrefs.putInt("DrillFontSize", mainFontSize);
        messageFontSize = Math.max(size - 6, 8);
        mainFont = new Font("Serif", Font.PLAIN, mainFontSize);
        messageFont = new Font("SansSerif", Font.PLAIN, messageFontSize);
        stimulusField.setFont(mainFont);
        responseField.setFont(mainFont);
        correctResponseField.setFont(mainFont);
        percentLabel.setFont(messageFont);
        numberOfTrialsLabel.setFont(messageFont);
        previousItemStimulus.setFont(messageFont);
        previousItemResponse.setFont(messageFont);
        pack();
    }
    
    /**
     * Tells how to use this Drill program.
     * @return Instructions.
     */
    private static String helpMessage() {
        return
            "Semicolons (;) separate alternatives; you may respond with any one,\n" +
            "or any combination (separated by semicolons), in any order.\n" +
            "\n" +
            "Parentheses enclose additional information, which should not be part\n" +
            "of your response.\n" +
            "\n" +
            "When responding to a German verb, always begin your response with \"to\".\n" +
            "\n" +
            "To enter umlauts on the Mac, type alt-u before the vowel. On Windows,\n" +
            "install the US-International keyboard, and type a double-quote before\n" +
            "the vowel.";
    }

    private void displayMessage(String text1, String text2) {
        previousItemStimulus.setText(text1);
        previousItemResponse.setText(text2);
    }

    /**
     * Runs the vocabulary drill program.
     * @param args Unused.
     * @throws UnsupportedLookAndFeelException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        new Drill().run();
    }

    private void run() {
        thisGui = this;
        load();
        userPrefs = Preferences.userNodeForPackage(Drill.class);
        setFontSize(userPrefs.getInt("DrillFontSize", mainFontSize));
        setVisible(true);
        displayNewItem(itemList.chooseNextItemToDisplay(false, itemList.reviewMode));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void catchQuit() {
        Runtime runtime = Runtime.getRuntime();
        Thread closerThread = new Thread(new Closer());
        runtime.addShutdownHook(closerThread);
    }

    private class Closer implements Runnable {
        @Override
        public void run() {
            if (isDirty) saveIfPossible();
        }
    }
}
