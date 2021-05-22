package tankgame;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Enemy {/*
    Image img;
    int x;
    int y;
    int sizeX;
    int sizeY;
    int speed;
    Random rand;
    Rectangle bbox;
    boolean show;
    private final TankGame outer;

    Enemy(Image img, int speed, Random rand, final TankGame outer) {
        this.outer = outer;
        this.img = img;
        this.x = Math.abs(rand.nextInt() % (600 - 30));
        this.y = 120;
        this.speed = speed;
        this.rand = rand;
        this.show = true;
        sizeX = img.getWidth(null);
        sizeY = img.getHeight(null);
        System.out.println("w:" + sizeX + " y:" + sizeY);
    }

    public boolean collision(int x, int y, int w, int h) {
        bbox = new Rectangle(this.x, this.y, this.sizeX, this.sizeY);
        Rectangle otherBBox = new Rectangle(x, y, w, h);
        return this.bbox.intersects(otherBBox);
    }

    public Enemy getInstance() {
        return this;
    }

    public void update() throws IOException, InterruptedException, MalformedURLException, LineUnavailableException, UnsupportedAudioFileException {
        y += speed;
        if (outer.m1.collision(x, y, sizeX, sizeY)) {
            show = false;
            // You need to remove this one and increase score etc
            outer.gameEvents.setValue("enemy");
            outer.score1 += 50;
            outer.explode1 = new Explosion("/Resources/explosion1_", 6, this.x, this.y, outer);
            this.reset();
            show = true;
        }
        if (outer.m2.collision(x, y, sizeX, sizeY)) {
            show = false;
            // You need to remove this one and increase score etc
            outer.gameEvents.setValue("enemy");
            outer.score2 += 50;
            outer.explode1 = new Explosion("/Resources/explosion1_", 6, this.x, this.y, outer);
            this.reset();
            show = true;
        }
        if (outer.fire != null) {
            if (this.collision(outer.fire.x, outer.fire.y, outer.fire.width, outer.fire.height)) {
                switch (outer.fire.getOwnedBy()) {
                    case "m1":
                        this.show = false;
                        outer.score1 += 100;
                        outer.explode1 = new Explosion("/Resources/explosion1_", 6, this.x, this.y, outer);
                        outer.boom1.play();
                        this.reset();
                        break;
                    case "m2":
                        this.show = false;
                        outer.score2 += 100;
                        outer.explode1 = new Explosion("/Resources/explosion1_", 6, this.x, this.y, outer);
                        outer.boom1.play();
                        this.reset();
                        break;
                    default:
                        break;
                }
                show = true;
            }
        }
        if (this.y >= 480) {
            this.reset();
        } else {
            outer.gameEvents.setValue("");
        }
    }

    public void reset() {
        this.x = Math.abs(outer.generator.nextInt() % (600 - 30));
        this.y = -10;
    }

    public void draw(ImageObserver obs) {
        if (show) {
            outer.g2.drawImage(img, x, y, obs);
        }
    }
*/}