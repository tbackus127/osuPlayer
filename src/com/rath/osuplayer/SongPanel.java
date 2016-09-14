
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.sun.org.apache.xpath.internal.operations.And;

/**
 * The main graphics panel. Song backgrounds and info will be rendered here.
 * 
 * @author Administrator
 * 
 */
public class SongPanel extends JPanel {

  /**
   * Serial Version ID (default)
   */
  private static final long serialVersionUID = 1L;

  /**
   * The song background from the beatmap folder
   */
  private Image songBG;

  /**
   * Reference to the child panel (OptionsPanel)
   */
  private OptionsPanel optPanel;

  /**
   * Width of this JPanel (fullscreen)
   */
  private int width;

  /**
   * Height of this JPanel (fullscreen)
   */
  private int height;

  /**
   * Reference to the parent JFrame (PlayerFrame)
   */
  private PlayerFrame parent;

  /**
   * Song metadata with the following indeces: 0: Beatmap directory 1:
   * Background image filename 2: Audio filename 3: Song title 4: Song artist 5:
   * Song source
   */
  private String[] metadata;

  /**
   * Default constructor
   * 
   * @param meta
   *          metadata from the .osu file
   * @param parent
   *          reference to the parent PlayerFrame
   * @param w
   *          the fullscreen width
   * @param h
   *          the fullscreen height
   */
  public SongPanel(PlayerFrame par, int w, int h) {
    super();

    // Set class fields
    this.width = w;
    this.height = h;
    this.parent = par;
    this.metadata = getNewMetadata();

    // Set the background of this panel
    try {
      this.songBG = ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(w, h, Image.SCALE_SMOOTH);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Create and add the options panel
    this.optPanel = new OptionsPanel(this);
    par.add(this.optPanel);
  }

  /**
   * Parses another random beatmap and loads its metadata
   * 
   * @return a String[] with indeces: {directory, BG-image, audio file, title,
   *         artist, source}
   */
  public String[] getNewMetadata() {

    // Find song directory
    File songDir = new File("Songs/");
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

    // Choose random beatmap directory
    Random rand = new Random();
    String currentMapDir = "Songs/" + beatmapFolders[rand.nextInt(beatmapFolders.length)];

    // Parse any .osu file for the background and audio file.
    return MapParser.parseBeatmap(currentMapDir);

  }

  /**
   * Fetches a new song and updates the panel.
   */
  public void newSong() {
    this.metadata = getNewMetadata();
    try {
      this.songBG = ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(this.width, this.height,
          Image.SCALE_SMOOTH);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Swing, why?
    repaint();
    remove(optPanel);
    add(optPanel);
  }

  /**
   * Switches the current audio state from playing to paused, and vice-versa.
   */
  public void togglePause() {
    System.err.println("Pause toggled.");
  }
  
  /**
   * Calls the main JFrame's closeEverything() method
   */
  public void closeEverything() {
    this.parent.closeEverything();
  }

  /**
   * Gets the preferred size of this panel (fullscreen)
   */
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }

  /**
   * Gets the minimum size of this panel (fullscreen)
   */
  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Gets the maximum size of this panel (fullscreen)
   */
  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  /**
   * Renders the panel by painting the background image of the beatmap to the
   * background of this panel, and also paints the song metadata on screen.
   * 
   * @param g
   *          the Graphics object
   */
  @Override
  public void paintComponent(Graphics g) {
    g.drawImage(this.songBG, 0, 0, null);
  }
}
