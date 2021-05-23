package tankgame;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Explosion extends TankGame {
    Image[] frames;
    int x, y, framesCount, frameNumber;
    String path = System.getProperty("user.dir");
    private final TankGame outer;

    Explosion(String name, int framesCount, int x, int y, final TankGame outer) throws IOException {
        this.outer = outer;
        this.x = x;
        this.y = y;
        this.framesCount = framesCount;
        frameNumber = 0;
        frames = new Image[framesCount];
        path = path + name;

        // load individual frames of explosion animation
        for (int i = 1; i <= framesCount; i++) {
            frames[i - 1] = ImageIO.read(new File(path + i + ".png"));           
        }
    }

    public void draw(ImageObserver obs) throws InterruptedException, IOException {
        outer.g2.drawImage(this.frames[frameNumber], x, y, obs);
    }

    public void updateIncrement() throws InterruptedException {
        frameNumber++;
    }
}