
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

  /** Serial version UID */
  private static final long serialVersionUID = 1L;

  /** The song background from the beatmap folder */
  private BufferedImage songBG;

  /** Reference to the child panel (OptionsPanel) */
  private OptionsPanel optPanel;

  /** The audio player */
  private AudioPlayer audioPlayer;

  /** Width of this JPanel (fullscreen) */
  private int width;

  /** Height of this JPanel (fullscreen) */
  private int height;

  /** Reference to the parent JFrame (PlayerFrame) */
  private PlayerFrame parent;

  /** Font for drawing the title font */
  private Font titleFont;

  /** Font for drawing the artist and source label font */
  private Font labelFont;

  /** Timer for repainting the window */
  private Timer repaintTimer;

  /** Visualization frames per second */
  private static final int TARGET_FRAMERATE = 60;

  /**
   * Song metadata with the following indeces: 0: Beatmap directory 1:
   * Background image filename 2: Audio filename 3: Song title 4: Song artist 5:
   * Song source
   */
  private String[] metadata;

  /** The Minim library object */
  private final Minim minim;

  /** Audio input data for Minim */
  private final AudioInput aInput;

  /** Fast Fourier Transform object */
  private FFT fft;

  /** Spectrum size */
  private final int specSize;

  /**
   * Default constructor
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

    this.repaintTimer = new Timer(0, new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        parent.revalidate();
        repaint();
      }
    });

    this.repaintTimer.setDelay(Math.round(1000 / TARGET_FRAMERATE));

    this.minim = new Minim(new MinimHandler());
    this.audioPlayer = minim
        .loadFile(this.metadata[0] + "/" + this.metadata[2]);
    this.aInput = minim.getLineIn(Minim.STEREO);
    this.fft = new FFT(this.aInput.bufferSize(), this.aInput.sampleRate());
    this.specSize = this.fft.specSize();

    // Set the background of this panel
    try {

      // Convert the BufferedImage that was converted into a non-castable Image
      // back into a BufferedImage
      this.songBG = convertImage(ImageIO.read(
          new File(metadata[0] + "/" + metadata[1])).getScaledInstance(
          this.width, this.height, Image.SCALE_SMOOTH));

      // Load and set up fonts
      File fontFile = new File("res/fonts/JAPANSANS80.OTF");
      this.titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile)
          .deriveFont(64f);
      this.labelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile)
          .deriveFont(36f);
      GraphicsEnvironment ge = GraphicsEnvironment
          .getLocalGraphicsEnvironment();
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
    this.audioPlayer.play();

    this.repaintTimer.start();
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
    String currentMapDir = "Songs/"
        + beatmapFolders[rand.nextInt(beatmapFolders.length)];

    // Parse any .osu file for the background and audio file.
    return MapParser.parseBeatmap(currentMapDir);

  }

  /**
   * Fetches a new song and updates the panel.
   */
  public void newSong() {

    // Fade out audio
    this.audioPlayer.shiftGain(0.0F, -50.0F, 400);

    this.metadata = getNewMetadata();
    String filePath = metadata[0] + "/";
    try {
      this.songBG = convertImage(ImageIO.read(new File(filePath + metadata[1]))
          .getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH));
    }
    catch (IOException e) {
      System.err.println("IOE@" + filePath + metadata[1]);
    }
    catch (NullPointerException npe) {
      System.err.println("NPE@" + filePath + metadata[1]);
    }

    // Swing, why is this necessary...?
    remove(optPanel);
    add(optPanel);
    repaint();

    // Reload the audio player.
    this.audioPlayer.close();
    this.audioPlayer = minim.loadFile(filePath + this.metadata[2]);

    // Start playing again
    // TODO: Fix mp3's with 0 lead-in time playing too late.
    this.audioPlayer.setGain(-50.0F);
    this.audioPlayer.play();
    this.audioPlayer.shiftGain(-50.0F, 0.0F, 400);

  }

  /**
   * Switches the current audio state from playing to paused, and vice-versa.
   */
  public void togglePause() {
    System.err.println("Pause toggled.");
    if (this.audioPlayer.isPlaying()) {

      this.audioPlayer.pause();
    } else {
      this.audioPlayer.play();
    }
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
   * Renders the panel by painting the background image of the beatmap to the
   * background of this panel, and also paints the song metadata on screen.
   * 
   * @param g the Graphics object
   */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(this.songBG, 0, 0, null);

    // Song title font calculations
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
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
    final double specWidth = (double) this.width / (double) this.specSize;
    g2.drawLine(0, centerY, this.width, centerY);

    // Get FFT data
    fft.forward(this.aInput.left);
    for (int i = 0; i < this.specSize; i++) {
      float band = this.fft.getBand(i);
      int bandExp = (int) (band * 1000F * (float) centerY);

      // System.out.println(specSize + "," + band + "," + specWidth + "," +
      // bandExp);
      g2.drawLine((int) (specWidth * i), centerY - bandExp,
          (int) (specWidth * (i + 1)), centerY);
    }
  }

  /**
   * Converts an Image into a BufferedImage when casting does not work.
   * 
   * @param img the Image to convert to a BufferedImage.
   * @return the BufferedImage that was converted.
   */
  private BufferedImage convertImage(Image img) {
    BufferedImage result = new BufferedImage(img.getWidth(null),
        img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    result.getGraphics().drawImage(img, 0, 0, null);
    return result;
  }
}
