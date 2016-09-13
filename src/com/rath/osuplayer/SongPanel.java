
package com.rath.osuplayer;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

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
   * The song background from the beatmap folder
   */
  private Image songBG;

  public SongPanel(String[] meta) {
    super();
    try {
      System.err.println(meta[0] + "/" + meta[1]);
      this.songBG = ImageIO.read(new File(meta[0] + "/" + meta[1]));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    g.drawImage(this.songBG, 0, 0, null);
  }
}
