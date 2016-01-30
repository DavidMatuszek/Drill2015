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
import java.io.IOException;

import javax.swing.JButton;
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
 * Creator/editor for vocabulary lists.
 */
public class Build extends JFrame {
    // Menus
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem newMenuItem = menuItem("New...", KeyEvent.VK_N, false);
    private JMenuItem loadMenuItem = menuItem("Open...", KeyEvent.VK_O, false);
    private JMenuItem saveMenuItem = menuItem("Save", KeyEvent.VK_S, false);
    private JMenuItem saveAsMenuItem = menuItem("Save As...", KeyEvent.VK_S, true);
   
    private JMenuItem saveAsVirginMenuItem = new JMenuItem("Save As Virgin...");
    private JMenuItem quitMenuItem = menuItem("Save And Quit", KeyEvent.VK_Q, false);
    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem helpMenuItem = new JMenuItem("Help");
    private JMenuItem revertMenuItem = menuItem("Revert", KeyEvent.VK_Z, false);
    // Text fields
    private JTextField searchField = new JTextField(20);
    private JTextField stimulusField = new JTextField(30);
    private JTextField responseField = new JTextField(30);
    private JTextField timesCorrectField = new JTextField(6);
    private JTextField timesIncorrectField = new JTextField(6);
    private JTextField consecutiveTimesCorrectField = new JTextField(4);
    private JTextField intervalField = new JTextField(6);
    private JTextField displayDateField = new JTextField(6);
    private JTextField itemNumberField = new JTextField(6);
    // Time label
    private JLabel listSizeLabel = new JLabel("No items yet");
    // Buttons
    private JButton searchForward = new JButton(">");
    private JButton searchBackward = new JButton("<");
    private JButton previous = new JButton("<");
    private JButton next = new JButton(">");
    private JButton deleteItem = new JButton("Delete Item");
    private JButton newItem = new JButton("New Item");
    private Font font = new Font("Serif", Font.PLAIN, 18);

    // Non-GUI data
    private ItemList list;
    private boolean isDirty = false;
    private boolean creatingNewItem = false;
    
    /** holds actual list position, that is, (itemNumberField) - 1 */
    private int itemNumber;
    
    /** The currently displayed item */
    private Item item;
    
    private Build gui;

    /**
     * Normal constructor, for creating a GUI-based vocabulary
     * builder.
     */
    public Build() {
        createGui();
        attachListeners();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        list = new ItemList();
        gui = this;
    }

    /**
     * Constructor for a GUI-less builder, primarily for
     * testing purposes.
     *
     * @param itemList The Items to use.
     */
    Build(ItemList itemList) {
        list = itemList;
    }

