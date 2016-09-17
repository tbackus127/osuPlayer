
package com.rath.osuplayer;

import java.io.File;
import java.io.FileNotFoundException;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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
   * The JavaFX media player
   */
  private MediaPlayer player;

  /** The current file to play or being played */
  private File audioFile;

  /**
   * URI String for audio player
   */
  private String uriString;

  /** Media handle for audio player */
  private Media media;

  /**
   * Default constructor
   * @throws FileNotFoundException 
   */
  public AudioPlayer(String fs) throws FileNotFoundException {

    // Needed to get JavaFX and swing to play nicely
    new javafx.embed.swing.JFXPanel();

    this.isPlaying = false;
    this.audioFile = new File(fs);
    if(!this.audioFile.exists()) {
      System.err.println("File " + this.audioFile.getAbsolutePath() + " not found!");
      throw new FileNotFoundException();
    }
    this.uriString = this.audioFile.toURI().toString();
    this.media = new Media(this.uriString);
    this.player = new MediaPlayer(this.media);

  }

  /**
   * Plays the loaded .mp3 file
   */
  public void play() {
    System.err.println("Playing audio");
    isPlaying = true;
    this.player.play();
  }

  /**
   * Pauses the currently playing .mp3 file.
   */
  public void pause() {
    isPlaying = false;
    this.player.pause();
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
   * Stops the player from playing
   */
  public void stop() {
    this.player.stop();
  }

  /**
   * Loads audio data from the file specified.
   * 
   * @param f
   *          the .mp3 file to load audio from.
   */
  public void setFile(File f) {
    stop();
    this.uriString = f.toURI().toString();
    this.media = new Media(this.uriString);
    this.player = new MediaPlayer(this.media);
  }
}
