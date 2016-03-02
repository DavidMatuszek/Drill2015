package drill;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 * The main class for the ImageDrill flash card program.
 *
 * @author Dave Matuszek
 * @version April 12, 2007
 */
public class ImageDrill extends JFrame {

    private transient final SpecialCharacters LANGUAGE = new Japanese();

    private JPanel stimulusPanel = new JPanel();
    private JTextField correctResponseField = new JTextField(40);;
    private JTextField responseField = new JTextField(40);
    private JLabel message = new JLabel("");
    private JMenuBar menuBar = new JMenuBar();

    private JMenu fileMenu = new JMenu("File");
    private JMenuItem loadMenuItem = new JMenuItem("Load...");
    private JMenuItem saveMenuItem = new JMenuItem("Save");
    private JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    private JMenuItem quitMenuItem = new JMenuItem("Quit");

    private JMenu specialMenu = new JMenu("Special");
    private JMenuItem dontScoreMenuItem = new JMenuItem("Don't score");
    private JMenuItem fixItemMenuItem = new JMenuItem("Fix item...");
    private JMenuItem typoMenuItem = menuItem("Typo", KeyEvent.VK_T);

    private Font font;

    private ImageDrill thisGui;
    private ItemList list;
    private boolean isDirty;
    private transient Item currentItem;
    private boolean itemIsNew;
    private int numberOfTrials = 0;
    private int numberCorrect = 0;
    private String correctResponse = "";
    private int itemsSeen = 0;
    private double itemsLearned = 0;
    
    private ArrayList<Image> pictures;
    private ArrayList<String> fileNames;

    /**
     * Constructor for the ImageDrill program.
     */
    public ImageDrill() {
        
        // TODO these two lines need to be moved to the load() method
        pictures = new ArrayList<Image>();
        fileNames = new ArrayList<String>();
        
        buildGui();
        attachListeners();
        pack();
        setLocationRelativeTo(getParent());
        catchQuit();
    }

    /**
     * Creates the main GUI for the ImageDrill program.
     */
    private void buildGui() {
        font = new Font("Serif", Font.PLAIN, 18);
        setTitle("ImageDrill 2007");
        buildMenuBar();
        setLayout(new BorderLayout());
        JPanel textPanels = new JPanel();
        textPanels.setLayout(new GridLayout(3, 1));
        setBackground(new Color(224, 224, 224));

        add(stimulusPanel, BorderLayout.CENTER);
        stimulusPanel.setMinimumSize(new Dimension(100, 100));
        stimulusPanel.setSize(new Dimension(100, 100));
        add(textPanels, BorderLayout.EAST);

        JPanel responsePanel = new JPanel();
        responsePanel.setLayout(new FlowLayout());
        responseField.setFont(font);
        responsePanel.add(responseField);
        textPanels.add(responsePanel);

        JPanel correctResponsePanel = new JPanel();
        correctResponsePanel.setLayout(new FlowLayout());
        correctResponseField.setFont(font);
        correctResponseField.setEditable(false);
        correctResponsePanel.add(correctResponseField);
        textPanels.add(correctResponsePanel);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout());
        messagePanel.add(message);
        textPanels.add(messagePanel);

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
    }

    /**
     * Returns a menu item whose text and accelerator keys do not
     * depend on which operating system is being used. If no
     * accelerator key is desired, use zero for that parameter.
     *
     * @param words The menu text to use.
     * @param key The accelerator (shortcut) key to use.
     * @return The complete menu item.
     */
    private JMenuItem menuItem(String words, int key) {
        JMenuItem menuItem = new JMenuItem(words);
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
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
        itemIsNew = true;
        currentItem = item;
        
        
        
//        stimulusField.setText(item.getStimulus());
        
        if (pictures == null || pictures.size() == 0) {
            try {
                pictures = new ImageHandler(this).loadImages();
            }
            catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        stimulusPanel.getGraphics().drawImage(pictures.get(0), 0, 0, this);
        
        
        
        correctResponseField.setText("");
        correctResponseField.setBackground(Color.WHITE);
        responseField.setText("");
        responseField.requestFocusInWindow();
        message.setText("");
        displayStatistics();
    }

    /**
     *
     * @param text
     */
    protected void processResponse(String text) {
        dontScoreMenuItem.setEnabled(false);
        if (itemIsNew) {
            correctResponse = LANGUAGE.addSpecialCharacters(currentItem.getResponse());
            // Got it right first try
            if (currentItem.responseIsCorrect(text)) {
                markAsCorrect();
            }
            // Got it wrong first try
            else {
                correctResponseField.setBackground(Color.PINK);
                String asciiResponse = currentItem.getResponse();
                correctResponseField.setText(LANGUAGE.addSpecialCharacters(asciiResponse));
                responseField.selectAll();
                itemIsNew = false;
                dontScoreMenuItem.setEnabled(true);
                typoMenuItem.setEnabled(true);
            }
        } else {
            // Eventually got it right
            if (currentItem.responseIsCorrect(text)) {
                numberOfTrials++;
                currentItem.demote();
                list.put(currentItem);
                displayNewItem(list.chooseNextItemToDisplay(false));
            }
            else {
                // Still getting it wrong
                responseField.selectAll();
                message.setText("Still not correct.");
                responseField.selectAll();
            }
        }
        isDirty = true;
    }

    private void markAsCorrect() {
        numberOfTrials++;
        numberCorrect++;
        currentItem.promote();
        list.put(currentItem);
        displayNewItem(list.chooseNextItemToDisplay(false));
    }

    private void displayStatistics() {
        String messageString;
        if (numberOfTrials <= 0) return;

        itemsSeen = list.getNumberOfItemsSeen();
        int itemsCorrect = list.getNumberOfItemsCorrect();
        itemsLearned = list.getNumberOfItemsLearned();
        messageString = itemsSeen + " seen, " + itemsCorrect
                + " correct, " + itemsLearned + " learned.";
        int percent = (100 * numberCorrect) / numberOfTrials;
        messageString += "     " + itemsSeen + " seen, "
//                + itemsCorrect + " correct, "
                + itemsLearned + " learned.";
        displayMessage(messageString);
    }

    private void attachListeners() {
        attachMenuListeners();
        responseField.addActionListener(new ActionListener() {
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
            public void actionPerformed(ActionEvent e) {
                if (cancelQuitting()) return;
                load();
            }
        });
        // Save
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        // Save as...
        saveAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        // Quit
        quitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitSafely();
            }
        });
        // Don't score
        dontScoreMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dontScore();
            }
        });
        // Fix item...
        fixItemMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FixItemDialog(thisGui, (ItemList)null, currentItem);

