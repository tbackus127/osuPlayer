
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

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

  // --------------------------------------------------------------------------
  /** How many recently played songs to keep track of. */
  private static final int QUEUE_THRESHOLD = 24;

  /** How many times the system tries to pick a song that was not recently played before giving up. */
  private static final int RECENT_RETRY_THRESHOLD = 20;

  /** Recently played songs filename. */
  private static final String RECENT_LIST_FILENAME = "osuplayer-recent.dat";

  // --------------------------------------------------------------------------
  /** How many bands are in an octave. */
  private static final int NUM_BANDS = 256; // Was 256

  /** Bar vertical scaling. */
  private static final double BAND_SCALE = 2.8D;

  /** Width of each spectrum bar. */
  private static final int FFT_BAR_WIDTH = 12;

  /** Space between each bar. */
  private static final int FFT_BAR_SPACING = 12;

  /** Visualization frames per second. */
  private static final int TARGET_FRAMERATE = 75;

  // --------------------------------------------------------------------------
  /** Default song title font size. */
  private static final float DEFAULT_TITLE_FONT_SIZE = 32.0F;

  /** Default song metadata font size. */
  private static final float DEFAULT_LABEL_FONT_SIZE = 22.0F;

  /** Path to the default font. */
  private static final String DEFAULT_FONT_STRING = "res/fonts/JAPANSANS80.OTF";

  // --------------------------------------------------------------------------
  /** Horizontal position of the song timer. */
  private static final double PLAYTIME_X = 0.7D;

  /** Vertical position of the song timer. */
  private static final double PLAYTIME_Y = 0.08D;

  /** Multiplier for horizontal progress bar positioning. */
  private static final double PROGRESS_X = 0.63D;

  /** Horizontal progress bar positioning. */
  private static final double PROGRESS_Y = 0.08;

  /** Progress bar length. */
  private static final double PROGRESS_LEN = 0.1D;

  /** The height of the progress bar inducator line. */
  private static final int PROG_LINE_HEIGHT = 12;

  // --------------------------------------------------------------------------
  /** The amount of spacing relative to window height between lines of a song's information. */
  private static final int SONGINFO_SPACING_Y = 24;

  /** Song metadata indentation amount. */
  private static final int SONGINFO_INDENT_X = 16;

  /** The minimum width of the song info panel. */
  private static final int SONGINFO_BG_MINWIDTH = 640;

  /** The relative vertical position of the info background. */
  private static final double SONGINFO_BG_Y = 0.01D;

  /** Left song info border filepath. */
  private static final String SONGINFO_BG_L = "res/img/info-border-left.png";

  /** Right song info border filepath. */
  private static final String SONGINFO_BG_R = "res/img/info-border-right.png";

  /** Song info border filepath (scalable). */
  private static final String SONGINFO_BG = "res/img/info-border-center.png";

  /** Width of the song info borders. */
  private static final int SONGINFO_W = 61;

  /** Height of the song info borders. */
  private static final int SONGINFO_H = 122;

  /** How many pixels lower the song title String will be drawn relative to the info panel. */
  private static final int SONGINFO_TEXT_START = 44;

  // --------------------------------------------------------------------------
  /** Filter panel's length. */
  private static final int FILTER_PANEL_HEIGHT = 80;

  // --------------------------------------------------------------------------
  /** The song background from the beatmap folder. */
  private BufferedImage songBG;

  /** The song info background left border. */
  private BufferedImage infoBGL;

  /** The song info background right border. */
  private BufferedImage infoBGR;

  /** The song info background. */
  private BufferedImage infoBGC;

  /** Reference to the filter panel. */
  private SongFilterPanel searchPanel;

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

  /** The color of the spectrum bars. */
  private Color barColor = new Color(255, 255, 255, 180);

  /** Font for drawing the artist and source label font. */
  private Font labelFont;

  /** Timer for repainting the window. */
  private final Timer repaintTimer;

  /** Key listener for SongPanel. */
  private PlayerKeyListener playerKeyListener;

  /**
   * Song metadata with the following indeces: 0: Beatmap directory 1: Background image filename 2: Audio filename 3:
   * Song title 4: Song artist 5: Song source
   */
  private String[] metadata;

  /** The title of the currently playing song. */
  private String songTitle;

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

    // Get the recently played queue if it exists
    this.recentlyPlayedSongs = new ArrayDeque<String>();

    final File recentlyPlayedFile = new File(RECENT_LIST_FILENAME);
    if (recentlyPlayedFile.exists()) {
      Scanner fscan = null;
      try {
        fscan = new Scanner(recentlyPlayedFile);
        while (fscan.hasNextLine()) {
          this.recentlyPlayedSongs.add(fscan.nextLine());
        }

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        fscan.close();
      }
    }

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
    } catch (NullPointerException npe) {
      System.err.println("Stereo Mix not enabled!");
      return;
    }
    this.fft.logAverages(NUM_BANDS, 10);

    // Set the background of this panel
    try {
      this.songBG = convertImage(ImageIO.read(new File(metadata[0] + "/" + metadata[1])).getScaledInstance(this.width,
          this.height, Image.SCALE_SMOOTH));

      // Song info background
      this.infoBGC = ImageIO.read(new File(SONGINFO_BG));
      this.infoBGL = ImageIO.read(new File(SONGINFO_BG_L));
      this.infoBGR = ImageIO.read(new File(SONGINFO_BG_R));

      // Load and set up fonts
      final File fontFile = new File(DEFAULT_FONT_STRING);
      this.titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(DEFAULT_TITLE_FONT_SIZE);
      this.labelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(DEFAULT_LABEL_FONT_SIZE);
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));

    } catch (IOException e) {
      e.printStackTrace();
    } catch (FontFormatException ffe) {
      ffe.printStackTrace();
    }

    // Get the complementary bar color for this image
    this.barColor = getBarColor(this.songBG);

    // Calculate title string width
    this.songTitle = this.metadata[3];

    // Create and add the filter panel
    this.searchPanel = new SongFilterPanel(this);
    // par.add(this.searchPanel);
    this.searchPanel.setBounds(this.width >> 1, 0, this.width >> 1, FILTER_PANEL_HEIGHT);

    // Player key listener
    this.playerKeyListener = new PlayerKeyListener(this);
    this.playerKeyListener.setAudioPlayer(this.audioPlayer);
    addKeyListener(this.playerKeyListener);
    setFocusable(true);
    requestFocus();

    // Start everything
    this.repaintTimer.start();
    this.audioPlayer.play();
  }

  /**
   * Parses another random beatmap and loads its metadata.
   * 
   * @return a String[] with indeces: {directory, BG-image, audio file, title, artist, source}
   */
  public final String[] getNewMetadata() {

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
  public final void newSong() {

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
    this.songTitle = metadata[3];
    this.recentlyPlayedSongs.add(this.songTitle);
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
    } catch (IOException e) {
      System.err.println("IOE@" + filePath + metadata[1]);
      this.songBG = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_BINARY);
    } catch (NullPointerException npe) {
      System.err.println("NPE@" + filePath + metadata[1]);
    }

    this.barColor = getBarColor(this.songBG);

    // Start playing again
    this.audioPlayer.play();
    this.repaintTimer.start();

    this.playerKeyListener.setAudioPlayer(this.audioPlayer);
  }

  /**
   * Switches the current audio state from playing to paused, and vice-versa.
   */
  public final void togglePause() {

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
  public final boolean isPaused() {

    return !this.audioPlayer.isPlaying();
  }

  /**
   * Calls the main JFrame's closeEverything() method
   */
  public final void closeEverything() {

    this.audioPlayer.close();
    this.minim.stop();
    this.minim.dispose();
    saveQueue(this.recentlyPlayedSongs);
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
    drawBackground(g2);

    final int centerY = this.height >> 1;
    drawSongInfo(g2, centerY);

    if (!this.audioPlayer.isPlaying())
      return;

    drawFFT(g2, centerY);
  }

  /**
   * Draws the background image.
   * 
   * @param g2 Graphics2D object.
   */
  private final void drawBackground(final Graphics2D g2) {

    if (this.songBG != null) {
      g2.drawImage(this.songBG, 0, 0, null);
    }
  }

  /**
   * Draws the song info panel.
   * 
   * @param g2 Graphics2D object.
   * @param centerY half of the window height.
   */
  private final void drawSongInfo(final Graphics2D g2, final int centerY) {

    // Calculate song title width
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setFont(this.titleFont);
    final int titleWidth = g2.getFontMetrics(this.titleFont).stringWidth(this.songTitle);

    // Ensure song info container is not smaller than the minimum
    int bgWidth = (titleWidth > SONGINFO_BG_MINWIDTH) ? titleWidth : SONGINFO_BG_MINWIDTH;

    // Left info container border
    final int infoBGx = (this.width >> 1) - SONGINFO_W - (bgWidth >> 1);
    final int infoBGy = (int) (SONGINFO_BG_Y * this.height);
    g2.drawImage(this.infoBGL, infoBGx, infoBGy, null);

    // Middle of info container
    final BufferedImage scaledInfoBG = convertImage(
        this.infoBGC.getScaledInstance(bgWidth, SONGINFO_H, Image.SCALE_FAST));
    final int infoBGcx = infoBGx + SONGINFO_W;
    g2.drawImage(scaledInfoBG, infoBGcx, infoBGy, null);

    // Right border of info container
    final int infoBGrx = infoBGcx + bgWidth;
    g2.drawImage(this.infoBGR, infoBGrx, infoBGy, null);

    // Song title font calculations
    int stringPosx = infoBGcx;
    int stringPosy = (int) (infoBGy + SONGINFO_TEXT_START);

    // Draw title
    g2.setColor(Color.WHITE);
    g2.drawString(this.songTitle, stringPosx, stringPosy);

    // Artist font calculations
    g2.setFont(this.labelFont);
    String artistString = "Artist: " + this.metadata[4];
    stringPosx += SONGINFO_INDENT_X;
    stringPosy += SONGINFO_SPACING_Y + 2;

    // Draw artist name
    g2.drawString(artistString, stringPosx, stringPosy);

    // Get and draw source name
    if (this.metadata[5] != null && this.metadata[5].length() > 0) {
      String sourceString = "Source: " + this.metadata[5];
      stringPosy += SONGINFO_SPACING_Y - 2;
      g2.drawString(sourceString, stringPosx, stringPosy);
    }

    //    drawProgressBar(g2);
  }

  /**
   * Draw the player's progress bar and playtime.
   * 
   * @param g2 the Graphics2D object.
   */
  private final void drawProgressBar(final Graphics2D g2) {

    // Draw playtime of current song
    final int timePosX = (int) (PLAYTIME_X * this.width);
    final int timePosY = (int) (PLAYTIME_Y * this.height) + 2;
    final String timeStr = getPlaytimeString(this.audioPlayer.position() / 1000);
    final int timeStrHeight = (int) this.labelFont.getLineMetrics(timeStr, g2.getFontRenderContext()).getHeight();
    g2.drawString(timeStr, timePosX, timePosY - 2);

    // Draw progress bar
    final int progBarPosX = (int) (PROGRESS_X * this.width);
    final int progBarPosY = (int) (PROGRESS_Y * this.height);
    final double progBarLen = PROGRESS_LEN * this.width;
    g2.drawLine(progBarPosX, progBarPosY, (int) (progBarPosX + progBarLen), progBarPosY);

    // Progress indicator line
    final int progBarLinePos = (int) (((double) this.audioPlayer.position() / (double) this.audioPlayer.length())
        * progBarLen) + progBarPosX;
    g2.drawLine(progBarLinePos, progBarPosY - PROG_LINE_HEIGHT, progBarLinePos, progBarPosY + PROG_LINE_HEIGHT);
  }

  /**
   * Draws the spectrum visualization.
   * 
   * @param g2 Graphics2D object.
   * @param centerY half the screen's height.
   */
  private final void drawFFT(final Graphics2D g2, final int centerY) {

    g2.setColor(this.barColor);

    // Get FFT data
    this.fft.forward(this.aInput.mix);

    for (int i = 0; i < this.fft.avgSize() - 1; i++) {

      // Calculate and render the FFT values
      final double py = this.fft.getAvg(i) * BAND_SCALE;
      g2.fillRect(i * (FFT_BAR_WIDTH + FFT_BAR_SPACING), this.height - (int) py, FFT_BAR_WIDTH, (int) py);
    }

  }

  /**
   * Converts an Image into a BufferedImage when casting does not work.
   * 
   * @param img the Image to convert to a BufferedImage.
   * @return the BufferedImage that was converted.
   */
  private static final BufferedImage convertImage(final Image img) {

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
      if (mf.hasId3v1Tag())
        mf.removeId3v1Tag();
      if (mf.hasId3v2Tag())
        mf.removeId3v2Tag();
      if (mf.hasCustomTag())
        mf.removeCustomTag();

      // Save temp file
      mf.save(newFileName);

    } catch (UnsupportedTagException e) {
      System.err.println("Unsupported tag!\n" + e.getMessage());
    } catch (InvalidDataException e) {
      System.err.println("Mp3 file is corrupt!");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (NotSupportedException e) {
      e.printStackTrace();
    }

    return newFileName;
  }

  /**
   * Gets a timestamp from a time in milliseconds.
   * 
   * @param t the time, in milliseconds.
   * @return a String of the format "M:SS".
   */
  private static final String getPlaytimeString(final int t) {

    final int mins = t / 60;
    final int secs = t % 60;

    return String.format("%d:%02d", mins, secs);
  }

  /**
   * Gets the complementary color of the average color of the bottom of the background image.
   * 
   * @param bg the BufferedImage to read.
   * @return a Color object with the average complementary color.
   */
  private static final Color getBarColor(final BufferedImage bg) {

    long redVal = 0L;
    long greenVal = 0L;
    long blueVal = 0L;

    // Sample from 70% of the image's height (favor the bottom)
    final double vertStart = 0.7D;

    // Calculate number of samples
    final int samplesX = 48;
    final int samplesY = 16;
    final int sampleLength = bg.getWidth() / samplesX;
    final int sampleHeight = (int) (bg.getHeight() * (1.0D - vertStart)) / samplesY;

    // Sample image and accumulate RGB values
    for (int i = 0; i < bg.getWidth(); i += sampleLength) {
      for (int j = (int) (bg.getHeight() * vertStart); j < bg.getHeight(); j += sampleHeight) {
        final int pxrgb = bg.getRGB(i, j);
        redVal += (pxrgb >> 16) & 0xFF;
        greenVal += (pxrgb >> 8) & 0xFF;
        blueVal += pxrgb & 0xFF;
      }
    }

    // Average and cap color values
    final int sampleSize = samplesX * samplesY;
    int pr = (int) (redVal / sampleSize);
    int pg = (int) (greenVal / sampleSize);
    int pb = (int) (blueVal / sampleSize);
    if (pr > 255)
      pr = 255;
    if (pg > 255)
      pg = 255;
    if (pb > 255)
      pb = 255;

    return new Color(255 - pr, 255 - pg, 255 - pb, 180);
  }

  /**
   * Saves the current song queue to a file.
   * 
   * @param q the queue.
   */
  private static final void saveQueue(final Queue<String> q) {

    final ArrayList<String> list = new ArrayList<String>(q);
    PrintStream fout = null;

    try {
      fout = new PrintStream(RECENT_LIST_FILENAME);

      for (final String s : list) {
        fout.println(s);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      fout.close();
    }
  }

  /**
   * Prints a message only if DEBUG_MODE is true.
   * 
   * @param msg the String to print to sysout.
   */
  private static final void debugOut(final String msg) {

    if (DEBUG_MODE)
      System.out.println(msg);
  }
}
