package View;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.*;
import Helper.Callback;

public class AddPlayerPopup extends JPanel implements ActionListener {
    JTextField nameField;

    Callback cancelButtonCallback;

    public AddPlayerPopup() {
        super();

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2));


        JLabel l = new JLabel("Add Player");
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cancelButtonCallback != null)
                    cancelButtonCallback.call();
            }
        });
        JButton addButton = new JButton("Add");

        buttonsPanel.add(addButton);
        buttonsPanel.add(cancelButton);

        nameField = new JTextField("name",15);
        nameField.requestFocusInWindow();
        nameField.setEditable(true);
        this.add(l);
        this.add(nameField);
        this.add(buttonsPanel);
        this.setLayout(new GridLayout(3, 1));

    }
    public void addCancelCallback(Callback c){
        this.cancelButtonCallback = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
