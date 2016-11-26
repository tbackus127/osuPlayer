
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import ddf.minim.AudioInput;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;


/**
 * The main graphics panel. Song backgrounds and info will be rendered here.
 * 
 * @author Administrator
 * 
 */
public class SongPanel extends JPanel {
  
  /** Serial version UID. */
  private static final long serialVersionUID = 1L;
  
  /** The minimum hertz required for a band. */
  private static final int MIN_BANDWIDTH = 440;
  
  /** How many bands are in an octave. */
  private static final int BANDS_PER_OCTAVE = 32;
  
  /** Band vertical scaling. */
  private static final int BAND_SCALE = 10;
  
  /** Visualization frames per second */
  private static final int TARGET_FRAMERATE = 60;
  
  /** Color for spectrum foreground. */
  private static final Color COLOR_SPEC_FG = new Color(96, 127, 255, 100);
  
  /** Color for spectrum background. */
  private static final Color COLOR_SPEC_BG = new Color(255, 255, 255, 220);
  
  /** The song background from the beatmap folder. */
  private BufferedImage songBG;
  
  /** Reference to the child panel (OptionsPanel). */
  private OptionsPanel optPanel;
  
  /** The audio player. */
  private AudioPlayer audioPlayer;
  
  /** Width of this JPanel (fullscreen). */
  private int width;
  
  /** Height of this JPanel (fullscreen). */
  private int height;
  
  /** Reference to the parent JFrame (PlayerFrame). */
  private PlayerFrame parent;
  
  /** Font for drawing the title font. */
  private Font titleFont;
  
  /** Font for drawing the artist and source label font. */
  private Font labelFont;
  
  /** Timer for repainting the window. */
  private Timer repaintTimer;
  
  /**
   * Song metadata with the following indeces: 0: Beatmap directory 1: Background image filename 2: Audio filename 3:
   * Song title 4: Song artist 5: Song source
   */
  private String[] metadata;
  
  /** The Minim library object. */
  private final Minim minim;
  
  /** Audio input data for Minim. */
  private final AudioInput aInput;
  
  /** Fast Fourier Transform object. */
  private FFT fft;
  
