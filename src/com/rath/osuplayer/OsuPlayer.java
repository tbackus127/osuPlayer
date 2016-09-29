
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * @author Tim Backus tbackus127@gmail.com
 */
public class OsuPlayer {
  
  /**
   * Main method
   * 
   * @param args Runtime arguments
   */
  public static void main(String[] args) {
    
    // Get dimensions of the screen
    Dimension fsDim = Toolkit.getDefaultToolkit().getScreenSize();
    final int fsWidth = fsDim.width;
    final int fsHeight = fsDim.height;
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        PlayerFrame frame = new PlayerFrame(fsWidth, fsHeight);
        frame.setUndecorated(true);
        frame.setVisible(true);
      }
    });
    
  }
  
}
