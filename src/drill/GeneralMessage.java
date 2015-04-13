package drill;
import java.awt.BorderLayout;
import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Creates and displays a very general modal dialog.
 * 
 * @author David Matuszek
 * @version Feb 17, 2007
 */
public class GeneralMessage extends JDialog implements ActionListener {
    /** The number of the choice made, or -1 if no choice. */
    public int choice = -1;
    private JButton[] buttons;
    
    /**
     * Constructs a modal JDialog with the given title and message, and a button for
     * each remaining argument. Clicking a button sets the public variable
     * <code>choice</code> equal to the numerical position of that item (starting
     * from zero), and this variable can be accessed after this dialog closes.
     * A value of -1 indicates that the dialog was closed without clicking a button.
     * @param owner The enclosing JFrame.
     * @param title The message to put in the title bar.
     * @param message The message to put in the main body of the dialog.
     * @param buttonLabels A label for each button choice.
     */
    public GeneralMessage(Frame owner, String title, String message, String... buttonLabels) {
        super(owner, title, true);
        buttons = new JButton[buttonLabels.length];
        setLayout(new BorderLayout());
        
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        add(messageArea, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].addActionListener(this);
            buttonPanel.add(buttons[i], buttonPanel);
        }
        add(buttonPanel, BorderLayout.SOUTH);

        add(new JLabel(" "), BorderLayout.WEST);
        add(new JLabel(" "), BorderLayout.EAST);
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < buttons.length; i++) {
            if (e.getSource() == buttons[i]) {
                choice = i;
                break;
            }
        }
        setVisible(false);
    }
    
    /**
     * Test method.
     * @param args Not used.
     */
    public static void main(String[] args) {
        GeneralMessage m = null;
        for (int i = 0; i < 4; i++) {
            m = new GeneralMessage(null, "Title goes here",
                                   "Do you like apples?\n"
                                   + "Well, how do you like THEM apples?",
                                   "Zero", "One", "Two", "Screw it!");
            System.out.println("m.choice = " + m.choice);
        }
        m.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
