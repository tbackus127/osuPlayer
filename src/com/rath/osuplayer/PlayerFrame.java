
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 * This class controls the main frame of the music player (borderless,
 * fullscreen)
 * 
 * @author Administrator
 * 
 */
public class PlayerFrame extends JFrame {

  /**
   * Serial version (default)
   */
  private static final long serialVersionUID = 1L;

  /**
   * Default constructor
   */
  public PlayerFrame() {
    super();
  }

  /**
   * Returns the preferred size of this frame
   * 
   * @return the screen size as a Dimension object
   */
  @Override
  public Dimension getPreferredSize() {
    return Toolkit.getDefaultToolkit().getScreenSize();
  }

  /**
   * Returns the size of this frame
   * 
   * @return the screen size as a Dimension object
   */
  @Override
  public Dimension getSize() {
    return getPreferredSize();
  }

  /**
   * Gets the default operation of the close button
   * 
   * @return the exit operation
   */
  @Override
  public int getDefaultCloseOperation() {
    return JFrame.EXIT_ON_CLOSE;
  }

  /**
   * Returns the initial state of the frame
   * 
   * @return maximized by default
   */
  @Override
  public int getExtendedState() {
    return Frame.MAXIMIZED_BOTH;
  }

  /**
   * Returns whether or not the frame will have a top bar and border
   * 
   * @return true -- undecorated
   */
  @Override
  public boolean isUndecorated() {
    return true;
  }

  /**
   * Returns the LayoutManager of this frame
   * 
   * @return null
   */
  @Override
  public LayoutManager getLayout() {
    return null;
  }
}