  /**
   * Default constructor.
   * 
   * @param meta metadata from the .osu file
   * @param parent reference to the parent PlayerFrame
   * @param w the fullscreen width
   * @param h the fullscreen height
   */
  public SongPanel(PlayerFrame par, int w, int h) {
    super();
    
    // Set class fields
    this.width = w;
    this.height = h;
    this.parent = par;
    this.metadata = getNewMetadata();
    System.err.println(this.metadata[0] + "/" + this.metadata[2]);
    
    // Timer to update visualization
    this.repaintTimer = new Timer(0, new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent evt) {
        parent.revalidate();
        parent.repaint();
      }
      
    });
    this.repaintTimer.setDelay(Math.round(1000 / TARGET_FRAMERATE));
    
    // Set up minim
    this.minim = new Minim(new MinimHandler());
    this.audioPlayer = minim.loadFile(this.metadata[0] + "/" + this.metadata[2], 512);
    this.aInput = minim.getLineIn(Minim.STEREO);
    
    // Set up FFT calculations
    try {
      this.fft = new FFT(this.aInput.bufferSize(), this.aInput.sampleRate());      
    } catch (NullPointerException npe) {
      System.err.println("Stereo Mix not enabled!");
      return;
    }
    this.fft.logAverages(MIN_BANDWIDTH, BANDS_PER_OCTAVE);
    
    // Set the background of this panel
    try {
      
      // Convert the BufferedImage that was converted into a non-castable Image
      // back into a BufferedImage
      this.songBG = convertImage(ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));
      
      // Load and set up fonts
      File fontFile = new File("res/fonts/JAPANSANS80.OTF");
      this.titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(64f);
      this.labelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(36f);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
    }
    
    // Exception handling
    catch (IOException e) {
      e.printStackTrace();
    } catch (FontFormatException ffe) {
      ffe.printStackTrace();
    }
    
    // Create and add the options panel
    this.optPanel = new OptionsPanel(this);
    par.add(this.optPanel);
    
    // Start everything
    this.repaintTimer.start();
    
    this.audioPlayer.play();
  }
  
  /**
   * Parses another random beatmap and loads its metadata.
   * 
   * @return a String[] with indeces: {directory, BG-image, audio file, title, artist, source}
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
    
    this.audioPlayer.close();
    this.repaintTimer.stop();
    
    this.metadata = getNewMetadata();
    String filePath = metadata[0] + "/";
    this.audioPlayer = minim.loadFile(filePath + this.metadata[2]);
    
    try {
      this.songBG = convertImage(ImageIO.read(new File(filePath + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));
    } catch (IOException e) {
      System.err.println("IOE@" + filePath + metadata[1]);
    } catch (NullPointerException npe) {
      System.err.println("NPE@" + filePath + metadata[1]);
    }
    
    repaint();
    
    // Start playing again
    // TODO: Fix mp3's with 0 lead-in time playing too late.
    this.repaintTimer.start();
    this.audioPlayer.play();
    
  }
  
  /**
   * Switches the current audio state from playing to paused, and vice-versa.
   */
  public void togglePause() {
    System.err.println("Pause toggled.");
    if (this.audioPlayer.isPlaying()) {
      
      this.audioPlayer.pause();
      this.repaintTimer.stop();
    } else {
      this.repaintTimer.start();
      this.audioPlayer.play();
    }
  }
  
  /**
   * Checks if the audio is currently paused (used for control rendering).
   * 
   * @return true if audio is paused; false otherwise.
   */
  public boolean isPaused() {
    return !this.audioPlayer.isPlaying();
  }
  
  /**
   * Calls the main JFrame's closeEverything() method
   */
  public void closeEverything() {
    this.audioPlayer.close();
    this.minim.stop();
    this.repaintTimer.stop();
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
   * Renders the panel by painting the background image of the beatmap to the background of this panel, and also paints
   * the song metadata on screen.
   * 
   * @param g the Graphics object
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
    if (this.metadata[5].length() > 0) {
      String sourceString = "Source: " + this.metadata[5];
      fy += (this.height / 20);
      g2.setColor(Color.BLACK);
      g2.drawString(sourceString, fx + 2, fy + 2);
      g2.setColor(Color.WHITE);
      g2.drawString(sourceString, fx, fy);
    }
    
    // Draw spectrum center line
    final int centerY = this.height >> 1;
    final double specWidth = (double) ((float) this.width / (this.fft.getBandWidth() * 4.0));
    g2.drawLine(0, centerY, this.width, centerY);
    
    // Get FFT data
    this.fft.forward(this.aInput.mix);
    
    // Draw spectrum
    int lastBandHeight = (int) (this.fft.getAvg(0) * BAND_SCALE);
    for (int i = 1; i < this.fft.avgSize(); i++) {
      final int bandHeight = (int) (this.fft.getAvg(i) * BAND_SCALE);
      
      final int xPointA = (int) (specWidth * (i - 1));
      final int xPointB = (int) (specWidth * i);
      
      // Background spectrum
      final Polygon bandSmooth = new Polygon();
      bandSmooth.addPoint(xPointA, centerY - lastBandHeight);
      bandSmooth.addPoint(xPointB, centerY - bandHeight);
      bandSmooth.addPoint(xPointB, centerY + bandHeight + 1);
      bandSmooth.addPoint(xPointA, centerY + lastBandHeight + 1);
      g2.setColor(COLOR_SPEC_FG);
      g2.fill(bandSmooth);
      
      
      final Polygon bandSmoothOverlay = new Polygon();
      bandSmoothOverlay.addPoint(xPointA, centerY - lastBandHeight / 2);
      bandSmoothOverlay.addPoint(xPointB, centerY - bandHeight / 2);
      bandSmoothOverlay.addPoint(xPointB, centerY + (bandHeight + 1) / 2);
      bandSmoothOverlay.addPoint(xPointA, centerY + (lastBandHeight + 1) / 2);
      g2.setColor(COLOR_SPEC_BG);
      g2.fill(bandSmoothOverlay);
      
      lastBandHeight = bandHeight;
    }
  }
  
  /**
   * Converts an Image into a BufferedImage when casting does not work.
   * 
   * @param img the Image to convert to a BufferedImage.
   * @return the BufferedImage that was converted.
   */
  private BufferedImage convertImage(Image img) {
    BufferedImage result = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    result.getGraphics().drawImage(img, 0, 0, null);
    return result;
  }
}
