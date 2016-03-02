/**
 * 
 */
package drill;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import static drill.ItemListIO.*;

/**
 * @author David Matuszek
 */
/**
 * @author David Matuszek
 */
/**
 * @author David Matuszek
 */
/**
 * @author David Matuszek
 */
public class Merge extends JFrame {

    private ItemList baseList = null;
    private ItemList nextList = null;
    private ItemList conflictList = new ItemList();
    private int baseIndex = -1; // The index of the item to update in baseList
    private Set<String> stimuli = new HashSet<String>();
    private Item currentItem;
    private Double difficulty = -1.0;
    private int choice;
    private int numberToCheck = 0;
    private int numberChecked = 0;
    private boolean quitting = false;

    /**
     * @throws HeadlessException
     */
    public Merge() throws HeadlessException {
        super("Program to combine vocabulary lists");
    }

    /**
     * @param args
     * @throws UnsupportedLookAndFeelException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("TextField.font", new Font(Font.SERIF, Font.PLAIN, 24));
        new Merge().run();

    }

    private void run() {
        createGui();
        addListeners();
        setLocationRelativeTo(getParent());
        pack();
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        showInstructions();
        
        initialize();
    }

    private void showInstructions() {
        String message =
                "This program merges multiple vocabulary lists (ItemLists).\n" + 
                "\n" + 
                "You will first be asked to choose a vocabulary list. This will form the basis of\n" + 
                "the new, merged vocabulary list. After that, you will be asked to choose another\n" + 
                "vocabulary list to merge into it.\n" + 
                "\n" + 
                "The program will then check each item in the new vocabulary list to see if it\n" + 
                "has exactly the same stimulus (foreign word or phrase) as in the list being\n" + 
                "built. If not, it will quietly be added to the merged list. However, if the new\n" + 
                "item has the same stimulus as an existing item, three responses will be displayed:\n" + 
                "the old item, the new item, and a suggested combination of the two. You can then\n" + 
                "edit any (or all) of these, choose which one you would like to keep by clicking\n" + 
                "on it, then click the OK button.\n" + 
                "\n" + 
                "The numeric fields represent (1) the number of times this item has been answered\n" + 
                "correctly, (2) the number of times it has been answered incorrectly, (3) how many\n" + 
                "of the most recent responses were consecutively correct, and (4) the estimated\n" + 
                "date at which the item will next be displayed. [On older vocabulary files, the\n" + 
                "consecutive correct, (3), was not recorded, and will be shown as zero.]\n" + 
                "\n" + 
                "When the entire vocabulary file has thus been processed, you will be asked to\n" + 
                "choose another file and repeat the process. To end the program at this point,\n" + 
                "just cancel the file request. You will then be prompted for a file on which to\n" + 
                "save the final result.";
        JOptionPane.showMessageDialog(this, message);
    }
    
    /**
     * 
     */
    private void addListeners() {
        radio1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = 1;
            }
        });
        radio2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = 2;
            }
        });
        radio3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = 3;
            }
        });
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (radio1.isSelected()) {
                    update(1);
                } else if (radio2.isSelected()) {
                    update(2);
                } else if (radio3.isSelected()) {
                    update(3);
                }
                if (quitting) {
                    chooseAndSaveOutputFile(baseList);
                }
                advance();
            }
        });
        englishWordField1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio1.setSelected(true);
                choice = 1;
            }
        });
        englishWordField2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio2.setSelected(true);
                choice = 2;
            }
        });
        englishWordField3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio3.setSelected(true);
                choice = 3;
            }
        });
    }

    /**
     * @param i
     */
    protected void update(int i) {
        if (i == 1) {
            currentItem.setResponse(englishWordField1.getText());
            currentItem.setTimesCorrect(getAsInt(numberCorrectField1));
            currentItem.setTimesIncorrect(getAsInt(numberIncorrectField1));
            currentItem.setConsecutiveTimesCorrect(getAsInt(consecCorrectField1));
            currentItem.setInterval(baseList.intervalForLevel(currentItem.getInterval(),
                                                              difficulty));
            currentItem.setDisplayDate(getAsInt(dateField1));
        } else if (i == 2) {
            currentItem.setResponse(englishWordField2.getText());
            currentItem.setTimesCorrect(getAsInt(numberCorrectField2));
            currentItem.setTimesIncorrect(getAsInt(numberIncorrectField2));
            currentItem.setConsecutiveTimesCorrect(getAsInt(consecCorrectField2));
            currentItem.setInterval(baseList.intervalForLevel(currentItem.getInterval(),
                                                              difficulty));
            currentItem.setDisplayDate(getAsInt(dateField2));
        } else if (i == 3) {
            currentItem.setResponse(englishWordField3.getText());
            currentItem.setTimesCorrect(getAsInt(numberCorrectField3));
            currentItem.setTimesIncorrect(getAsInt(numberIncorrectField3));
            currentItem.setConsecutiveTimesCorrect(getAsInt(consecCorrectField3));
            currentItem.setInterval(baseList.intervalForLevel(currentItem.getInterval(),
                                                              difficulty));
            currentItem.setDisplayDate(getAsInt(dateField3));
        }
    }

    /**
     * @param numberCorrectField12
     * @return
     */
    private int getAsInt(JTextField textField) {
        try {
            Integer number = new Integer(textField.getText());
            return number.intValue();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    JTextField foreignWordField = new JTextField();
    
    JTextField englishWordField1 = new JTextField(40);
    JTextField englishWordField2 = new JTextField(40);
    JTextField englishWordField3 = new JTextField(40);
    
    JTextField numberCorrectField1 = new JTextField(5);
    JTextField numberCorrectField2 = new JTextField(5);
    JTextField numberCorrectField3 = new JTextField(5);
    
    JTextField numberIncorrectField1 = new JTextField(5);
    JTextField numberIncorrectField2 = new JTextField(5);
    JTextField numberIncorrectField3 = new JTextField(5);
    
    JTextField consecCorrectField1 = new JTextField(5);
    JTextField consecCorrectField2 = new JTextField(5);
    JTextField consecCorrectField3 = new JTextField(5);

    JTextField dateField1 = new JTextField(12);
    JTextField dateField2 = new JTextField(12);
    JTextField dateField3 = new JTextField(12);

    JRadioButton radio1 = new JRadioButton();
    JRadioButton radio2 = new JRadioButton();
    JRadioButton radio3 = new JRadioButton();

    JButton okButton = new JButton("OK");
    
    JLabel conflictLabel = new JLabel("");
    
    Font bigFont = new Font(Font.SERIF, Font.PLAIN, 36);

    /**
     * Create the GUI.
     */
    private void createGui() {
        setLayout(new BorderLayout());
        add(foreignWordField, BorderLayout.NORTH);
        foreignWordField.setFont(bigFont);
        JPanel choices = new JPanel();
        choices.setLayout(new GridLayout(4, 1));
        choices.add(makeLabelPanel());
        choices.add(makeChoicePanel(radio1, englishWordField1, numberCorrectField1,
                                    numberIncorrectField1, consecCorrectField1, dateField1));
        choices.add(makeChoicePanel(radio2, englishWordField2, numberCorrectField2,
                                    numberIncorrectField2, consecCorrectField2, dateField2));
        choices.add(makeChoicePanel(radio3, englishWordField3, numberCorrectField3,
                                    numberIncorrectField3, consecCorrectField3, dateField3));
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(radio1);
        radioGroup.add(radio2);
        radioGroup.add(radio3);
        add(choices, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        okButton.setFont(bigFont);
        buttonPanel.add(okButton, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * @return
     */
    private Component makeLabelPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel subpanel = new JPanel();
        Font smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
        JLabel label = new JLabel("Right     Wrong    Consec   Date                    ");
        label.setFont(smallFont);
        subpanel.add(label);
        panel.add(subpanel, BorderLayout.EAST);
        panel.add(new JLabel(""), BorderLayout.CENTER);
        conflictLabel.setFont(smallFont);
        panel.add(conflictLabel, BorderLayout.WEST);
        return panel;
    }

    /**
     * Make one of the three response panels in this GUI.
     * @param radio
     * @param text
     * @param correct
     * @param incorrect
     * @param consecutive
     * @param date
     * @return
     */
    JPanel makeChoicePanel(JRadioButton radio,
                           JTextField text,
                           JTextField correct,
                           JTextField incorrect,
                           JTextField consecutive,
                           JTextField date) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(radio, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        JPanel numbers = new JPanel();
        new BoxLayout(numbers, BoxLayout.X_AXIS);
        numbers.add(correct);
        numbers.add(incorrect);
        numbers.add(consecutive);
        numbers.add(date);
        panel.add(numbers, BorderLayout.EAST);
        return panel;
    }

    private void readAndMergeLists() {
        List<ItemList> lists = new ArrayList<ItemList>(5);

        ItemList baseList = chooseAndReadInputFile();

        File file = chooseInputFile();
        while (file != null) {
            ItemList nextList = readOneList(file);
            if (nextList == null) continue;
            numberToCheck += nextList.size();
            numberChecked = 0;
            merge(baseList, nextList);
            file = chooseInputFile();
        }
    }
    
    /**
     * @param baseList
     * @param nextList
     */
    private void merge(ItemList baseList, ItemList nextList) {
        // FIXME This logic won't work; control must be from the GUI
        for (Item item : nextList) {
            int index = baseList.searchForStimulus(item.getStimulus());
            if (index == -1) {
                numberChecked += 1;
                baseList.add(item);
                continue;
            }
            Item baseItem = baseList.get(index);
            if (item.getResponse().equals(baseItem.getResponse())) {
                numberChecked += 1;
                continue;
            }
        }
    }
    
    private void initialize() {
        baseList = chooseAndReadInputFile();
        if (baseList == null) System.exit(0);
        for (Item item : baseList) {
            String stimulus = item.getStimulus();
            stimuli.add(stimulus);
        }
        difficulty = baseList.getDifficulty();
        advance();        
    }

    private void advance() {
        File file;
        if (conflictList.isEmpty()) {
            file = chooseInputFile();
            if (file == null) {
                quitting = true;
                chooseAndSaveOutputFile(baseList);
                return;
            }
            nextList = readOneList(file);
            this.setTitle(ItemListIO.getInputFileName());
            conflictList = new ItemList();
            for (Item item : nextList) {
                String stimulus = item.getStimulus();
                if (stimuli.contains(stimulus) && responsesDiffer(item)) {
                    conflictList.add(item);
                }
                else {
                    baseList.add(item);
                }
                stimuli.add(stimulus);
            }
            displayNumberOfConflictsFound(conflictList.size());
            advance();
        } else { // Conflicts found! Provide one item for user to respond to
            displayOneConflict();        
        }
    }

/**
     * @param item
     * @return
     */
    private boolean responsesDiffer(Item item) {
        String newResponse = item.getResponse();
        int index = baseList.searchForStimulus(item.getStimulus());
        String oldResponse = baseList.get(index).getResponse();
        return ! newResponse.equals(oldResponse);
    }

/**
     * 
     */
    private void displayOneConflict() {
        displayNumberOfConflictsFound(conflictList.size());
        // find
        Item newItem = conflictList.remove(0);
        String stimulus = newItem.getStimulus();
        baseIndex = baseList.searchForStimulus(stimulus);
        Item oldItem = baseList.get(baseIndex);
        currentItem = oldItem;
        // display old and new
        foreignWordField.setText(stimulus);
        englishWordField1.setText(oldItem.getResponse());
        englishWordField2.setText(newItem.getResponse());
        numberCorrectField1.setText(oldItem.getTimesCorrect() + "");
        numberCorrectField2.setText(newItem.getTimesCorrect() + "");
        numberIncorrectField1.setText(oldItem.getTimesIncorrect() + "");
        numberIncorrectField2.setText(newItem.getTimesIncorrect() + "");
        consecCorrectField1.setText(oldItem.getConsecutiveTimesCorrect() + "");
        consecCorrectField2.setText(newItem.getConsecutiveTimesCorrect() + "");
        dateField1.setText(oldItem.getDisplayDate() + "");
        dateField2.setText(newItem.getDisplayDate() + "");
        // create and display suggestions
        englishWordField3.setText(combineResponses(oldItem, newItem));
        numberCorrectField3.setText(oldItem.getTimesCorrect() +
                                    newItem.getTimesCorrect() + "");
        numberIncorrectField3.setText(oldItem.getTimesIncorrect() +
                                      newItem.getTimesIncorrect() + "");
        consecCorrectField3.setText(Math.max(oldItem.getConsecutiveTimesCorrect(),
                                             newItem.getConsecutiveTimesCorrect()) + "");
        dateField3.setText(Math.min(oldItem.getDisplayDate(),
                                    newItem.getDisplayDate()) + "");
        radio3.setSelected(true);
        this.repaint();
    }

    /**
     * @param oldItem
     * @param newItem
     * @return
     */
    private String combineResponses(Item oldItem, Item newItem) {
        String oldResponses = oldItem.getResponse();
        String[] oldArray = oldResponses.split("; *");
        String[] newArray = newItem.getResponse().split("; *");
        for (String newResponse : newArray) {
            if (! arrayContains(oldArray, newResponse)) {
                oldResponses += "; " + newResponse;
            }
        }
        return oldResponses;
    }
    
    private boolean arrayContains(String[] array, String item) {
        for (String s : array) {
            if (s.equals(item)) return true;
        }
        return false;
    }

    private void displayNumberOfConflictsFound(int size) {
        conflictLabel.setText(size + " conflict" + (size == 1 ? "" : "s") + " to fix.");
    }

}
