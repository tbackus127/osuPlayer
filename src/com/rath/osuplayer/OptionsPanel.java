
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
  private Image playImgHover;
  private Image pauseImg;
  private Image pauseImgHover;
  private Image newSongImg;
  private Image newSongImgHover;
  private Image closeImg;
  private Image closeImgHover;

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
  public OptionsPanel(final SongPanel par) {
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
      this.playImgHover = ImageIO.read(new File("res/img/play-hover.png"));
      this.pauseImgHover = ImageIO.read(new File("res/img/pause-hover.png"));
      this.newSongImgHover = ImageIO.read(new File("res/img/refresh-hover.png"));
      this.closeImgHover = ImageIO.read(new File("res/img/close-hover.png"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    // Control button setup
    // Pause button
    this.playPauseButton = new JButton(new ImageIcon(this.pauseImg));
    this.playPauseButton.setBorder(BorderFactory.createEmptyBorder());
    this.playPauseButton.setContentAreaFilled(false);
    this.playPauseButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        if (parent.isPaused()) {
          playPauseButton.setIcon(new ImageIcon(pauseImgHover));
        } else {
          playPauseButton.setIcon(new ImageIcon(playImgHover));
        }
        parent.togglePause();
      }
    });
    this.playPauseButton.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent evt) {
        if (parent.isPaused()) {
          playPauseButton.setIcon(new ImageIcon(playImgHover));
        } else {
          playPauseButton.setIcon(new ImageIcon(pauseImgHover));
        }
      }

      @Override
      public void mouseExited(MouseEvent evt) {
        if (parent.isPaused()) {
          playPauseButton.setIcon(new ImageIcon(playImg));
        } else {
          playPauseButton.setIcon(new ImageIcon(pauseImg));
        }
      }

    });

    // New song button
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
    this.newSongButton.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent evt) {
        newSongButton.setIcon(new ImageIcon(newSongImgHover));
      }

      @Override
      public void mouseExited(MouseEvent evt) {
        newSongButton.setIcon(new ImageIcon(newSongImg));
      }

    });

    // Close button
    this.closeButton = new JButton(new ImageIcon(this.closeImg));
    this.closeButton.setBorder(BorderFactory.createEmptyBorder());
    this.closeButton.setContentAreaFilled(false);
    this.closeButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        closeEverything();
      }
    });
    this.closeButton.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent evt) {
        closeButton.setIcon(new ImageIcon(closeImgHover));
      }

      @Override
      public void mouseExited(MouseEvent evt) {
        closeButton.setIcon(new ImageIcon(closeImg));
      }

    });

    // Add control buttons
    add(this.playPauseButton);
    add(this.newSongButton);
    add(this.closeButton);

  }

  /**
   * Closes everything.
   */
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
