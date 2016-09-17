
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
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

  /**
   * Reference to the child panel (OptionsPanel)
   */
  private OptionsPanel optPanel;
  
  /**
   * The audio player
   */
  private AudioPlayer audioPlayer;

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
   * Font for drawing the title font
   */
  private Font titleFont;

  /**
   * Font for drawing the artist and source label font
   */
  private Font labelFont;

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
    try {
      this.audioPlayer = new AudioPlayer(this.metadata[0] + "/" + this.metadata[2]);
    }
    catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }

    // Set the background of this panel
    try {
      this.songBG = ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(this.width, this.height,
          Image.SCALE_SMOOTH);
      File fontFile = new File("res/fonts/YANONEKAFFEESATZ-REGULAR.TTF");
      this.titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(64f);
      this.labelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(36f);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (FontFormatException ffe) {
      ffe.printStackTrace();
    }
    
    

    // Create and add the options panel
    this.optPanel = new OptionsPanel(this);
    par.add(this.optPanel);
    this.audioPlayer.play();
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
    String filePath = metadata[0] + "/";
    try {
      this.songBG = ImageIO.read(new File(filePath + metadata[1])).getScaledInstance(this.width, this.height,
          Image.SCALE_SMOOTH);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Swing, why?
    repaint();
    remove(optPanel);
    add(optPanel);
    this.audioPlayer.setFile(new File(filePath + metadata[2]));
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
    Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(this.songBG, 0, 0, null);

    // Song title font calculations
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setFont(this.titleFont);
    String titleString = this.metadata[3];
    int fx = this.optPanel.getWidth() + (this.width >> 5);
    int fy = this.height - this.optPanel.getHeight() - (this.height >> 5);

    // Draw shadow
    g2.setColor(Color.BLACK);
    g2.drawString(titleString, fx + 2, fy + 2);

    // Draw title
    g2.setColor(Color.WHITE);
    g2.drawString(titleString, fx, fy);

    // Artist font calculations
    g2.setFont(this.labelFont);
    String artistString = "Artist: " + this.metadata[4];
    fx += (this.width >> 5);
    fy += (this.height / 20);

    // Draw Shadow and artist name
    g2.setColor(Color.BLACK);
    g2.drawString(artistString, fx + 2, fy + 2);
    g2.setColor(Color.WHITE);
    g2.drawString(artistString, fx, fy);

    // Calculate and draw source name
    String sourceString = "Source: " + this.metadata[5];
    fy += (this.height / 20);
    g2.setColor(Color.BLACK);
    g2.drawString(sourceString, fx + 2, fy + 2);
    g2.setColor(Color.WHITE);
    g2.drawString(sourceString, fx, fy);
  }
}
