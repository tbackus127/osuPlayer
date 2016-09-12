
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFrame;

/**
 * @author Tim Backus tbackus127@gmail.com
 */
public class OsuPlayer {

  /**
   * Main method
   * 
   * @param args
   *          Runtime arguments
   */
  public static void main(String[] args) {

    // Find song directory
    File songDir = new File("Songs/");
    System.err.println(songDir.getAbsolutePath());
    if (!songDir.exists() || !songDir.isDirectory()) {
      System.err.println("Songs directory not found!");
    }

    // Get beatmap folder list
    String[] beatmapFolders = songDir.list(new FilenameFilter() {

      @Override
      public boolean accept(File curr, String name) {
        return new File(curr, name).isDirectory();
      }
    });

    // Get screen dimensions
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    JFrame frame = new JFrame();

    // Create window and display it
    frame.setSize(dim);
    frame.setPreferredSize(dim);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    frame.setUndecorated(true);
    frame.setLayout(null);

    frame.setVisible(true);
  }

}
