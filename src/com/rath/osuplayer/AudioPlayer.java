
package com.rath.osuplayer;

import java.io.File;

/**
 * This class plays audio from an .mp3 file.
 * 
 * @author Tim Backus tbackus127@gmail.com
 */
public class AudioPlayer {

  /**
   * Whether or not there is currently audio playing
   */
  private boolean isPlaying;

  /**
   * Default constructor
   */
  public AudioPlayer() {
    this.isPlaying = false;
  }

  /**
   * Plays the loaded .mp3 file
   */
  public void play() {
    isPlaying = true;
  }

  /**
   * Pauses the currently playing .mp3 file.
   */
  public void pause() {
    isPlaying = false;
  }

  /**
   * Checks whether or not there is currently audio playing
   * 
   * @return true if there is audio currently playing; false if not.
   */
  public boolean isPlaying() {
    return isPlaying;
  }

  /**
   * Loads audio data from the file specified.
   * 
   * @param f
   *          the .mp3 file to load audio from.
   */
  public void changeFile(File f) {

  }
}