    /**
     * Creates the GUI.
     */
    private void createGui() {
        buildMenuBar();

        JPanel searchPanel = makeSearchPanel();
        JPanel mainPanel = makeMainPanel();
        JPanel controlPanel = makeControlPanel();

        add(searchPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

       setBackground(Color.LIGHT_GRAY);
    }

    /**
     * Builds the menu bar and menus.
     */
    private void buildMenuBar() {
        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(newMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(saveAsVirginMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(quitMenuItem);
        menuBar.add(helpMenu);
        helpMenu.add(helpMenuItem);
        helpMenu.add(revertMenuItem);
    }

    /**
     * Attaches listeners to all active components, except those
     * in the menus.
     */
    private void attachListeners() {
        attachMenuListeners();
        // Search forward
        searchForward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchForward();
            }
        });
        // Search backward
        searchBackward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBackward();
            }
        });
        // Go to next item
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });
        // Go to previous item
        previous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previous();
            }
        });
        // Delete item
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteItem();
            }
        });
        // New Item
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startCreatingItem();
            }
        });
        // "Return" in search field --> search forward
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchForward();
            }
        });
        // "Return" in stimulus field --> go to response field
        stimulusField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int duplicate = list.searchForStimulus(get(stimulusField).trim());
                if (duplicate != -1 && duplicate != itemNumber) {
                    showItem(duplicate);
                }
                responseField.requestFocus();
            }
        });
        // "Return" in response field --> finish item
        responseField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startCreatingItem();
            }
        });
        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitSafely();
            }
        });        
    }

    /**
     * Attaches listeners to the menu items.
     */
    private void attachMenuListeners() {
        // New...
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFile();
            }
        });
        // Load...
        loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                load();
            }
        });
        // Save
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getModifiers() == 00 ) {System.out.println("Shifted!");}
                save();
            }
        });
        // Save As...
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        // Save As Virgin...
        saveAsVirginMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAsVirgin();
            }
        });
        // Quit
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitSafely();
            }
        });
        // Help
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String message =  "To find an item, enter any substring or regular expression in the\n"
                                + "Find field, and hit Enter. Use the < and > arrows to search for\n"
                                + "additional matching items.\n"
                                + "\n"
                                + "To create items,\n"
                                + "   * Type in the stimulus, then hit Return. The cursor will move to the\n"
                                + "     Response field.\n"
                                + "        - If this is a new stimulus, the response field will be blank.\n"
                                + "        - If this is an existing stimulus, the response will be shown.\n"
                                + "          You may edit it or leave it alone.\n"
                                + "  *  Hit Return while in the Response field. The item will be saved, and\n"
                                + "     the Stimulus and Response fields will be cleared for a new entry.\n"
                                + "\n"
                                + "To enter umlauts on the Mac, hit Alt-u, then desired vowel.\n"
                                + "\n"
                                + "To enter umlauts in Windows, use the United States-International\n"
                                + "keyboard, then hit a double-quote (\") followed by the vowel.\n"
                                + "\n"
                                + "The item position in the list, the times correct, the times incorrect, and\n"
                                + "the desired display date (number of item presentations before this item is\n"
                                + "shown again) are editable; the interval between presentations is computed\n"
                                + "from the times correct and times incorrect, and is not editable.\n"
                                + "\n"
                                + "The arrows at the bottom of the screen can be used to step forward and\n"
                                + "backward through all the items.\n"
                                + "\n"
                                + "The \"Delete Item\" button deletes the currently displayed item.\n"
                                + "\n"
                                + "The \"New Item\" button saves the currently displayed item and clears the\n"
                                + "screen to allow the next item to be entered. This is equivalent to hitting\n"
                                + "Enter while in the Response field.";
                JOptionPane.showMessageDialog(gui, message);  
            }            
        });
        // Revert
        revertMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                stimulusField.setText(item.getStimulus());
                responseField.setText(item.getResponse());
            }
            
        });
    }

    /**
     * Returns a menu item whose text and accelerator keys do not
     * depend on which operating system is being used. If no
     * accelerator key is desired, use zero for that parameter.
     *
     * @param words The menu text to use.
     * @param key The accelerator (shortcut) key to use.
     * @param shifted <code>true</code> if the shift key should be held down.
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
     * Creates the panel containing the "search" field.
     * 
     * @return The search panel.
     */
    private JPanel makeSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(new JLabel(" Find:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JPanel searchButtonPanel = new JPanel();
        searchButtonPanel.setLayout(new GridLayout(1, 2));
        searchButtonPanel.add(searchBackward);
        searchButtonPanel.add(searchForward);
        searchPanel.add(searchButtonPanel, BorderLayout.EAST);
        return searchPanel;
    }

    /**
     * Creates the panel containing fields for the stimulus, response,
     * item number, times correct, interval, and display date.
     * 
     * @return The panel containing fields for Item information.
     */
    private JPanel makeMainPanel() {
        searchField.setFont(font);
        stimulusField.setFont(font);
        responseField.setFont(font);
        
        JPanel mainPanel = new JPanel();
        JPanel stimulusResponsePanel = new JPanel();
        JPanel stimulusPanel = new JPanel();
        JPanel responsePanel = new JPanel();
        JPanel statisticsPanel = new JPanel();

        mainPanel.setLayout(new GridLayout(2, 1));
        stimulusPanel.add(new JLabel("Stimulus:"), BorderLayout.WEST);
        stimulusPanel.add(stimulusField, BorderLayout.CENTER);
        responsePanel.add(new JLabel("Response:"), BorderLayout.WEST);
        responsePanel.add(responseField, BorderLayout.CENTER);

        stimulusResponsePanel.setLayout(new GridLayout(2, 1));
        stimulusResponsePanel.add(stimulusPanel);
        stimulusResponsePanel.add(responsePanel);
        stimulusResponsePanel.setBackground(Color.RED);

        statisticsPanel.setLayout(new GridLayout(2, 2));
        JPanel itemNumberPanel = new JPanel();
        itemNumberPanel.setLayout(new FlowLayout());
        itemNumberPanel.add(listSizeLabel);
        itemNumberPanel.add(itemNumberField);
        statisticsPanel.add(itemNumberPanel);
        statisticsPanel.add(makePanel("Interval:", intervalField));
        intervalField.setEditable(false);

        statisticsPanel.add(makePanel("Times correct:", timesCorrectField));
        statisticsPanel.add(makePanel("Consecutive correct:", consecutiveTimesCorrectField)); // was a blank JLabel
        statisticsPanel.add(makePanel("Display date:", displayDateField));
        statisticsPanel.add(makePanel("Times incorrect:", timesIncorrectField));

        mainPanel.add(stimulusResponsePanel);
        mainPanel.add(statisticsPanel);

        mainPanel.setBackground(Color.GREEN);
        return mainPanel;
    }

    /**
     * Creates the panel containing buttons for stepping forward and
     * backward through the ItemList, and for deleting and adding Items.
     * 
     * @return The panel containing buttons.
     */
    private JPanel makeControlPanel() {
        JPanel panel = new JPanel();
        JPanel movePanel = new JPanel();
        JPanel deleteItemPanel = new JPanel();
        JPanel newItemPanel = new JPanel();

        panel.setLayout(new BorderLayout());

        deleteItemPanel.setLayout(new FlowLayout());
        deleteItemPanel.add(deleteItem);
        panel.add(deleteItemPanel, BorderLayout.WEST);

        movePanel.setLayout(new FlowLayout());
        movePanel.add(previous);
        movePanel.add(next);
        panel.add(movePanel, BorderLayout.CENTER);

        newItemPanel.setLayout(new FlowLayout());
        newItemPanel.add(newItem);
        panel.add(newItemPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates a panel containing a label and a text field.
     * 
     * @param label The label for the text field.
     * @param textField The text field.
     * @return The panel containing the desired label and text field.
     */
    private static JPanel makePanel(String label, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel(label));
        panel.add(textField);
        return panel;
    }
    
    // -------------------- No logic above this line --------------------
    
    /**
     * Checks whether the currently displayed item is valid, and if so,
     * updates the ItemList; otherwise, displays a message indicating
     * the nature of the problem. An item with blank stimulus and
     * response is simply discarded.
     * 
     * @return <code>true</code> if there is no problem, <code>false</code>
     * otherwise.
     */
    private boolean okToTakeAction() {
        if (displayIsBlank()) return true;

        String message = errorsInDisplay();
        if (message != null) {
            JOptionPane.showMessageDialog(this, message);
            return false;
        }
        try {
            updateList();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Updates the ItemList from the display.
     * <ul>
     *   <li>If the currently displayed item is new and valid, it is
     *       added to the ItemList.</li>
     *   <li>If a pre-existing item is modified in a valid way, it is
     *       changed in the ItemList.</li>
     *   <li>If the currently displayed item is invalid for any reason,
     *       an exception is thrown and the ItemList is not changed. </li>
     * </ul>
     * @throws IllegalArgumentException If the displayed item is invalid.
     */
    private void updateList() throws IllegalArgumentException {
        int newPosition = getInt(itemNumberField, "Item number") - 1;
        if (newPosition < 0 || newPosition > list.size()) {
            throw new IllegalArgumentException("\"Item number\" must be between 1 and "
                                               + (list.size() + 1) + ".");
        }
        if (creatingNewItem) {
            Item theNewItem = new Item(stimulusField.getText(),
                                       responseField.getText(),
                                       getInt(timesCorrectField, "Times correct"),
                                       getInt(timesIncorrectField, "Times incorrect"),
                                       getInt(consecutiveTimesCorrectField, "Consecutive correct"),
                                       getInt(intervalField, "Interval"),
                                       getDate());
            itemNumber = getInt(itemNumberField, "Item number") - 1;
            list.add(itemNumber, theNewItem);
            item = theNewItem;
            isDirty = true;
            creatingNewItem = false;
        }
        else { // Viewing and/or modifying pre-existing item
            boolean changed = item.modify(stimulusField.getText(),
                                          responseField.getText(),
                                          getInt(timesCorrectField, "Times correct"),
                                          getInt(timesIncorrectField, "Times incorrect"),
                                          getInt(consecutiveTimesCorrectField, "Consecutive correct"),
                                          getInt(intervalField, "Interval"),
                                          getDate());
            if (changed) isDirty = true;
            if (newPosition != itemNumber) {
                list.moveItem(itemNumber, newPosition);
                itemNumber = newPosition;
                isDirty = true;
            }
        }
    }
    
    /**
     * Finds the next Item (end around) that satisfies the regular
     * expression given in the search field. Both the stimulus field
     * and the response field are searched. If no such element is
     * found, the search field changes color momentarily.
     */
    protected void searchForward() {
        if (!okToTakeAction()) return;
        
        String pattern = searchField.getText();      
        int location = list.searchForward(itemNumber, pattern);
        displayIfFound(location);
    }
    
    /**
     * Finds the previous Item (end around) that satisfies the regular
     * expression given in the search field. Both the stimulus field
     * and the response field are searched. If no such element is
     * found, the search field changes color momentarily.
     */
    protected void searchBackward() {
        if (!okToTakeAction()) return;
        
        String pattern = searchField.getText();      
        int location = list.searchBackward(itemNumber, pattern);
        displayIfFound(location);
    }

    /**
     * If an Item is found by a search command, display it, otherwise
     * flash the search field.
     * @param lookAt The index of the found item, or -1 if not found.
     */
    private void displayIfFound(int lookAt) {
        if (lookAt >= 0) showItem(lookAt);
        else new Pinker().start();
    }

    /**
     * Displays the next Item (end around) in the ItemList.
     */
    protected void next() {
        if (!okToTakeAction()) return;
        showItem(itemNumber + 1);
    }

    /**
     * Displays the previous Item (end around) in the ItemList.
     */
    protected void previous() {
        if (!okToTakeAction()) return;
        showItem(itemNumber - 1);
    }

    /**
     * Deletes the currently displayed Item. The display does not
     * have to be in a valid state.
     */
    protected void deleteItem() {
        if (!creatingNewItem) list.remove(itemNumber);
        isDirty = true;
        showItem(itemNumber - 1);
    }

    /**
     * Clears the stimulus and response fields, and sets the other
     * fields to their default values. No Item is created at this
     * point; only the display is changed.
     */
    protected void startCreatingItem() {
        if (!okToTakeAction()) return;
        
        creatingNewItem = true;
        
        itemNumber = list.size();
        stimulusField.setText("");
        responseField.setText("");
        listSizeLabel.setText("Item (of " + list.size() + "):");
        itemNumberField.setText((itemNumber + 1) + "");
        timesCorrectField.setText("0");
        timesIncorrectField.setText("0");
        intervalField.setText(ItemList.intervalForLevel(0, ItemList.difficulty) + "");
        setDate(Integer.MAX_VALUE);
        stimulusField.requestFocus();
    }
    
    /**
     * Saves the current file and opens a new file.
     */
    protected void newFile() {
        if (!okToTakeAction()) return;
        save();
        
        try {
            list.newFile();
            setTitle(list.getFileName());
            startCreatingItem();
        }
        catch (Exception e) {
            if (tryAgain("create", e)) {
                newFile();
            }
        }
    }

    /**
     * If the currently displayed item is in a valid state, updates
     * and saves the ItemList, and loads a new ItemList chosen by
     * the user. If the currently displayed item is invalid, prompts
     * the user to correct it (just deleting it is also an option),
     * and does not proceed with saving and loading.
     */
    public void load() {
        if (!okToTakeAction()) return;
        save();

        try {
            list = new ItemList();
            list.load(Build.class);
            setTitle(list.getFileName());
            itemNumber = 0; // Display the first item
            item = list.get(itemNumber);
            showItem();
        }
        catch (Exception e) {
            if (tryAgain("load", e)) {
                load();
            }
            else {
                dispose();
                System.exit(0);
            }
        }
    }

    /**
     * Warns the user of an I/O exception, and asks whether to
     * try again with a different file.
     * 
     * @param msg A word or phrase such as "save new", to describe
     *            the I/O operation that failed.
     * @param e The exception thrown by the failed operation.
     * @return <code>true</code> if the user wants to try another file.
     */
    private boolean tryAgain(String msg, Exception e) {
        String problem = e.getMessage();
        String errorMessage = "Unable to " + msg + " file.\n   "
                + (problem == null ? "" : "   [" + problem + "]\n")
                + "Would you like to try a different file?";
        int retry = JOptionPane.showConfirmDialog(this,
                                                  errorMessage,
                                                  "I/O Error",
                                                  JOptionPane.YES_NO_OPTION);
        return (retry == JOptionPane.YES_OPTION);
    }
    
    private void editOrStartFile() {
        Object[] options = new String[] { "Start a new file", "Edit an existing file" };
        int option = JOptionPane.showOptionDialog(this,
                "What would you like to do?",
                "Option Dialog",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        if (option == 0) {
            newFile();
        }
        else if (option == 1) {
            load();
            searchField.requestFocus();
        }
    }

    /**
     * If the currently displayed item is in a valid state, updates
     * and saves the ItemList on the same file as it was loaded from.
     * If the currently displayed item is invalid, prompts the user
     * to correct it (just deleting it is also an option), and does
     * not proceed with saving the ItemList.
     */
    protected void save() {
        if (!okToTakeAction()) return;
        if (!isDirty) return;
        
        try {
            list.save();
            isDirty = false;
        }
        catch (Exception e) {
            if (tryAgain("save", e)) {
                save();
            }
        }
    }

    /**
     * If the currently displayed item is in a valid state, updates
     * and saves the ItemList on a file chosen by the user.
     * If the currently displayed item is invalid, prompts the user
     * to correct it (just deleting it is also an option), and does
     * not proceed with saving the ItemList.
     */
    protected void saveAs() {
        if (!okToTakeAction()) return;
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

    /**
     * If the currently displayed item is in a valid state, updates
     * and saves the ItemList on a file chosen by the user, removing
     * all information added by the Drill program--that is, all
     * information about how well Items have been learned.
     * If the currently displayed item is invalid, prompts the user
     * to correct it (just deleting it is also an option), and does
     * not proceed with saving the ItemList.
     */
    protected void saveAsVirgin() {
        if (!okToTakeAction()) return;
        save();
        try {
            list.saveAsVirgin();
            setTitle(list.getFileName());
            showItem();
            isDirty = false;
        }
        catch (Exception e) {
            if (tryAgain("save new", e)) {
                saveAsVirgin();
            }
        }
    }

    /**
     * If the currently displayed item is in a valid state, updates
     * and saves the ItemList on the same file as it was loaded from.
     * If the currently displayed item is invalid, prompts the user
     * to correct it (just deleting it is also an option), and does
     * not quit the program.
     */
    void quitSafely() {
        if (!okToTakeAction()) return;
        save();
        if (cancelQuitting()) return;
        quit();
    }

    /**
     * Saves the currently displayed Item to the ItemList if and only
     * if it is a valid Item, then saves the ItemList and quits the
     * program. If the currently displayed Item is invalid, it is
     * discarded.<p>
     * This method should be used only when the user cannot be given
     * an opportunity to correct errors in the displayed item, such
     * as when the program is quit other than by closing the window
     * or choosing "Quit" from the Java "File" menu.
     */
    protected void quit() {
        dispose();
    }

    /**
     * Warns the user that the ItemList has not been saved, and asks
     * whether to cancel the "Quit" operation.
     * 
     * @return <code>true</code> if the user does not want to quit
     * the program.
     */
    private boolean cancelQuitting() {
        if (isDirty) {
            save();
//            int yesNoCancel = JOptionPane
//                .showConfirmDialog(this, "Save the current file first?");
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

    /**
     * Displays the Item at the given position in the ItemList. The position
     * is wrapped if it goes outside the ItemList boundaries.
     * @param position The number of the item to be displayed.
     */
    private void showItem(int position) {
        itemNumber = list.adjust(position);
        showItem();
    }

    /**
     * Displays the Item in the ItemList at the position given by the
     * global variable "itemNumber".
     */
    private void showItem() {
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items!");
            return;
        }
        creatingNewItem = false;

        itemNumber = list.adjust(itemNumber); // Why be half safe?
        
        item = list.get(itemNumber);
        stimulusField.setText(item.getStimulus());
        responseField.setText(item.getResponse());
        listSizeLabel.setText("Item (of " + list.size() + "):");
        itemNumberField.setText((itemNumber + 1) + "");
        timesCorrectField.setText(item.getTimesCorrect() + "");
        timesIncorrectField.setText(item.getTimesIncorrect() + "");
        consecutiveTimesCorrectField.setText(item.getConsecutiveTimesCorrect() + "");
        intervalField.setText(item.getInterval() + "");
        setDate(item.getDisplayDate());
        stimulusField.requestFocus();
    }

    /**
     * Gets and returns the value in the display date field.
     * If the value is the word "MAX", the largest possible
     * integer value is returned.
     * @return A number representing the contents of the
     * display date field.
     */
    private int getDate() {
        String date = displayDateField.getText();
        if (date.trim().toUpperCase().equals("MAX")) {
            return Integer.MAX_VALUE;
        }
        else if (isInt(displayDateField)){
            return getInt(displayDateField, "Display date");
        }
        else return -1;
    }

    /**
     * Displays the current Item's display date in the display
     * date field. If the date is the largest possible integer
     * (representing a virgin item), the string "MAX" is
     * displayed instead.
     *
     * @param date The date to be displayed.
     */
    private void setDate(int date) {
        if (date == Integer.MAX_VALUE) {
            displayDateField.setText("MAX");
        }
        else {
            displayDateField.setText(item.getDisplayDate() + "");
        }
    }

    /**
     * Tests whether the current item has empty stimulus
     * and response fields; such items can be discarded.
     *
     * @return <code>true</code> if the item is blank,
     *  therefore discardable.
     */
    private boolean displayIsBlank() {
        return get(stimulusField).length() == 0
                && get(responseField).length() == 0;
    }

    /**
     * Tests if the display shows an invalid item.
     * @return An appropriate error message if there is an invalid
     * field in the display, or <code>null</code> if the displayed
     * item is valid.
     */
    private String errorsInDisplay() {
        String message = null;
        // Stimulus
        if (get(stimulusField).length() == 0) {
            message = "Stimulus is missing.";
        }
        // Response
        else if (get(responseField).length() == 0) {
            message = "Response is missing.";
        }
        // Times correct
        else if (get(timesCorrectField).length() == 0) {
            message = "\"Times correct\" field is blank.";
        }
        else if (!isInt(timesCorrectField)) {
            message = "\"Times correct\" field is not a number.";
        }
        // Times incorrect
        else if (get(timesIncorrectField).length() == 0) {
            message = "\"Times incorrect\" field is blank.";
        }
        else if (!isInt(timesIncorrectField)) {
            message = "\"Times incorrect\" field is not a number.";
        }
        // Interval
        else if (get(intervalField).length() == 0) {
            message = "\"Interval\" field is blank.";
        }
        else if (!isInt(intervalField)) {
            message = "\"Interval\" field is not a number.\"";
        }
        // Display date
        else if (get(displayDateField).length() == 0) {
            message = "\"Display date\" field is blank.";
        }
        else if (getDate() < 0) {
            message = "Error in \"Display date\" field.";
        }
        else if (!isInt(itemNumberField)
                || getInt(itemNumberField, "Item number") < 1
                || getInt(itemNumberField, "Item number") > (list.size() + 1)) {
            message = "\"Item number\" must be a number between 1 and "
                + (list.size() + 1) + ".";
        }
        else if (creatingNewItem) {
            int duplicate = list.searchForStimulus(get(stimulusField).trim());
            if (duplicate != -1 && duplicate != itemNumber) {
                message = "This stimulus was entered previously.\n" +
                          "Your new entry has not been added;\n" +
                          "instead, the old entry will be shown.\n" +
                          "You may edit the entry if you wish.";
                showItem(duplicate);
            }
        }
        return message;
    }

    /**
     * Returns the value in a given field, with leading and trailing
     * whitespace removed.
     * 
     * @param field The field to be examined.
     * @return The value in the field.
     */
    private static String get(JTextField field) {
        return field.getText().trim();
    }
    
    /**
     * Returns <code>true</code> if the given field contains an integer.
     * 
     * @param field The field to be examined.
     * @return <code>true</code> if the given field contains an integer.
     */
    static boolean isInt(JTextField field) {
        try {
            Integer.parseInt(field.getText().trim());
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the integer value in the given field.
     * @param field The field to be examined.
     * @param fieldName The name of the field, to be used if an exception
     *        needs to be thrown.
     * @return The integer value in the given field.
     * @throws IllegalArgumentException If the given field does not contain an integer.
     */
    static int getInt(JTextField field, String fieldName)
            throws IllegalArgumentException {
        String numeral = field.getText().trim();
        if (numeral.length() == 0) {
            throw new IllegalArgumentException("\"" + fieldName
                                               + "\" field should not be blank.");
        }
        return Integer.parseInt(numeral);
    }

    /**
     * Runs the vocabulary builder program.
     * @param args Unused.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        new Build().run();
    }

    private void run() {
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
        catchQuit();
        editOrStartFile();
    }

    /**
     * When the program is quit, starts a Thread to save data.
     */
    private void catchQuit() {
        Runtime runtime = Runtime.getRuntime();
        Thread closerThread = new Thread(new Closer());
        runtime.addShutdownHook(closerThread);
    }
    
    /**
     * Attempts to save all possible data when the program is quit.
     */
    private class Closer implements Runnable {
        @Override
        public void run() {
            try {
                updateList();
            }
            catch (IllegalArgumentException e) {
                System.out.println("Currently displayed item is invalid!");
            }
            finally {
                if (isDirty) try {
                    list.save();
                }
                catch (IOException e) {
                    System.out.println("Unable to save file!");
                }
            }
        }
    }
    
    /**
     * Turns the search field pink for a brief period.
     */
    class Pinker extends Thread {
        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            searchField.setBackground(Color.PINK);
            try { Thread.sleep(500); } catch (InterruptedException e) {
                // empty block
            }
            searchField.setBackground(Color.WHITE);
        }
    }
}
