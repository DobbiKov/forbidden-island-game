package View;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import Helper.Callback;

public class AddPlayerPopup extends JPanel implements ActionListener {
    JTextField nameField;

    Callback mainCallback;

    public AddPlayerPopup() {
        super();

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2));


        JLabel l = new JLabel("Add Player");
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(mainCallback != null)
                    mainCallback.callHide();
            }
        });
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(nameField.getText().equals("")){
                    return;
                }
                mainCallback.callAddPlayer(nameField.getText());
                mainCallback.callHide();
            }
        });

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
    public void addMainCallback(Callback c){
        this.mainCallback = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
