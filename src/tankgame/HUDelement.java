package tankgame;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HUDelement extends TankGame {

    Image[] element;
    int x, y, numFrames;
    String path = System.getProperty("user.dir");
    int frameCount = 0;
    final TankGame outer;

    HUDelement(String name, int numFrames, int x, int y, final TankGame outer) throws IOException {
        this.x = x;
        this.y = y;
        this.numFrames = numFrames;
        path = path + name;
        this.outer = outer;

        element = new Image[numFrames];
        if (numFrames == 1) {
            element[0] = ImageIO.read(new File(path + ".png"));
        } else {
            for (int i = 0; i < numFrames; i++) {
                element[i] = ImageIO.read(new File(path + i + ".png"));
            }
        }
    }

    public void draw(ImageObserver obs) {
        outer.g2.drawImage(element[frameCount], x, y, obs);
    }

    public void updateIncrement() {
        if (!(frameCount == numFrames - 1)) {
            frameCount++;
        }
    }

    public void updateDecrement() {
        if (frameCount != 0) {
            frameCount--;
        }
    }
}