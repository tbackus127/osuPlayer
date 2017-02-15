
package com.rath.osuplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

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

  /** Reference to the parent panel. */
  private final SongPanel parent;

  /** The search text field. */
  private final JTextField searchField;

  /** The width of this panel. */
  private final int width;

  /** The height of this panel. */
  private final int height;

  /**
   * Default constructor.
   * 
   * @param sp reference to the parent panel.
   */
  public SongFilterPanel(final SongPanel sp) {
    super();

    this.parent = sp;

    this.searchField = new JTextField();
    this.searchField.setText(DEFAULT_FIELD_TEXT);

    // Calculation variables
    final Dimension pdim = this.parent.getPreferredSize();
    final int pw = pdim.width;
    final int ph = pdim.height;
    this.width = (pw >> 1);
    this.height = (ph >> 4);

    // Set size and transparency
    setBounds(new Rectangle(0, ph - this.height, this.width, this.height));
    setOpaque(false);
    setBackground(new Color(0, 0, 0, 0));
    setLayout(new FlowLayout());

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
