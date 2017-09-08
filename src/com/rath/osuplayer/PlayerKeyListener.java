
package com.rath.osuplayer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ddf.minim.AudioPlayer;

/**
 * This class listens for key presses in the main window.
 * 
 * @author Tim Backus tbackus127@gmail.com
 */
public class PlayerKeyListener implements KeyListener {

  /** Reference to the SongPanel. */
  private final SongPanel songPanel;

  /** The audio player to control. */
  private AudioPlayer player;

  /**
   * Default constructor.
   * 
   * @param p Minim AudioPlayer.
   */
  public PlayerKeyListener(final SongPanel sp) {
    this.songPanel = sp;
  }

  public final void setAudioPlayer(final AudioPlayer ap) {

    this.player = ap;
  }

  /**
   * Triggers when a key is pressed.
   * 
   * @param evt KeyEvent
   */
  @Override
  public void keyPressed(KeyEvent evt) {

    if (this.player == null)
      return;

    // Left arrow = back
    if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
      if (evt.isShiftDown()) {
        this.player.cue(this.player.position() - 5000);
      } else {
        this.player.cue(this.player.position() - 1000);
      }

      // Right arrow = forward
    } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
      if (evt.isShiftDown()) {
        this.player.cue(this.player.position() + 5000);
      } else {
        this.player.cue(this.player.position() + 1000);
      }

      // ESC = Close
    } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
      this.songPanel.closeEverything();

      // N = new song
    } else if (evt.getKeyCode() == KeyEvent.VK_N) {
      this.songPanel.newSong();
    } else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
      this.songPanel.togglePause();
    }
  }

  /**
   * Triggers when a key is released (does nothing).
   * 
   * @param evt KeyEvent
   */
  @Override
  public void keyReleased(KeyEvent e) {}

  /**
   * Triggers when a key is typed (does nothing).
   * 
   * @param evt KeyEvent
   */
  @Override
  public void keyTyped(KeyEvent evt) {}

}
