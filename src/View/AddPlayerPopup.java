package View;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class AddPlayerPopup extends JPanel implements ActionListener {


    public AddPlayerPopup() {
        super();

        JLabel l = new JLabel("Add Player");
        JButton b = new JButton("Add");
        this.add(l);
        this.add(b);
        this.setLayout(new GridLayout(2, 1));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
