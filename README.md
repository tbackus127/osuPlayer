## osu! Music Player

This project will be able to be ran inside the main directory for the rhythm game 'osu!' as an executable .jar file when it is finalized.
It will choose a random beatmap and display its background, song title, artist, and source by parsing the beatmap file.
Along with a map's metadata, it displays a spectrum visualization, song progress bar (currently disabled), and may in the future render storyboards and video included with the beatmap.


#### How to Compile

1. Ensure the latest version of the JDK is installed (both 'java' and 'javac' are valid commands).
2. Open a terminal in the project root.
3. Compile with 'javac -cp "lib/*" src/com/rath/osuplayer/*.java

#### How to Run

1. You will first need a collection of osu! beatmaps, unzipped, in a folder named "Songs" in the project root directory. Download them either from the website http://osu.ppy.sh, or from osu!direct if you are a supporter.
3. Launch the player with 'java -cp "src;lib/*" com.rath.osuplayer.OsuPlayer' to run (use a colon instead of a semicolon if running from a UNIX-based system).
4. Use the play/pause button to pause and unpause the song, the circular arrow will randomly select a new song, and the X button will close the player.
