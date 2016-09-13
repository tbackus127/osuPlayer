
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.WindowEvent;

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

  private final SongPanel songPanel;
  
  /**
   * Width of this frame
   */
  private final int frameWidth;

  /**
   * Height of this frame
   */
  private final int frameHeight;

  /**
   * Default constructor
   */
  public PlayerFrame(int w, int h) {
    super();
    this.frameWidth = w;
    this.frameHeight = h;
    this.songPanel = new SongPanel(this, this.frameWidth, this.frameHeight);
    
    add(this.songPanel);
    revalidate();
    repaint();
  }

  public void closeEverything() {
    System.exit(0);
  }
  
  /**
   * Returns the preferred size of this frame
   * 
   * @return the screen size as a Dimension object
   */
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.frameWidth, this.frameHeight);
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
   * Returns the LayoutManager of this frame
   * 
   * @return null
   */
  @Override
  public LayoutManager getLayout() {
    return null;
  }
}
