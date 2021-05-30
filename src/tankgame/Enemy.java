package tankgame;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Random;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Enemy {
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
    boolean isColliding = false;
    int lastDirection = 0;
    long lastChangedDirectionTime;
    long attemptsStartTime;

    Enemy(Image img, int speed, Random rand, final TankGame outer) {
        this.outer = outer;
        this.img = img;
        this.rand = rand;
        respawn();
        this.speed = speed;
        sizeX = img.getWidth(null);
        sizeY = img.getHeight(null);
        lastChangedDirectionTime = new Date().getTime();
    }

    public boolean collision(int x, int y, int w, int h) {
        bbox = new Rectangle(this.x, this.y, this.sizeX, this.sizeY);
        Rectangle otherBBox = new Rectangle(x, y, w, h);
        return this.bbox.intersects(otherBBox);
    }

    public void update() throws IOException, InterruptedException, MalformedURLException, LineUnavailableException, UnsupportedAudioFileException {
        if (isColliding) {
            isColliding = false;
            switch (lastDirection) { // Last direction
                case 0: // Up
                    lastDirection++;
                    x += speed;
                    break;
                case 1: // Right
                    lastDirection++;
                    y += speed;
                    break;
                case 2: // Down
                    lastDirection++;
                    x -= speed;
                    break;
                case 3: // Left
                    lastDirection = 0;
                    y -= speed;
                    break;
                default:
                    System.out.println("Unknown type of direction!");
                    System.exit(-1);
            }
        } else {
            long currTime = new Date().getTime();
            if (currTime - lastChangedDirectionTime > 4000 && rand.nextBoolean()) {
                lastChangedDirectionTime = currTime;
                lastDirection = Math.abs(rand.nextInt()) % 4;
            }
            switch (lastDirection) { // Current direction
                case 0: // Up
                    y -= speed;
                    break;
                case 1: // Right
                    x += speed;
                    break;
                case 2: // Down
                    y += speed;
                    break;
                case 3: // Left
                    x -= speed;
                    break;
                default:
                    System.out.println("Unknown type of direction!");
                    System.exit(-1);
            }
        }

        if (outer.tank1.collision(x, y, sizeX, sizeY)) {
            show = false;
            outer.tank1.health -= 25;
            outer.healthBar1.updateIncrement();
            if (outer.tank1.health > 0) outer.boom1.play();
            System.out.println("Player 1 health updated to: " + outer.tank1.health);
            if (outer.tank1.health == 0) {
                outer.tank1.isExploded = true;
                outer.boom1.play();
            }
            outer.explode1 = new Explosion("/resources/explosion1_", 6, this.x, this.y, outer);
            this.respawn();
        }
        if (outer.tank2.collision(x, y, sizeX, sizeY)) {
            show = false;
            outer.tank2.health -= 25;
            outer.healthBar2.updateIncrement();
            if (outer.tank2.health > 0) outer.boom1.play();
            System.out.println("Player 2 health updated to: " + outer.tank2.health);
            if (outer.tank2.health == 0) {
                outer.tank2.isExploded = true;
                outer.boom2.play();
            }
            outer.explode1 = new Explosion("/resources/explosion1_", 6, this.x, this.y, outer);
            this.respawn();
        }
        if (outer.newTempBullet != null) {
            if (this.collision(outer.newTempBullet.x, outer.newTempBullet.y, outer.newTempBullet.width, outer.newTempBullet.height)) {
                switch (outer.newTempBullet.getOwnedBy()) {
                    case "tank1":
                        this.show = false;
                        outer.score1 += 100;
                        outer.explode1 = new Explosion("/resources/explosion1_", 6, this.x, this.y, outer);
                        outer.boom1.play();
                        this.respawn();
                        break;
                    case "tank2":
                        this.show = false;
                        outer.score2 += 100;
                        outer.explode1 = new Explosion("/resources/explosion1_", 6, this.x, this.y, outer);
                        outer.boom1.play();
                        this.respawn();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void respawn() {
        int tempX = Math.abs(rand.nextInt()) % 1500 + 50;
        int tempY = Math.abs(rand.nextInt()) % 800 + 50;
        attemptsStartTime = new Date().getTime();
        while (isColliding(tempX, tempY)) {
            tempX = Math.abs(rand.nextInt()) % 1500 + 50;
            tempY = Math.abs(rand.nextInt()) % 800 + 50;
            long currTime = new Date().getTime();
            if (currTime - attemptsStartTime > 100) {
                this.show = false;
                this.x = -100;
                return;
            }
        }
        this.x = tempX;
        this.y = tempY;
        this.show = true;
    }

    public void draw(ImageObserver obs) {
        if (show) {
            outer.g2.drawImage(img, x, y, obs);
        }
    }

    public boolean isColliding(int tempX, int tempY) {
        boolean result = false;
        if (!outer.tank1.isExploded && outer.tank1.collision(tempX, tempY, sizeX, sizeY)) result = true;
        if (!outer.tank2.isExploded && outer.tank2.collision(tempX, tempY, sizeX, sizeY)) result = true;
        for (TankGame.Bullet tempBullet : outer.bulletsList) {
            if (tempBullet != null && tempBullet.show && tempBullet.collision(tempX, tempY, sizeX, sizeY)) result = true;
        }
        for (Wall[] tempWallArray : outer.layout)
            for (Wall tempWall : tempWallArray)
                if (tempWall != null && tempWall.blockName.equals("wall1") && tempWall.collision(tempX, tempY, sizeX, sizeY)) result = true;
        return result;
    }
}