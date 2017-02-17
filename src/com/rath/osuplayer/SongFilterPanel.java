
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This panel will handle searching and filtering beatmaps.
 * 
 * @author Tim Backus tbackus127@gmail.com
 *
 */
public class SongFilterPanel extends JPanel {

  /** Size of this panel. */
  private static final Dimension PANEL_SIZE = new Dimension(400, 80);

  /** Serial UID. */
  private static final long serialVersionUID = 1L;

  /** The default text that is shown in the search field. */
  private static final String DEFAULT_FIELD_TEXT = "Search for a song...";

  /** The height of the text field. */
  private static final int FIELD_HEIGHT = 48;

  /** Width of the magnifying glass icon. */
  private static final int MAG_WIDTH = 80;

  /** The location and filename of the font. */
  private static final String SEARCH_FONT_STRING = "res/fonts/JAPANSANS80.otf";

  /** Search font color. */
  private static final Color SEARCH_FONT_COLOR = new Color(220, 220, 220, 255);

  /** The size of the search font. */
  private static final float SEARCH_FONT_SIZE = 32F;

  /** Reference to the parent panel. */
  private final SongPanel parent;

  /** The magnifying glass icon. */
  private Image magImg;

  /** The search field font. */
  private Font searchFont;

  /** The search text field. */
  private final JTextField searchField;

  /** The width of this panel. */
  private final int width;

  /** The height of this panel. */
  private final int height;

  /** Whether or not the search field has been changed. */
  private boolean searchFieldChanged = false;

  /**
   * Default constructor.
   * 
   * @param sp reference to the parent panel.
   */
  public SongFilterPanel(final SongPanel sp) {
    super();

    this.parent = sp;

    try {
      this.magImg = ImageIO.read(new File("res/img/mag.png"));
      final File fontFile = new File(SEARCH_FONT_STRING);
      this.searchFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(SEARCH_FONT_SIZE);
    }
    catch (IOException e) {
      System.err.println("Cannot read mag.png.");
    }
    catch (FontFormatException e) {
      e.printStackTrace();
    }

    // Calculation variables
    final Dimension pdim = this.parent.getPreferredSize();
    final int pw = pdim.width;
    final int ph = pdim.height;
    this.width = (pw >> 1) - MAG_WIDTH;
    this.height = (ph >> 4);

    // Set size and transparency
    setBounds(new Rectangle(0, ph - this.height, this.width - MAG_WIDTH, this.height));
    setBackground(new Color(0, 0, 0, 160));
    setLayout(new FlowLayout());

    // Draw magnifying glass
    if (this.magImg != null) {
      final JLabel imgLbl = new JLabel(new ImageIcon(this.magImg));
      add(imgLbl);
    }

    // Create search field
    this.searchField = new JTextField();

    this.searchField.addKeyListener(new KeyListener() {

      @Override
      public void keyPressed(KeyEvent arg0) {}

      @Override
      public void keyReleased(KeyEvent arg0) {}

      @Override
      public void keyTyped(KeyEvent arg0) {

        if (!searchFieldChanged) {
          searchFieldChanged = true;
          searchField.setText("");
        }

        // TODO: Search library
      }

    });

    if (this.searchFont != null) {
      this.searchField.setFont(this.searchFont);
    }

    this.searchField.setForeground(SEARCH_FONT_COLOR);
    this.searchField.setText(DEFAULT_FIELD_TEXT);
    this.searchField.setOpaque(false);
    this.searchField.setPreferredSize(new Dimension(((int) pdim.getWidth() >> 1) - MAG_WIDTH, FIELD_HEIGHT));
    this.searchField.setBorder(BorderFactory.createEmptyBorder());
    add(this.searchField);
  }

  /**
   * Gets the preferred size of this panel.
   * 
   * @return a Dimension specified by PANEL_SIZE.
   */
  @Override
  public Dimension getPreferredSize() {
    return PANEL_SIZE;
  }

  /**
   * Gets the maximum size of this panel.
   * 
   * @return a Dimension specified by PANEL_SIZE.
   */
  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  /**
   * Gets the minimum size of this panel.
   * 
   * @return a Dimension specified by PANEL_SIZE.
   */
  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }
}
