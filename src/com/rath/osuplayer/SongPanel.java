
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
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
import java.util.ArrayDeque;
import java.util.Queue;
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

  /**
   * Enables debug mode (println()'s. println()'s everywhere).
   */
  private static final boolean DEBUG_MODE = false;

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /** The amount of padding info drawn should have. */
  private static final int PLAYER_PADDING = 16;

  /** The amount of spacing relative to window height between lines of a song's information. */
  private static final double SONG_INFO_SPACING_Y = 0.05D;

  /** Song metadata indentation amount. */
  private static final double SONG_INFO_INDENT_X = 0.03125D;

  /** The relative vertical position of the info background. */
  private static final double INFO_BG_Y = 0.78D;

  /** Horizontal position of the song timer. */
  private static final double PLAYTIME_X = 0.035D;

  /** Vertical position of the song timer. */
  private static final double PLAYTIME_Y = 0.87D;

  /** Multiplier for horizontal progress bar positioning. */
  private static final double PROGRESS_X_MULT = 2.1D;

  /** Horizontal progress bar positioning. */
  private static final double PROGRESS_X = PLAYTIME_X * PROGRESS_X_MULT;

  /** Progress bar length. */
  private static final double PROGRESS_LEN = 0.1D;

  /** The height of the progress bar inducator line. */
  private static final int PROG_LINE_HEIGHT = 12;

  /** How many recently played songs to keep track of. */
  private static final int QUEUE_THRESHOLD = 40;

  /** How many times the system tries to pick a song that was not recently played before giving up. */
  private static final int RECENT_RETRY_THRESHOLD = 20;

  /** How many bands are in an octave. */
  private static final int NUM_BANDS = 256;

  /** Band vertical scaling. */
  private static final double BAND_SCALE = 2.3D;

  /** Visualization frames per second. */
  private static final int TARGET_FRAMERATE = 120;

  /** The opacity of the song info background. */
  private static final int INFO_BG_OPACITY = 160;

  /** Color for spectrum foreground. */
  private static final Color COLOR_SPEC_BG = new Color(96, 127, 255, 120);

  /** Color for spectrum background. */
  private static final Color COLOR_SPEC_FG = new Color(255, 255, 255, 180);

  /** Color of the song info background. */
  private static final Color COLOR_INFO_BG = new Color(0, 0, 0, INFO_BG_OPACITY);

  /** Multiplier for foreground spectrum size. */
  private static final double FG_SPEC_MULT = 0.5D;

  /** Default song title font size. */
  private static final float DEFAULT_TITLE_FONT_SIZE = 64.0F;

  /** Default song metadata font size. */
  private static final float DEFAULT_LABEL_FONT_SIZE = 28.0F;

  /** Horizontal offset in pixels for font shadow. */
  private static final int FONT_SHADOW_OFFSET_X = 2;

  /** Vertical offset in pixels for font shadow. */
  private static final int FONT_SHADOW_OFFSET_Y = 2;

  /** Path to the default font. */
  private static final String DEFAULT_FONT_STRING = "res/fonts/JAPANSANS80.OTF";

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

  /** Handle to the last played audio file (tags stripped). */
  private File lastAudioFile;

  /** A set of the most recently played songs. */
  private Queue<String> recentlyPlayedSongs;

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

    this.recentlyPlayedSongs = new ArrayDeque<String>();

    // Set class fields
    this.width = w;
    this.height = h;
    this.parent = par;
    this.metadata = getNewMetadata();

    // Add song to recently played queue
    this.recentlyPlayedSongs.add(this.metadata[3]);
    debugOut("Added \"" + this.metadata[3] + "\" to recently played.");

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
    String audioFileStr = this.metadata[0] + "/" + this.metadata[2];
    debugOut("Chose audio file: \"" + audioFileStr + "\".");
    audioFileStr = stripMP3Tags(audioFileStr);
    debugOut("Loaded stripped audio file: \"" + audioFileStr + "\".");
    this.audioPlayer = minim.loadFile(audioFileStr, 2048);
    this.lastAudioFile = new File(audioFileStr);
    this.aInput = minim.getLineIn(Minim.STEREO);

    // Get audio runtime
    this.songRuntime = this.audioPlayer.length() / 1000;
    debugOut("Runtime: " + this.songRuntime);

    // Set up FFT calculations
    try {
      this.fft = new FFT(this.aInput.bufferSize(), this.aInput.sampleRate());
    }
    catch (NullPointerException npe) {
      System.err.println("Stereo Mix not enabled!");
      return;
    }
    this.fft.linAverages(NUM_BANDS);

    // Set the background of this panel
    try {
      this.songBG = convertImage(ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));

      // Load and set up fonts
      final File fontFile = new File(DEFAULT_FONT_STRING);
      this.titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(DEFAULT_TITLE_FONT_SIZE);
      this.labelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(DEFAULT_LABEL_FONT_SIZE);
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));

    }
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

    debugOut("Fetching new metadata.");

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

    debugOut("Chose \"" + currentMapDir + "\" as next song.");

    // Parse any .osu file for the background and audio file.
    return MapParser.parseBeatmap(currentMapDir);
  }

  /**
   * Fetches a new song and updates the panel.
   */
  public void newSong() {

    // Stop updating and playing
    this.repaintTimer.stop();
    this.audioPlayer.close();

    // Delete copied and stripped mp3
    this.lastAudioFile.delete();

    debugOut("Recently played songs queue:");
    debugOut(this.recentlyPlayedSongs.toString());

    // Try to play new songs that haven't played in a while
    int recentSongCount = 0;
    while (recentSongCount <= RECENT_RETRY_THRESHOLD) {
      this.metadata = getNewMetadata();
      if (!this.recentlyPlayedSongs.contains(this.metadata[3])) {
        break;
      }
      debugOut("Chose recently played song. Retrying " + (RECENT_RETRY_THRESHOLD - recentSongCount) + " more times.");
      recentSongCount++;
    }

    // Add song to recently played
    this.recentlyPlayedSongs.add(metadata[3]);
    if (this.recentlyPlayedSongs.size() > QUEUE_THRESHOLD) {
      debugOut("Recently played queue reached size threshold. Removing oldest song.");
      this.recentlyPlayedSongs.remove();
    }

    // Load new audio file
    final String filePath = metadata[0] + "/";
    String audioFileStr = filePath + this.metadata[2];
    audioFileStr = stripMP3Tags(audioFileStr);
    debugOut("Loading audio \"" + audioFileStr + "\".");
    this.audioPlayer = minim.loadFile(audioFileStr);
    this.lastAudioFile = new File(audioFileStr);

    // Get and scale new background image
    try {
      this.songBG = convertImage(ImageIO.read(new File(filePath + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));
    }
    catch (IOException e) {
      System.err.println("IOE@" + filePath + metadata[1]);
      this.songBG = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_BINARY);
    }
    catch (NullPointerException npe) {
      System.err.println("NPE@" + filePath + metadata[1]);
    }

    // Start playing again
    this.audioPlayer.play();
    this.repaintTimer.start();

  }

  /**
   * Switches the current audio state from playing to paused, and vice-versa.
   */
  public void togglePause() {
    if (this.audioPlayer.isPlaying()) {
      this.repaintTimer.stop();
      this.audioPlayer.pause();
      this.parent.repaint();
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
    this.minim.dispose();
    this.lastAudioFile.delete();
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

    if (this.songBG != null) {
      g2.drawImage(this.songBG, 0, 0, null);
    }
    final int centerY = this.height >> 1;

    final int infoBGStart = (int) (this.height * INFO_BG_Y);

    // Draw song info background
    g2.setColor(COLOR_INFO_BG);
    g2.fillRect(0, infoBGStart, this.width, centerY >> 1);

    // Song title font calculations
    final String titleString = this.metadata[3];
    int fontx = this.optPanel.getWidth();
    int fonty = this.height - this.optPanel.getHeight() - PLAYER_PADDING;

    g2.setFont(this.titleFont);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    final double fntWidth = g2.getFontMetrics(this.titleFont).stringWidth(titleString);

    // Scale font if the song title is too long.
    final double maxFontWidth = this.width - fontx - PLAYER_PADDING;
    if (fntWidth > maxFontWidth) {
      final float fntSize = (float) (((maxFontWidth) / fntWidth) * this.titleFont.getSize());
      final Font tFont = this.titleFont.deriveFont(fntSize);
      g2.setFont(tFont);
    }

    // Draw shadow
    g2.setColor(Color.BLACK);
    g2.drawString(titleString, fontx + FONT_SHADOW_OFFSET_X, fonty + FONT_SHADOW_OFFSET_Y);

    // Draw title
    g2.setColor(Color.WHITE);
    g2.drawString(titleString, fontx, fonty);

    // Artist font calculations
    g2.setFont(this.labelFont);
    String artistString = "Artist: " + this.metadata[4];
    fontx += (this.width * SONG_INFO_INDENT_X);
    fonty += (this.height * SONG_INFO_SPACING_Y);

    // Draw Shadow and artist name
    g2.setColor(Color.BLACK);
    g2.drawString(artistString, fontx + FONT_SHADOW_OFFSET_X, fonty + FONT_SHADOW_OFFSET_Y);
    g2.setColor(Color.WHITE);
    g2.drawString(artistString, fontx, fonty);

    // Calculate and draw source name
    if (this.metadata[5].length() > 0) {
      String sourceString = "Source: " + this.metadata[5];
      fonty += (this.height * SONG_INFO_SPACING_Y);
      g2.setColor(Color.BLACK);
      g2.drawString(sourceString, fontx + FONT_SHADOW_OFFSET_X, fonty + FONT_SHADOW_OFFSET_Y);
      g2.setColor(Color.WHITE);
      g2.drawString(sourceString, fontx, fonty);
    }

    // Draw playtime of current song
    final int timePosX = (int) (PLAYTIME_X * this.width);
    final int timePosY = (int) (PLAYTIME_Y * this.height);
    final String timeStr = getPlaytimeString(this.audioPlayer.position() / 1000);
    final int timeStrHeight = (int) this.labelFont.getLineMetrics(timeStr, g2.getFontRenderContext()).getHeight();
    g2.drawString(timeStr, timePosX, timePosY);

    // TODO: Draw progress bar
    final int progBarPosX = (int) (PROGRESS_X * this.width);
    final int progBarPosY = timePosY - (timeStrHeight >> 1);
    final double progBarLen = PROGRESS_LEN * this.width;
    g2.drawLine(progBarPosX, progBarPosY, (int) (progBarPosX + progBarLen), progBarPosY);

    // Progress indicator line
    final int progBarLinePos = (int) (((double) this.audioPlayer.position() / (double) this.audioPlayer.length())
        * progBarLen) + progBarPosX;

    g2.drawLine(progBarLinePos, progBarPosY - PROG_LINE_HEIGHT, progBarLinePos, progBarPosY + PROG_LINE_HEIGHT);
    System.out.println(progBarLinePos);

    // Draw spectrum center line
    final double specWidth = (double) ((float) this.width / (this.fft.getBandWidth() * 4.0));
    g2.drawLine(0, centerY, this.width, centerY);

    if (!this.audioPlayer.isPlaying()) return;

    /*
     * Begin FFT visualization
     */

    // Get FFT data
    this.fft.forward(this.aInput.mix);

    // Draw spectrum (top half)
    final GeneralPath gpbg = new GeneralPath();
    final GeneralPath gpfg = new GeneralPath();

    // Move paths to the leftmost point of the spectrum
    gpbg.moveTo(0, centerY);
    gpfg.moveTo(0, centerY);
    for (int i = 2; i < this.fft.avgSize() - 1; i++) {

      // For point n-1
      final double lpx = (i - 1) * specWidth;
      final double lpy = centerY - this.fft.getAvg(i - 1) * BAND_SCALE;
      final double lpy2 = centerY - (this.fft.getAvg(i - 1) * BAND_SCALE) * FG_SPEC_MULT;

      // Point n
      final double px = i * specWidth;
      final double py = centerY - this.fft.getAvg(i) * BAND_SCALE;
      final double py2 = centerY - (this.fft.getAvg(i) * BAND_SCALE) * FG_SPEC_MULT;

      // Point n + 1
      final double npx = (i + 1) * specWidth;
      final double npy = centerY - this.fft.getAvg(i + 1) * BAND_SCALE;
      final double npy2 = centerY - (this.fft.getAvg(i + 1) * BAND_SCALE) * FG_SPEC_MULT;

      // Draw curves
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

      // n + 1
      final double lpx = (i + 1) * specWidth;
      final double lpy = centerY + this.fft.getAvg(i + 1) * BAND_SCALE + 2;
      final double lpy2 = centerY + (this.fft.getAvg(i + 1) * BAND_SCALE + 4) * FG_SPEC_MULT;

      // n
      final double px = i * specWidth;
      final double py = centerY + this.fft.getAvg(i) * BAND_SCALE + 2;
      final double py2 = centerY + (this.fft.getAvg(i) * BAND_SCALE + 4) * FG_SPEC_MULT;

      // n - 1
      final double npx = (i - 1) * specWidth;
      final double npy = centerY + this.fft.getAvg(i - 1) * BAND_SCALE + 2;
      final double npy2 = centerY + (this.fft.getAvg(i - 1) * BAND_SCALE + 4) * FG_SPEC_MULT;

      // Draw curves
      Point point3 = calcPoint3(lpx, lpy, px, py);
      Point point3b = calcPoint3(lpx, lpy2, px, py2);
      gpbg.lineTo(point3.getX(), point3.getY());
      gpfg.lineTo(point3b.getX(), point3b.getY());
      point3 = calcPoint3(npx, npy, px, py);
      point3b = calcPoint3(npx, npy2, px, py2);
      gpbg.curveTo(px, py, px, py, point3.getX(), point3.getY());
      gpfg.curveTo(px, py2, px, py2, point3b.getX(), point3b.getY());
    }

    // Fill polygons with BG and FG colors
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
  private static final String stripMP3Tags(final String s) {

    final String newFileName = s.substring(0, s.length() - 4) + "0.mp3";
    try {

      // Strip tags if they exist
      final Mp3File mf = new Mp3File(s);

      // Strip tags if they exist
      if (mf.hasId3v1Tag()) mf.removeId3v1Tag();
      if (mf.hasId3v2Tag()) mf.removeId3v2Tag();
      if (mf.hasCustomTag()) mf.removeCustomTag();

      // Save temp file
      mf.save(newFileName);

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

    return newFileName;
  }

  /**
   * Calculates the curve point for Beizer curves.
   * 
   * @param x1 the first point's X coordinate.
   * @param y1 the first point's Y coordinate.
   * @param x2 the second point's X coordinate.
   * @param y2 the second point's Y coordinate.
   * @return a Point with the result.
   */
  private static final Point calcPoint3(final double x1, final double y1, final double x2, final double y2) {
    final double arcSize = 10;
    final double d1 = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    final double per = arcSize / d1;
    final double dx = (x1 - x2) * per;
    final double dy = (y1 - y2) * per;
    return new Point((int) (x2 + dx), (int) (y2 + dy));
  }

  private static final String getPlaytimeString(final int t) {
    final int mins = t / 60;
    final int secs = t % 60;

    return String.format("%d:%02d", mins, secs);
  }

  /**
   * Prints a message only if DEBUG_MODE is true.
   * 
   * @param msg the String to print to sysout.
   */
  private static final void debugOut(final String msg) {
    if (DEBUG_MODE) System.out.println(msg);
  }
}
