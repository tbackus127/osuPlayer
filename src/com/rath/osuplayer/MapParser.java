
package com.rath.osuplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;

public class MapParser {
  
  /**
   * Retrieves metadata from a beatmap directory
   * 
   * @param map the beatmap directory to get metadata from
   * @return a String[] with the map's directory, background file, audio file, title, artist, and source, respectively.
   */
  public static String[] parseBeatmap(String dir) {
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
          result[2] = line.split(":", 2)[1].trim();
          foundCount++;
          
          // Song Title
        } else if (line.startsWith("Title:")) {
          result[3] = line.split(":", 2)[1].trim();
          foundCount++;
          
          // Song Artist
        } else if (line.startsWith("Artist:")) {
          result[4] = line.split(":", 2)[1].trim();
          foundCount++;
          
          // Song Source
        } else if (line.startsWith("Source:")) {
          result[5] = line.split(":", 2)[1].trim();
          
          // Background Image
        } else if (line.startsWith("//Background and Video")) {
          line = fscan.nextLine();
          if (line.startsWith("Video")) line = fscan.nextLine();
          result[1] = line.split(",")[2].trim();
          result[1] = result[1].substring(1, result[1].length() - 1);
          foundCount++;
        }
      }
      fscan.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    
    return result;
  }
}
