
package com.rath.osuplayer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;

/**
 * @author Tim Backus tbackus127@gmail.com
 */
public class OsuPlayer {

  /**
   * Retrieves metadata from a beatmap directory
   * 
   * @param map
   *          the beatmap directory to get metadata from
   * @return a String[] with the map's directory, background file, audio file,
   *         title, artist, and source, respectively.
   */
  private static String[] parseBeatmap(String dir) {
    String[] result = new String[6];
    File map = new File(dir);
    result[0] = dir;

    // Choose the first .osu file we come across to parse
    File osuFile = map.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File curr, String name) {
        return new File(curr, name).getName().endsWith("osu");
      }
    })[0];

    // Go through the .osu file and find the correct metadata
    try {
      Scanner fscan = new Scanner(osuFile, "UTF-8");
      int foundCount = 0;
      while (fscan.hasNextLine()) {

        // We've already found the info we need
        if (foundCount >= 4) break;

        String line = fscan.nextLine();

        // Audio file
        if (line.startsWith("AudioFilename:")) {
          result[2] = line.split(":", 2)[1];
          foundCount++;

          // Song Title
        } else if (line.startsWith("Title:")) {
          result[3] = line.split(":", 2)[1];
          foundCount++;

          // Song Artist
        } else if (line.startsWith("Artist:")) {
          result[4] = line.split(":", 2)[1];
          foundCount++;

          // Song Source
        } else if (line.startsWith("Source:")) {
          result[5] = line.split(":", 2)[1];

          // Background Image
        } else if (line.startsWith("//Background and Video")) {
          line = fscan.nextLine();
          result[1] = line.split(",")[2];
          result[1] = result[1].substring(1, result[1].length() - 1);
          foundCount++;
        }
      }
      fscan.close();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Main method
   * 
   * @param args
   *          Runtime arguments
   */
  public static void main(String[] args) {

    // -------------------------------------------------------------
    // Beatmap processing
    // -------------------------------------------------------------

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
    String[] mapMetadata = parseBeatmap(currentMapDir);
    SongPanel songPanel = new SongPanel(mapMetadata);

    // -------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------

    // Get screen dimensions
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    JFrame frame = new JFrame();

    // Create window and display it
    frame.setSize(dim);
    frame.setPreferredSize(dim);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    frame.setUndecorated(true);
    frame.setLayout(null);

    frame.add(songPanel);
    frame.revalidate();
    frame.repaint();
    
    frame.setVisible(true);
  }

}
