
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

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
  private OptionsPanel optPanel;

  private int width;
  private int height;
  private PlayerFrame parent;
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
    this.width = w;
    this.height = h;
    this.parent = par;
    this.metadata = getNewMetadata();
    try {
      this.songBG = ImageIO.read(new File(metadata[0] + "/" + metadata[1]))
          .getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    this.optPanel = new OptionsPanel(this);
    par.add(this.optPanel);
    par.revalidate();
    par.repaint();
  }

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
    String currentMapDir = "Songs/"
        + beatmapFolders[rand.nextInt(beatmapFolders.length)];

    // Parse any .osu file for the background and audio file.
    return MapParser.parseBeatmap(currentMapDir);

  }

  /**
   * Fetches a new song and updates the panel.
   */
  public void newSong() {
    System.err.println("Called new");
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  public void closeEverything() {
    this.parent.closeEverything();
  }

  /**
   * Renders the panel
   * 
   * @param g
   *          the Graphics object
   */
  @Override
  public void paintComponent(Graphics g) {
    g.drawImage(this.songBG, 0, 0, null);
  }
}
