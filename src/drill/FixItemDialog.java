package drill;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog to allow a Drill user to correct an item.
 * @author David Matuszek
 * @version Mar 26, 2007
 */
public class FixItemDialog extends JDialog {
    private JTextField stimulusField = new JTextField(40);
    private JTextField responseField = new JTextField(40);
    
    private JButton cancelButton = new JButton("Cancel");
    private JButton okButton = new JButton("OK");
    
    transient Item item;
    
    FixItemDialog(final Frame frame, final ItemList itemList, Item itemToBeChanged) {
        super(frame, true);
        item = itemToBeChanged;
        setLayout(new GridLayout(3, 1));
        
        JPanel stimulusPanel = new JPanel();
        stimulusPanel.setLayout(new FlowLayout());
        stimulusPanel.add(stimulusField);
        add(stimulusPanel);
        
        JPanel responsePanel = new JPanel();
        responsePanel.setLayout(new FlowLayout());
        responsePanel.add(responseField);
        add(responsePanel);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(cancelButton, BorderLayout.WEST);
        controlPanel.add(okButton, BorderLayout.EAST);
        add(controlPanel);
        
        stimulusField.setText(item.getStimulus());
        responseField.setText(item.getResponse());
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String stimulus = stimulusField.getText().trim();
                item.setStimulus("CURRENT " + stimulus);
                int stimulusIndex = itemList.searchForStimulus(stimulus);
                if (stimulusIndex < 0) {
                    item.setStimulus(stimulus);
                    String response = responseField.getText().trim();
                    item.setResponse(response);
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(frame,
                        "Cannot change because the stimulus\n" +
                        stimulus +
                        "\nis already in use, with the response\n" +
                        itemList.get(stimulusIndex).getResponse());
                        item.setStimulus(stimulus);                          
                                                  
                }                
            }            
        });
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }            
        });
        pack();
        this.setLocationRelativeTo(frame);
        setVisible(true);
    }
}
