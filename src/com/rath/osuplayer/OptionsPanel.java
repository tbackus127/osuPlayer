
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OptionsPanel extends JPanel {
  
  /**
   * The parent JPanel, the SongPanel
   */
  private SongPanel parent;
  
  /**
   * Width of the panel
   */
  private int width;
  
  /**
   * Height of the panel
   */
  private int height;
  
  private Image playImg;
  private Image pauseImg;
  private Image newSongImg;
  private Image closeImg;
  
  private JButton playPauseButton;
  private JButton newSongButton;
  private JButton closeButton;
  
  /**
   * Default serial version ID
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Default constructor
   * 
   * @param par the parent JPanel (SongPanel)
   */
  public OptionsPanel(SongPanel par) {
    super();
    this.parent = par;
    
    // Calculation variables
    final Dimension pdim = this.parent.getPreferredSize();
    final int pw = pdim.width;
    final int ph = pdim.height;
    this.width = (pw >> 2) - (pw / 24);
    this.height = (ph >> 3) - (ph / 60);
    
    // Set size and transparency
    setBounds(new Rectangle(0, ph - this.height, this.width, this.height));
    setOpaque(false);
    setBackground(new Color(0, 0, 0, 0));
    setLayout(new FlowLayout());
    
    // Load images
    try {
      this.playImg = ImageIO.read(new File("res/img/play.png"));
      this.pauseImg = ImageIO.read(new File("res/img/pause.png"));
      this.newSongImg = ImageIO.read(new File("res/img/refresh.png"));
      this.closeImg = ImageIO.read(new File("res/img/close.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // Control button setup
    this.playPauseButton = new JButton(new ImageIcon(this.pauseImg));
    this.playPauseButton.setBorder(BorderFactory.createEmptyBorder());
    this.playPauseButton.setContentAreaFilled(false);
    this.playPauseButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent evt) {
        if(parent.isPaused()) {
          playPauseButton.setIcon(new ImageIcon(pauseImg));
        } else {
          playPauseButton.setIcon(new ImageIcon(playImg));
        }
        parent.togglePause();
      }
    });
    
    this.newSongButton = new JButton(new ImageIcon(this.newSongImg));
    this.newSongButton.setBorder(BorderFactory.createEmptyBorder());
    this.newSongButton.setContentAreaFilled(false);
    this.newSongButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent evt) {
        parent.newSong();
        playPauseButton.setIcon(new ImageIcon(pauseImg));
      }
    });
    
    this.closeButton = new JButton(new ImageIcon(this.closeImg));
    this.closeButton.setBorder(BorderFactory.createEmptyBorder());
    this.closeButton.setContentAreaFilled(false);
    this.closeButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent evt) {
        closeEverything();
      }
    });
    
    // Add control buttons
    add(this.playPauseButton);
    add(this.newSongButton);
    add(this.closeButton);
    
  }
  
  public void closeEverything() {
    this.parent.closeEverything();
  }
  
  /**
   * Gets the preferred size of the panel.
   * 
   * @return a Dimension with half the width and 1/4 the height of the screen.
   */
  @Override
  public Dimension getPreferredSize() {
    System.err.println("OptPanel: w=" + this.width + ", h=" + this.height);
    return new Dimension(this.width, this.height);
  }
  
  /**
   * Gets the minimum size of the panel
   * 
   * @return a Dimension with half the width and 1/4 the height of the screen.
   */
  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }
  
  /**
   * Gets the maximum size of the panel
   * 
   * @return a Dimension with half the width and 1/4 the height of the screen.
   */
  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }
}
