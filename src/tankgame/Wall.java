package tankgame;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Wall {

    Image img;
    Rectangle box;
    int x, sizeX;
    int y, sizeY;
    boolean breakable, show;
    String blockName;

    private final TankGame outer;

    Wall(Image img, int x, int y, boolean breakable, String pickup, final TankGame outer) {
        this.outer = outer;
        this.img = img;
        this.x = x;
        this.y = y;
        this.sizeX = img.getWidth(null);
        this.sizeY = img.getHeight(null);
        this.breakable = breakable;
        this.blockName = pickup;
        show = true;
    }

    public void update() throws IOException, MalformedURLException, LineUnavailableException, UnsupportedAudioFileException {
        if (outer.m1.collision(x, y, sizeX, sizeY)) {
            show = true;
            outer.m1.speed = -5;
            if (outer.m1.x < this.x - 5) {
                outer.m1.x -= 5;
            } else {
                outer.m1.x += 5;
            }
            if (outer.m1.y < this.y - 5) {
                outer.m1.y -= 5;
            } else {
                outer.m1.y += 5;
            }
            outer.m1.speed = 5;

            if (this.blockName != null && this.blockName.equals("health")) {
                outer.m1.health += 25;
                outer.healthBar1.updateDecrement();
                System.out.println("Player 1 picked up health.");
                this.show = false;
                this.y = -100;
            }
        }
        if (outer.m2.collision(x, y, sizeX, sizeY)) {
            show = true;
            outer.m2.speed = -5;
            if (outer.m2.x < this.x - 5) {
                outer.m2.x -= 5;
            } else {
                outer.m2.x += 5;
            }
            if (outer.m2.y < this.y - 5) {
                outer.m2.y -= 5;
            } else {
                outer.m2.y += 5;
            }
            outer.m2.speed = 5;

            if (this.blockName != null && this.blockName.equals("health")) {
                outer.m2.health += 25;
                outer.healthBar2.updateDecrement();
                System.out.println("Player 2 picked up health.");
                this.show = false;
                this.y = -100;
            }
        }

        for (TankGame.Bullet tempBullet : outer.bulletsList) {
            if (tempBullet != null) {
                if (this.collision(tempBullet.x, tempBullet.y, tempBullet.width, tempBullet.height)) {
                    String bulletOwner = tempBullet.getOwnedBy();
                    if (bulletOwner.equals("m1") || bulletOwner.equals("m2")) {
                        tempBullet.show = false;
                        outer.explode1 = new Explosion("/Resources/explosion1_", 6, tempBullet.x, tempBullet.y, outer);
                        if (this.breakable) {
                            this.show = false;
                            this.y = -100;
                        }
                    }
                }
            }
        }
    }

    public void draw(ImageObserver obs) {
        outer.g2.drawImage(img, x, y, obs);
    }

    public boolean collision(int x, int y, int w, int h) {
        box = new Rectangle(this.x, this.y, this.sizeX, this.sizeY);
        Rectangle otherBBox = new Rectangle(x, y, w, h);
        return this.box.intersects(otherBBox);
    }

}
