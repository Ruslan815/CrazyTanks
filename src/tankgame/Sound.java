package tankgame;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.sound.sampled.*;

public class Sound {
    URL url;
    private Clip clip;
    boolean loop;
    String path = System.getProperty("user.dir");

    Sound(String name, boolean loop) throws MalformedURLException, LineUnavailableException, UnsupportedAudioFileException, IOException {
        url = new URL("file:///" + path + name);
        clip = AudioSystem.getClip();
        this.loop = loop;
        AudioInputStream audioIS = AudioSystem.getAudioInputStream(url);
        clip.open(audioIS);
        SwingUtilities.invokeLater(() -> {}); // Отдельный Thread для потока музыки
    }

    public void play() {
        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.setFramePosition(clip.getFrameLength());
            clip.loop(1);
        }
    }

    public void flush() {
        clip.flush();
    }
}