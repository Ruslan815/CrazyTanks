package tankgame;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JApplet;
import javax.swing.JFrame;

public class TankGame extends JApplet implements Runnable {

    private Thread thread;
    Image miniMap, leftScreen, rightScreen;
    Image sea, bullet;
    Image playerOne, playerTwo;
    Image powerUp;
    private BufferedImage bimg1, bimg2;
    Graphics2D g2;
    int speed = 1, move = 0;
    int score1 = 0;
    int score2 = 0;
    int fireCounter1, fireCounter2, fireCounter3;
    Random generator = new Random(1234567);
    Wall[][] layout = new Wall[28][50];
    Tank m1, m2;
    Bullet fire;
    Explosion explode1, explode2;
    Sound backgroundMusic, boom1, boom2, gameOver;

    // declare HUD elements
    HUDelement healthBar1, healthBar2, healthLabel1, healthLabel2, score_1, score_2, scoreLabel1, scoreLabel2, game_over;

    ArrayList<Bullet> bulletsList = new <Bullet>ArrayList();
    int w = 1610, h = 930; // fixed size window game 
    Enemy e1;
    Enemy e2, e3;
    GameEvents gameEvents;

    @Override
    public void init() {

        setFocusable(true);
        setBackground(Color.white);
        Image wall1, wall2, island3, enemyOneImg, enemyTwoImg, enemyThreeImg;

        // Подключаем ресурсы
        try {
            String path = System.getProperty("user.dir");
            path = path + "/";

            //System.out.println(path);

            bullet = ImageIO.read(new File(path + "tankResources/Shell_basic_strip60/Shell_basic_16.png"));
            sea = ImageIO.read(new File(path + "tankResources/Background.png"));
            wall1 = ImageIO.read(new File(path + "tankResources/Wall1.png"));
            wall2 = ImageIO.read(new File(path + "tankResources/Wall2.png"));
            playerOne = ImageIO.read(new File(path + "tankResources/Tank_blue_basic_strip60/Tank_blue_basic_16.png"));
            playerTwo = ImageIO.read(new File(path + "tankResources/Tank_red_basic_strip60/Tank_red_basic_16.png"));
            powerUp = ImageIO.read(new File(path + "tankResources/Pickup_3.png"));

            healthBar1 = new HUDelement("/Resources/health", 5, 75, 820, this);
            healthBar2 = new HUDelement("/Resources/health", 5, 1450, 820, this);

            game_over = new HUDelement("/Resources/gameOver1", 1, 745, 80, this);

            File layoutFile = new File(path + "tankResources/map_layout");
            BufferedReader reader = new BufferedReader(new FileReader(layoutFile));
            String layoutStream;

            int i = 0;
            int j = 0;
            int index = 0;
            // Отрисовка карты из файла layout
            while ((layoutStream = reader.readLine()) != null) {
                while (index < layoutStream.length()) {
                    switch (layoutStream.charAt(index)) {
                        case '0':
                            layout[j][i] = null;
                            i++;
                            break;
                        case '1':
                            layout[j][i] = new Wall(wall1, i * 32, j * 32, true, null, this);
                            i++;
                            break;
                        case '2':
                            layout[j][i] = new Wall(wall2, i * 32, j * 32, false, null, this);
                            i++;
                            break;
                        case '3':
                            layout[j][i] = new Wall(powerUp, i * 32, j * 32, false, "health", this);
                            i++;
                        default:
                            break;
                    }
                    index++;
                }
                i = 0;
                index = 0;
                j++;
            }

            // Подключаем музыку
            try {
                backgroundMusic = new Sound("/tankResources/nirvana.wav", true);
                backgroundMusic.play();
                boom1 = new Sound("/Resources/snd_explosion1.wav", false);
                boom2 = new Sound("/Resources/snd_explosion2.wav", false);
            } catch (MalformedURLException | LineUnavailableException | UnsupportedAudioFileException ex) {
                Logger.getLogger(TankGame.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Init wall arrays
            m1 = new Tank(playerOne, 150, 400, 5, 90);
            m2 = new Tank(playerTwo, 1485, 400, 5, 270);
            gameEvents = new GameEvents();
            gameEvents.addObserver(m1);
            gameEvents.addObserver(m2);
            KeyControl key = new KeyControl(this);
            addKeyListener(key);
        } catch (IOException e) {
            System.out.print("No resources are found.");
        }

        System.out.println("Game Started.");
    }

    public class Bullet {

        Image img;
        int x, bx;
        int y, by;
        int height;
        int width;
        int power;
        double angle;
        Rectangle box;
        boolean show;
        String shootStyle;
        private String ownedBy;

        Bullet(Image img, int x, int y, int bx, int by, double angle) {
            this.img = img;
            this.x = x;
            this.y = y;
            this.bx = bx;
            this.by = by;
            this.angle = angle;
            power = 5;
            height = img.getHeight(null);
            width = img.getWidth(null);
            show = true;
        }

        public void draw(ImageObserver obs) {
            if (show) {
                AffineTransform old = g2.getTransform();
                g2.rotate(Math.toRadians(this.angle), this.x + (this.width / 2), this.y + (this.height / 2));
                g2.drawImage(this.img, this.x, this.y, obs);
                g2.setTransform(old);
            }
        }

        // Удаляем объект Bullet.
        public void update(int i) throws IOException {
            if (this.show) {
                this.x += this.bx;
                this.y -= this.by;
            } else if (!show) {
                this.x = this.bx;
                this.y = this.by;
                boom1.flush();
                boom1.play();
                bulletsList.remove(i);
            }

            // Удаляем объект Bullet когда он выходит за экран.
            if (this.y <= -20 || this.y > h + 20 || this.x < -20 || this.x > w + 20) { // h = 930, w = 1610
                show = false;
                bulletsList.remove(i);
                bulletsList.trimToSize();
            }

            // player 2 попал в player 1
            if (this.collision(m1.x, m1.y, m1.width, m1.height) && "m2".equals(this.getOwnedBy()) && this.show && this != null) {
                gameEvents.setValue("m1_collision");
                score2 += 50;
                explode1 = new Explosion("/Resources/explosion1_", 6, this.x, this.y, TankGame.this);
                show = false;
                bulletsList.remove(i);
                bulletsList.trimToSize();

            }

            // player 1 попал в player 2
            if (this.collision(m2.x, m2.y, m2.width, m2.height) && "m1".equals(this.getOwnedBy()) && this.show && this != null) {
                gameEvents.setValue("m2_collision");
                score1 += 50;
                explode1 = new Explosion("/Resources/explosion1_", 6, this.x, this.y, TankGame.this);
                show = false;
                bulletsList.remove(i);
                bulletsList.trimToSize();
            }
        }

        public boolean collision(int x, int y, int w, int h) {
            box = new Rectangle(this.x, this.y, this.width, this.height);
            Rectangle otherBox = new Rectangle(x, y, w, h);
            return this.box.intersects(otherBox);
        }

        public String getOwnedBy() {
            return ownedBy;
        }

        public void setOwnedBy(String planeID) {
            ownedBy = planeID;
        }
    }

    public class Tank implements Observer {

        Image img;
        int x, y, speed, width, height;
        double angle;
        protected int health = 100;
        Rectangle bbox;
        boolean isExploded;

        Tank(Image img, int x, int y, int speed, double angle) {
            this.img = img;
            this.x = x;
            this.y = y;
            this.speed = speed;
            width = img.getWidth(null);
            height = img.getHeight(null);
            isExploded = false;
            this.angle = angle;
        }

        public void draw(ImageObserver obs) {
            AffineTransform old = g2.getTransform();
            g2.rotate(Math.toRadians(this.angle), this.x + (this.width / 2), this.y + (this.height / 2));
            g2.drawImage(this.img, this.x, this.y, obs);
            g2.setTransform(old);
        }

        public boolean collision(int x, int y, int w, int h) {
            bbox = new Rectangle(this.x, this.y, this.width, this.height);
            Rectangle otherBox = new Rectangle(x, y, w, h);
            return this.bbox.intersects(otherBox);
        }

        @Override
        public void update(Observable obj, Object arg) {
            double rotation = Math.toRadians(15);
            GameEvents gameEvent = (GameEvents) arg;
            if (gameEvent.type == 1) {
                KeyEvent keyEvent = (KeyEvent) gameEvent.event;
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (this.equals(m2)) m2.angle -= 15;
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (this.equals(m2)) m2.angle += 15;
                        break;
                    case KeyEvent.VK_UP:
                        m2.y -= speed * Math.cos(Math.toRadians(m2.angle));
                        m2.x += speed * Math.sin(Math.toRadians(m2.angle));
                        break;
                    case KeyEvent.VK_DOWN:
                        m2.y += speed * Math.cos(Math.toRadians(m2.angle));
                        m2.x -= speed * Math.sin(Math.toRadians(m2.angle));
                        break;
                    case KeyEvent.VK_A:
                        if (this.equals(m1)) m1.angle -= 15;
                        break;
                    case KeyEvent.VK_D:
                        if (this.equals(m1)) m1.angle += 15;
                        break;
                    case KeyEvent.VK_W:
                        m1.y -= speed * Math.cos(Math.toRadians(m1.angle));
                        m1.x += speed * Math.sin(Math.toRadians(m1.angle));
                        break;
                    case KeyEvent.VK_S:
                        m1.y += speed * Math.cos(Math.toRadians(m1.angle));
                        m1.x -= speed * Math.sin(Math.toRadians(m1.angle));
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                    case KeyEvent.VK_SPACE:
                        if (this.equals(m1)) {
                            fire = new Bullet(bullet, m1.x + (m1.width / 2), m1.y + (m1.height / 2), (int) (m1.speed * Math.sin(Math.toRadians(m1.angle))),
                                    (int) (m1.speed * Math.cos(Math.toRadians(m1.angle))), m1.angle);
                            fire.setOwnedBy("m1");
                            bulletsList.add(fire);
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        if (this.equals(m2)) {
                            fire = new Bullet(bullet, m2.x + (m2.width / 2), m2.y + (m2.height / 2), (int) (m2.speed * Math.sin(Math.toRadians(m2.angle))),
                                    (int) (m2.speed * Math.cos(Math.toRadians(m2.angle))), m2.angle);
                            fire.setOwnedBy("m2");
                            bulletsList.add(fire);
                        }
                        break;
                    default:
                        break;
                }
            } else if (gameEvent.type == 2) {
                String msg = (String) gameEvent.event;
                if (msg.equals("m1_collision") && this.equals(m1)) {
                    m1.health -= 25;
                    healthBar1.update();
                    if (health > 0) boom1.play();
                    System.out.println("Player 1 health updated to: " + m1.health);
                    if (m1.health == 0) {
                        m1.isExploded = true;
                        boom2.play();
                    }
                }
                if (msg.equals("m2_collision") && this.equals(m2)) {
                    m2.health -= 25;
                    healthBar2.update();
                    if (health > 0) boom1.play();
                    System.out.println("Player 2 health updated to: " + m2.health);
                    if (m2.health == 0) {
                        m2.isExploded = true;
                        boom2.play();
                    }
                }
            }
        }
    }

    public void drawBackGroundWithTileImage() {
        int TileWidth = sea.getWidth(this);
        int TileHeight = sea.getHeight(this);

        int NumberX = (int) (w / TileWidth);
        int NumberY = (int) (h / TileHeight);

        for (int i = -1; i <= NumberY; i++) {
            for (int j = 0; j <= NumberX; j++) {
                g2.drawImage(sea, j * TileWidth,
                        i * TileHeight + (move % TileHeight), TileWidth,
                        TileHeight, this);
            }
        }
    }

    public void drawDemo() throws IOException, InterruptedException, MalformedURLException, LineUnavailableException, UnsupportedAudioFileException, AWTException {

        drawBackGroundWithTileImage();

        for (Wall[] layout1 : layout) {
            for (Wall layout11 : layout1) {
                if (layout11 != null) {
                    layout11.update();
                }
            }
        }

        // update bullet locations
        for (int i = 0; i < bulletsList.size(); i++) {
            bulletsList.get(i).update(i);
        }

        // update explosion frames to advance animation
        if (explode1 != null && explode1.frameCount < explode1.numFrames) {
            explode1.update();
        }

        for (Wall[] layout1 : layout) {
            for (Wall layout11 : layout1) {
                if (layout11 != null) {
                    layout11.draw(this);
                }
            }
        }

        // draw planes while health > 0
        if (!m1.isExploded) {
            m1.draw(this);
        }

        if (!m2.isExploded) {
            m2.draw(this);
        }

        // draw bullets
        bulletsList.stream().forEach((Bullet clip1) -> {
            clip1.draw(this);
        });

        // remove planes from screen upon death
        if (m1.isExploded) {
            explode2 = new Explosion("/Resources/explosion2_", 7, m1.x, m1.y, this);
            m1.y = -100;

        }
        if (m2.isExploded) {
            explode2 = new Explosion("/Resources/explosion2_", 7, m2.x, m2.y, this);
            m2.y = -100;
        }

        // draw explosion frame
        if (explode1 != null && explode1.frameCount < explode1.numFrames) {
            //System.out.println("Draw explosion.");
            explode1.draw(this);
        }
        if (explode2 != null && explode2.frameCount < explode2.numFrames) {
            //System.out.println("Draw explosion.");
            explode2.draw(this);
        }

        // set screen limits
        int x1, y1, x2, y2, width, height;
        x1 = m1.x - 120;
        y1 = m1.y - 300;
        x2 = m2.x - 120;
        y2 = m2.y - 300;
        width = 500;
        height = 600;
        if (x1 < 0) {
            x1 = 0;
        }
        if (x2 < 0) {
            x2 = 0;
        }
        if (y1 < 0) {
            y1 = 0;
        }
        if (y2 < 0) {
            y2 = 0;
        }
        if (x1 + width > 1590) {
            x1 = 1095;
        }
        if (x2 + width > 1590) {
            x2 = 1095;
        }
        //System.out.println("x: " + x + ", full: " + (x + width));
        if (y1 + height > 900) {
            y1 = 300;
        }
        if (y2 + height > 900) {
            y2 = 300;
        }
        //System.out.println("y + height: " + (y + height));

        // draw screen portions
        leftScreen = bimg1.getSubimage(x1, y1, width, height);
        rightScreen = bimg1.getSubimage(x2, y2, width, height);

        leftScreen = leftScreen.getScaledInstance(800, 900, Image.SCALE_FAST);
        rightScreen = rightScreen.getScaledInstance(800, 900, Image.SCALE_FAST);
        miniMap = bimg1.getScaledInstance(400, 225, Image.SCALE_FAST);

        // render single frame from parts
        BufferedImage display = new BufferedImage(this.w, this.h, BufferedImage.TYPE_INT_RGB);
        Graphics temp = display.getGraphics();

        temp.drawImage(leftScreen, 0, 0, null);
        temp.drawImage(rightScreen, 800, 0, null);
        temp.drawImage(miniMap, 600, 660, null);

        g2.drawImage(leftScreen, 0, 0, this);
        g2.drawImage(rightScreen, 800, 0, this);
        g2.drawImage(miniMap, 600, 660, this);

        // draw health bars
        healthBar1.draw(this);
        healthBar2.draw(this);

        // if a player dies, display game over logo and final score
        if (m1.isExploded || m2.isExploded) {
            m1.isExploded = true;
            m2.isExploded = true;
            game_over.draw(this);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            g2.drawString("FINAL SCORE", 800, 125);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.drawString("Player 1", 750, 150);
            g2.drawString(Integer.toString(score1), 960, 150);
            g2.drawString("Player 2", 750, 180);
            g2.drawString(Integer.toString(score2), 960, 180);

        }
    }

    @Override
    public void paint(Graphics g) {
        if (bimg1 == null) {
            Dimension windowSize = getSize();
            bimg1 = (BufferedImage) createImage(windowSize.width,
                    windowSize.height);
            g2 = bimg1.createGraphics();

        }
        try {
            drawDemo();
        } catch (IOException | InterruptedException | LineUnavailableException | UnsupportedAudioFileException | AWTException ex) {
            Logger.getLogger(TankGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        g.drawImage(bimg1, 0, 0, this);
    }

    @Override
    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

    }

    @Override
    public void run() {

        Thread me = Thread.currentThread();
        while (thread == me) {
            repaint();
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                break;
            }

        }
    }

    public static void main(String argv[]) {
        final TankGame demo = new TankGame();
        demo.init();
        JFrame f = new JFrame("TankGame2");
        f.addWindowListener(new WindowAdapter() {
        });
        f.getContentPane().add("Center", demo);
        f.pack();
        f.setSize(new Dimension(1610, 930));
        f.setVisible(true);
        f.setResizable(false);
        demo.start();
    }

}
