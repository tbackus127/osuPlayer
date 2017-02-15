
package com.rath.osuplayer;

import java.awt.Dimension;

import javax.swing.JPanel;

public class SongFilterPanel extends JPanel {

  private static final Dimension PANEL_SIZE = new Dimension(400, 80);

  /** Serial UID. */
  private static final long serialVersionUID = 1L;

  public SongFilterPanel() {
    super();
  }

  @Override
  public Dimension getPreferredSize() {
    return PANEL_SIZE;
  }

  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }
}
