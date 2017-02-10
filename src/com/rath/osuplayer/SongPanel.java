
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

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

  /** How many bands are in an octave. */
  private static final int NUM_BANDS = 256;

  /** Band vertical scaling. */
  private static final double BAND_SCALE = 2.3D;

  /** Visualization frames per second */
  private static final int TARGET_FRAMERATE = 120;

  /** Color for spectrum foreground. */
  private static final Color COLOR_SPEC_BG = new Color(96, 127, 255, 100);

  /** Color for spectrum background. */
  private static final Color COLOR_SPEC_FG = new Color(255, 255, 255, 220);

  /** Multiplier for foreground spectrum size. */
  private static final double FG_SPEC_MULT = 0.5D;

  /** The song background from the beatmap folder. */
  private BufferedImage songBG;

  /** Reference to the child panel (OptionsPanel). */
  private OptionsPanel optPanel;

  /** The audio player. */
  private AudioPlayer audioPlayer;

  /** Runtime in seconds of the current song. */
  private int songRuntime;

  /** Width of this JPanel (fullscreen). */
  private final int width;

  /** Height of this JPanel (fullscreen). */
  private final int height;

  /** Reference to the parent JFrame (PlayerFrame). */
  private final PlayerFrame parent;

  /** Font for drawing the title font. */
  private Font titleFont;

  /** Font for drawing the artist and source label font. */
  private Font labelFont;

  /** Timer for repainting the window. */
  private final Timer repaintTimer;

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
        parent.repaint();

        // If the song is done, fetch a new one.
        if (!audioPlayer.isPlaying()) {
          newSong();
        }
      }

    });
    this.repaintTimer.setDelay(Math.round(1000 / TARGET_FRAMERATE));

    // Set up minim
    this.minim = new Minim(new MinimHandler());
    final String audioFileStr = this.metadata[0] + "/" + this.metadata[2];
    stripMP3Tags(audioFileStr);
    this.audioPlayer = minim.loadFile(audioFileStr, 2048);
    this.aInput = minim.getLineIn(Minim.STEREO);

    this.songRuntime = this.audioPlayer.length() / 1000;
    System.out.println("Runtime: " + this.songRuntime);

    // Set up FFT calculations
    try {
      this.fft = new FFT(this.aInput.bufferSize(), this.aInput.sampleRate());
    }
    catch (NullPointerException npe) {
      System.err.println("Stereo Mix not enabled!");
      return;
    }

    this.fft.linAverages(NUM_BANDS);
    // this.fft.logAverages(MIN_BANDWIDTH, NUM_BANDS);

    // Set the background of this panel
    try {

      // Convert the BufferedImage that was converted into a non-castable Image
      // back into a BufferedImage
      this.songBG = convertImage(ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));

      // Load and set up fonts
      final File fontFile = new File("res/fonts/JAPANSANS80.OTF");
      this.titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(64f);
      this.labelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(36f);
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
    }

    // Exception handling
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (FontFormatException ffe) {
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
    final File songDir = new File("Songs/");
    if (!songDir.exists() || !songDir.isDirectory()) {
      System.err.println("Songs directory not found!");
    }

    // Get beatmap folder list
    final String[] beatmapFolders = songDir.list(new FilenameFilter() {

      @Override
      public boolean accept(File curr, String name) {
        return new File(curr, name).isDirectory();
      }
    });

    // Choose random beatmap directory
    final Random rand = new Random();
    final String currentMapDir = "Songs/" + beatmapFolders[rand.nextInt(beatmapFolders.length)];

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
    final String filePath = metadata[0] + "/";
    final String audioFileStr = filePath + this.metadata[2];
    stripMP3Tags(audioFileStr);
    this.audioPlayer = minim.loadFile(audioFileStr);

    try {
      this.songBG = convertImage(ImageIO.read(new File(filePath + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));
    }
    catch (IOException e) {
      System.err.println("IOE@" + filePath + metadata[1]);
    }
    catch (NullPointerException npe) {
      System.err.println("NPE@" + filePath + metadata[1]);
    }

    // Start playing again
    this.repaintTimer.start();
    this.audioPlayer.play();

  }

  /**
   * Switches the current audio state from playing to paused, and vice-versa.
   */
  public void togglePause() {
    if (this.audioPlayer.isPlaying()) {

      this.repaintTimer.stop();
      this.audioPlayer.pause();
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

    final Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(this.songBG, 0, 0, null);

    // Song title font calculations
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setFont(this.titleFont);
    final String titleString = this.metadata[3];
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

    // Draw spectrum (top half)
    final GeneralPath gpbg = new GeneralPath();
    final GeneralPath gpfg = new GeneralPath();
    gpbg.moveTo(0, centerY);
    gpfg.moveTo(0, centerY);
    for (int i = 2; i < this.fft.avgSize() - 1; i++) {

      final double lpx = (i - 1) * specWidth;
      final double lpy = centerY - this.fft.getAvg(i - 1) * BAND_SCALE;
      final double lpy2 = centerY - (this.fft.getAvg(i - 1) * BAND_SCALE) * FG_SPEC_MULT;
      final double px = i * specWidth;
      final double py = centerY - this.fft.getAvg(i) * BAND_SCALE;
      final double py2 = centerY - (this.fft.getAvg(i) * BAND_SCALE) * FG_SPEC_MULT;
      final double npx = (i + 1) * specWidth;
      final double npy = centerY - this.fft.getAvg(i + 1) * BAND_SCALE;
      final double npy2 = centerY - (this.fft.getAvg(i + 1) * BAND_SCALE) * FG_SPEC_MULT;
      Point point3 = calcPoint3(lpx, lpy, px, py);
      Point point3b = calcPoint3(lpx, lpy2, px, py2);
      gpbg.lineTo(point3.getX(), point3.getY());
      gpfg.lineTo(point3b.getX(), point3b.getY());
      point3 = calcPoint3(npx, npy, px, py);
      point3b = calcPoint3(npx, npy2, px, py2);
      gpbg.curveTo(px, py, px, py, point3.getX(), point3.getY());
      gpfg.curveTo(px, py2, px, py2, point3b.getX(), point3b.getY());

    }

    // Bottom half
    for (int i = this.fft.avgSize() - 2; i >= 2; i--) {

      final double lpx = (i + 1) * specWidth;
      final double lpy = centerY + this.fft.getAvg(i + 1) * BAND_SCALE + 2;
      final double lpy2 = centerY + (this.fft.getAvg(i + 1) * BAND_SCALE + 4) * FG_SPEC_MULT;
      final double px = i * specWidth;
      final double py = centerY + this.fft.getAvg(i) * BAND_SCALE + 2;
      final double py2 = centerY + (this.fft.getAvg(i) * BAND_SCALE + 4) * FG_SPEC_MULT;
      final double npx = (i - 1) * specWidth;
      final double npy = centerY + this.fft.getAvg(i - 1) * BAND_SCALE + 2;
      final double npy2 = centerY + (this.fft.getAvg(i - 1) * BAND_SCALE + 4) * FG_SPEC_MULT;
      Point point3 = calcPoint3(lpx, lpy, px, py);
      Point point3b = calcPoint3(lpx, lpy2, px, py2);
      gpbg.lineTo(point3.getX(), point3.getY());
      gpfg.lineTo(point3b.getX(), point3b.getY());
      point3 = calcPoint3(npx, npy, px, py);
      point3b = calcPoint3(npx, npy2, px, py2);
      gpbg.curveTo(px, py, px, py, point3.getX(), point3.getY());
      gpfg.curveTo(px, py2, px, py2, point3b.getX(), point3b.getY());
    }

    gpbg.lineTo(0, centerY);
    g2.setColor(COLOR_SPEC_BG);
    g2.fill(gpbg);
    g2.setColor(COLOR_SPEC_FG);
    g2.fill(gpfg);
  }

  /**
   * Converts an Image into a BufferedImage when casting does not work.
   * 
   * @param img the Image to convert to a BufferedImage.
   * @return the BufferedImage that was converted.
   */
  private BufferedImage convertImage(Image img) {
    final BufferedImage result = new BufferedImage(img.getWidth(null), img.getHeight(null),
        BufferedImage.TYPE_INT_ARGB);
    result.getGraphics().drawImage(img, 0, 0, null);
    return result;
  }

  /**
   * Strips an .mp3 file of its tags (Minim doesn't like them).
   * 
   * @param s relative path to the .mp3 file as a String.
   */
  private static final void stripMP3Tags(final String s) {

    System.out.println("Stripping " + s);
    final String newFileName = s.substring(0, s.length() - 4) + "0.mp3";
    try {

      // Strip tags if they exist
      final Mp3File mf = new Mp3File(s);

      // If the mp3 is already stripped, do nothing
      if (!mf.hasId3v1Tag() && !mf.hasId3v2Tag() && !mf.hasCustomTag()) return;

      // Strip tags if they exist
      if (mf.hasId3v1Tag()) mf.removeId3v1Tag();
      if (mf.hasId3v2Tag()) mf.removeId3v2Tag();
      if (mf.hasCustomTag()) mf.removeCustomTag();

      // Save temp file
      mf.save(newFileName);

      // Delete original and rename temp
      final File originalMp3 = new File(s);
      originalMp3.delete();
      final File strippedMp3 = new File(newFileName);
      strippedMp3.renameTo(originalMp3);

    }
    catch (UnsupportedTagException e) {
      System.err.println("Unsupported tag!\n" + e.getMessage());
    }
    catch (InvalidDataException e) {
      System.err.println("Mp3 file is corrupt!");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (NotSupportedException e) {
      e.printStackTrace();
    }
  }

  private static final Point calcPoint3(final double x1, final double y1, final double x2, final double y2) {
    final double arcSize = 10;
    final double d1 = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    final double per = arcSize / d1;
    final double dx = (x1 - x2) * per;
    final double dy = (y1 - y2) * per;
    return new Point((int) (x2 + dx), (int) (y2 + dy));
  }
}
