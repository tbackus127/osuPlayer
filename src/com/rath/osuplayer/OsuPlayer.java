
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class OsuPlayer {

  public static void main(String[] args) {

    // Get screen dimensions
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    JFrame frame = new JFrame();

    frame.setSize(dim);
    frame.setPreferredSize(dim);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    frame.setUndecorated(true);
    frame.setLayout(null);

    frame.setVisible(true);
  }

}
