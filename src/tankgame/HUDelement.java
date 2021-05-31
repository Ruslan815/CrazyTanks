package tankgame;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HUDElement {

    Image[] element;
    int x, y, framesCount;
    String path = System.getProperty("user.dir");
    int frameNumber = 0;
    final TankGame outer;

    HUDElement(String name, int framesCount, int x, int y, final TankGame outer) throws IOException {
        this.x = x;
        this.y = y;
        this.framesCount = framesCount;
        path = path + name;
        this.outer = outer;

        element = new Image[framesCount];
        if (framesCount == 1) {
            element[0] = ImageIO.read(new File(path + ".png"));
        } else {
            for (int i = 0; i < framesCount; i++) {
                element[i] = ImageIO.read(new File(path + i + ".png"));
            }
        }
    }

    public void draw(ImageObserver obs) {
        outer.g2.drawImage(element[frameNumber], x, y, obs);
    }

    public void updateIncrement() {
        if (!(frameNumber == framesCount - 1)) {
            frameNumber++;
        }
    }

    public void updateDecrement() {
        if (frameNumber != 0) {
            frameNumber--;
        }
    }
}