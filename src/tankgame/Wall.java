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
        if (outer.tank1.collision(x, y, sizeX, sizeY)) {
            if (this.blockName != null && this.blockName.equals("health")) {
                if (outer.tank1.health <= 100) {
                    outer.tank1.health += 25;
                    outer.healthBar1.updateDecrement();
                }
                this.show = false;
                this.y = -100;
            } else {
                show = true;
                outer.tank1.speed = -5;
                if (outer.tank1.x < this.x - 5) {
                    outer.tank1.x -= 5;
                } else {
                    outer.tank1.x += 5;
                }
                if (outer.tank1.y < this.y - 5) {
                    outer.tank1.y -= 5;
                } else {
                    outer.tank1.y += 5;
                }
                outer.tank1.speed = 5;
            }
        }
        if (outer.tank2.collision(x, y, sizeX, sizeY)) {
            if (this.blockName != null && this.blockName.equals("health")) {
                if (outer.tank2.health <= 100) {
                    outer.tank2.health += 25;
                    outer.healthBar2.updateDecrement();
                }
                this.show = false;
                this.y = -100;
            } else {
                show = true;
                outer.tank2.speed = -5;
                if (outer.tank2.x < this.x - 5) {
                    outer.tank2.x -= 5;
                } else {
                    outer.tank2.x += 5;
                }
                if (outer.tank2.y < this.y - 5) {
                    outer.tank2.y -= 5;
                } else {
                    outer.tank2.y += 5;
                }
                outer.tank2.speed = 5;
            }
        }

        for (Enemy tempEnemy: outer.enemies) {
            if (tempEnemy.collision(x, y, sizeX, sizeY) && !this.blockName.equals("health")) {
                show = true;
                tempEnemy.isColliding = true;
                tempEnemy.speed = -2;
                if (tempEnemy.x < this.x - 1) {
                    tempEnemy.x -= 1;
                } else {
                    tempEnemy.x += 1;
                }
                if (tempEnemy.y < this.y - 1) {
                    tempEnemy.y -= 1;
                } else {
                    tempEnemy.y += 1;
                }
                tempEnemy.speed = 2;
            }
        }

        for (TankGame.Bullet tempBullet : outer.bulletsList) {
            if (tempBullet != null) {
                if (this.collision(tempBullet.x, tempBullet.y, tempBullet.width, tempBullet.height) && !this.blockName.equals("health")) {
                    String bulletOwner = tempBullet.getOwnedBy();
                    if (bulletOwner.equals("tank1") || bulletOwner.equals("tank2")) {
                        tempBullet.show = false;
                        outer.explode1 = new Explosion("/resources/explosion1_", 6, tempBullet.x, tempBullet.y, outer);
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