//                stimulusField.setText(currentItem.getStimulus()); // TODO update
//                correctResponseField.setText(currentItem.getResponse());

            }
        });
        // Typo
        typoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                markAsCorrect();
            }
        });
    }

    // -----------------------------------------------------------

    protected void dontScore() {
        correctResponseField.setBackground(Color.WHITE);
        correctResponseField.setText("");
        responseField.selectAll();
        itemIsNew = true;
    }

    protected void load() {
        try {
            list.load(ImageDrill.class);
            setTitle(list.getFileName() + "    (" + list.size() + " items)");
            isDirty = false;
            numberOfTrials = 0;
            numberCorrect = 0;
        }
        catch (Exception e) {
            if (tryAgain("load", e)) {
                load();
                displayNewItem(list.chooseNextItemToDisplay(false));
            }
            else {
                dispose();
                System.exit(0);
            }
        }
    }

    protected void save() {
        try {
            list.save();
            isDirty = false;
        }
        catch (Exception e) {
            if (tryAgain("save", e)) {
                save();
            }
        }
        finally {
        }
    }

    protected void saveIfPossible() {
        try {
            list.save();
        }
        catch (Exception e) {
        }
    }

    protected void saveAs() {
        try {
            list.saveAs();
            setTitle(list.getFileName());
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

    void quitSafely() {
        if (cancelQuitting()) return;
        else quit();
    }

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

    private void displayMessage(String text) {
        message.setText(text);
    }

    /**
     * Runs the image drill program.
     * @param args Unused.
     */
    public static void main(String[] args) {
        new ImageDrill().run();
    }

    private void run() {
        thisGui = this;
        list = new ItemList();
        load();
        setVisible(true);
        displayNewItem(list.chooseNextItemToDisplay(false));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void catchQuit() {
        Runtime runtime = Runtime.getRuntime();
        Thread closerThread = new Thread(new Closer());
        runtime.addShutdownHook(closerThread);
    }

    private class Closer implements Runnable {
        public void run() {
            if (isDirty) saveIfPossible();
        }
    }
}
