## osu! Music Player

This project will (when finished) be able to simply be ran inside the main directory for the rhythm game 'osu!'.
It will choose a random beatmap and display its background, song title, artist, and source by parsing the beatmap file.
Along with a map's metadata, it will feature a spectrum visualization, song progress bar, and perhaps even storyboard/video rendering.


#### How to Compile

1. Ensure the latest version of the JDK is installed (both 'java' and 'javac' are valid commands).
2. Open a terminal in the project root.
3. Compile with 'javac -cp lib/* src/com/rath/osuplayer/*.java

#### How to Run

1. You will first need a collection of osu! beatmaps, unzipped, in a folder named "Songs" in the project root directory.
2. Launch the player with 'java -cp "bin;lib/*" com.rath.osuplayer.OsuPlayer' to run.
3. Use the play/pause button to pause and unpause the song, the circular arrow will randomly select a new song, and the X button will close the player.
