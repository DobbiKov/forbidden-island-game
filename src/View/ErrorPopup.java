package View;

import Errors.MaximumNumberOfPlayersReachedException;
import Helper.AddPlayerCallback;
import Model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ErrorPopup extends JPanel{
    JLabel titleTextLabel;
    JTextArea mainTextArea;

    Runnable onCloseClick;

    public ErrorPopup(String titleText, String mainText){
        super();



        titleTextLabel = new JLabel("Error: " + titleText);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(onCloseClick != null)
                    onCloseClick.run();
            }
        });

        mainTextArea = new JTextArea(titleText);

        this.add(titleTextLabel);
        this.add(mainTextArea);
        this.add(okButton);
        this.setLayout(new GridLayout(3, 1));
    }
    public void addOnCloseClick(Runnable onCloseClick){
        this.onCloseClick = onCloseClick;
    }

    /// Call this static method and provide the window to draw the popup in and two text in order to draw a popup with a notification about an error
    public static void CreateErrorPopup(JFrame window, String titleText, String mainText){
        ErrorPopup panel = new ErrorPopup(titleText, mainText);
        PopupFactory factory = new PopupFactory();
        Popup po = factory.getPopup(window, panel, window.getX() + (window.getSize().width/2), window.getY() + (window.getSize().height/2));
        panel.addOnCloseClick(po::hide);
        po.show();
    }
}
