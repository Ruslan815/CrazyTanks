package tankgame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class TankGame extends JPanel implements Runnable {

    private Thread thread;
    Image miniMap, leftScreen, rightScreen;
    Image backgroundImage, bullet;
    Image playerOne, playerTwo;
    Image bonusItem;
    private BufferedImage bufferedImg1;
    Graphics2D g2;
    int speed = 1, move = 0;
    int score1 = 0;
    int score2 = 0;
    static int selectedMapNumber = 0;
    static JFrame currentJFrame, currentMenu;
    static JPanel currentMainGame;
    int fireCounter1, fireCounter2, fireCounter3;
    Random rand = new Random(777);
    Wall[][] layout = new Wall[28][50];
    Tank tank1, tank2; //TODO , tank777;
    Bullet newTempBullet;
    Explosion explode1, explode2;
    Sound backgroundMusic, boom1, boom2, gameOver;

    // declare HUD elements
    HUDelement healthBar1, healthBar2, healthLabel1, healthLabel2, score_1, score_2, scoreLabel1, scoreLabel2, game_over;

    ArrayList<Bullet> bulletsList = new ArrayList<Bullet>();
    int w = 1610, h = 930; // fixed size window game 
    Enemy e1;
    Enemy e2, e3;
    GameEvents gameEvents;

    public void init() {

        setFocusable(true);
        setBackground(Color.white);
        Image wall1, wall2, island3, enemyOneImg, enemyTwoImg, enemyThreeImg;

        // Подключаем ресурсы
        try {
            String path = System.getProperty("user.dir");
            path = path + "/";

            bullet = ImageIO.read(new File(path + "resources/Bullet60/Shell_basic_16.png"));
            backgroundImage = ImageIO.read(new File(path + "resources/Background.png"));
            wall1 = ImageIO.read(new File(path + "resources/Wall1.png"));
            wall2 = ImageIO.read(new File(path + "resources/Wall2.png"));
            playerOne = ImageIO.read(new File(path + "resources/TankBlueBasic60/Tank_blue_basic_16.png"));
            playerTwo = ImageIO.read(new File(path + "resources/TankRedBasic60/Tank_red_basic_16.png"));
            bonusItem = ImageIO.read(new File(path + "resources/BonusItem.png"));

            healthBar1 = new HUDelement("/resources/health", 6, 75, 820, this);
            healthBar2 = new HUDelement("/resources/health", 6, 1450, 820, this);
            healthBar1.updateIncrement();
            healthBar2.updateIncrement();

            game_over = new HUDelement("/resources/GameOver", 1, 745, 80, this);

            File layoutFile = new File(path + "resources/map_layout1");
            if (TankGame.selectedMapNumber == 0) {
                System.exit(0);
            } else {
                layoutFile = new File(path + "resources/map_layout" + TankGame.selectedMapNumber);
            }
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
                            layout[j][i] = new Wall(wall1, i * 32, j * 32, true, "wall1", this);
                            i++;
                            break;
                        case '2':
                            layout[j][i] = new Wall(wall2, i * 32, j * 32, false, "wall2", this);
                            i++;
                            break;
                        case '3':
                            layout[j][i] = new Wall(bonusItem, i * 32, j * 32, false, "health", this);
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
                backgroundMusic = new Sound("/resources/nirvana.wav", true);
                backgroundMusic.play();
                boom1 = new Sound("/resources/SoundExplosion1.wav", false);
                boom2 = new Sound("/resources/SoundExplosion2.wav", false);
            } catch (MalformedURLException | LineUnavailableException | UnsupportedAudioFileException ex) {
                Logger.getLogger(TankGame.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Init observers
            tank1 = new Tank(playerOne, 150, 400, 5, 90);
            tank2 = new Tank(playerTwo, 1485, 400, 5, 270);
            //TODO tank777 = new Tank(playerOne, 150, 600, 5, 0);
            gameEvents = new GameEvents();
            gameEvents.addObserver(tank1);
            gameEvents.addObserver(tank2);
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

        public void update(int i) throws IOException {
            // Удаляем объект Bullet.
            if (this.show) {
                this.x += this.bx;
                this.y -= this.by;
            } else {
                this.x = this.bx;
                this.y = this.by;
                boom1.flush();
                boom1.play();
                bulletsList.remove(i);
                bulletsList.trimToSize();
            }

            // Удаляем объект Bullet когда он выходит за экран.
            if (this.y <= -20 || this.y > h + 20 || this.x < -20 || this.x > w + 20) { // h = 930, w = 1610
                show = false;
                bulletsList.remove(i);
                bulletsList.trimToSize();
            }

            // player 2 попал в player 1
            if (this.collision(tank1.x, tank1.y, tank1.width, tank1.height) && "tank2".equals(this.getOwnedBy()) && this.show) {
                gameEvents.setValue("tank2_hit_tank1");
                score2 += 50;
                explode1 = new Explosion("/resources/explosion1_", 6, this.x, this.y, TankGame.this);
                show = false;
                bulletsList.remove(i);
                bulletsList.trimToSize();
            }

            // player 1 попал в player 2
            if (this.collision(tank2.x, tank2.y, tank2.width, tank2.height) && "tank1".equals(this.getOwnedBy()) && this.show) {
                gameEvents.setValue("tank1_hit_tank2");
                score1 += 50;
                explode1 = new Explosion("/resources/explosion1_", 6, this.x, this.y, TankGame.this);
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

        public void setOwnedBy(String ownerID) {
            ownedBy = ownerID;
        }
    }

    public class Tank implements Observer {

        Image img;
        int x, y, speed, width, height;
        double angle;
        protected int health = 100;
        Rectangle bbox;
        boolean isExploded;
        boolean[] pressedKeys; // Left, Right, Up, Down, Shoot
        long lastShot; // One shot in 500 ms

        Tank(Image img, int x, int y, int speed, double angle) {
            this.img = img;
            this.x = x;
            this.y = y;
            this.speed = speed;
            width = img.getWidth(null);
            height = img.getHeight(null);
            isExploded = false;
            this.angle = angle;
            this.pressedKeys = new boolean[5];
            Arrays.fill(this.pressedKeys, false);
            this.lastShot = new Date().getTime();
        }

        public void draw(ImageObserver obs) {
            this.moveTank();
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

        public void moveTank() {
            if (this.pressedKeys[0]) this.angle -= 5;
            if (this.pressedKeys[1]) this.angle += 5;

            if (this.pressedKeys[2]) {
                int dx = 0;
                int dy = 0;
                dy -= this.speed * Math.cos(Math.toRadians(this.angle));
                dx += this.speed * Math.sin(Math.toRadians(this.angle));
                if (this.equals(tank1) && !tank2.collision(this.x + dx, this.y + dy, this.width, this.height)) {
                    this.y += dy;
                    this.x += dx;
                } else if (this.equals(tank2) && !tank1.collision(this.x + dx, this.y + dy, this.width, this.height)) {
                    this.y += dy;
                    this.x += dx;
                }
            }
            if (this.pressedKeys[3]) {
                int dx = 0;
                int dy = 0;
                dy += this.speed * Math.cos(Math.toRadians(this.angle));
                dx -= this.speed * Math.sin(Math.toRadians(this.angle));
                if (this.equals(tank1) && !tank2.collision(this.x + dx, this.y + dy, this.width, this.height)) {
                    this.y += dy;
                    this.x += dx;
                } else if (this.equals(tank2) && !tank1.collision(this.x + dx, this.y + dy, this.width, this.height)) {
                    this.y += dy;
                    this.x += dx;
                }
            }

            if (this.pressedKeys[4]) {
                long currTime = new Date().getTime();
                if (this.equals(tank1) && (currTime - tank1.lastShot) < 500) {
                    return;
                } else if (this.equals(tank2) && (currTime - tank2.lastShot) < 500) {
                    return;
                }

                newTempBullet = new Bullet(bullet, this.x + (this.width / 2), this.y + (this.height / 2), (int) (this.speed * Math.sin(Math.toRadians(this.angle))),
                        (int) (this.speed * Math.cos(Math.toRadians(this.angle))), this.angle);

                if (this.equals(tank1)) {
                    tank1.lastShot = currTime;
                    newTempBullet.setOwnedBy("tank1");
                } else if (this.equals(tank2)) {
                    tank2.lastShot = currTime;
                    newTempBullet.setOwnedBy("tank2");
                }
                bulletsList.add(newTempBullet);
            }
        }

        @Override
        public void update(Observable obj, Object arg) {
            GameEvents gameEvent = (GameEvents) arg;
            if (gameEvent.type == 1) {
                KeyEvent keyEvent = (KeyEvent) gameEvent.event;
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (this.equals(tank2)) tank2.pressedKeys[0] = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (this.equals(tank2)) tank2.pressedKeys[1] = true;
                        break;
                    case KeyEvent.VK_UP:
                        if (this.equals(tank2)) tank2.pressedKeys[2] = true;
                        break;
                    case KeyEvent.VK_DOWN:
                        if (this.equals(tank2)) tank2.pressedKeys[3] = true;
                        break;
                    case KeyEvent.VK_A:
                        if (this.equals(tank1)) tank1.pressedKeys[0] = true;
                        break;
                    case KeyEvent.VK_D:
                        if (this.equals(tank1)) tank1.pressedKeys[1] = true;
                        break;
                    case KeyEvent.VK_W:
                        if (this.equals(tank1)) tank1.pressedKeys[2] = true;
                        break;
                    case KeyEvent.VK_S:
                        if (this.equals(tank1)) tank1.pressedKeys[3] = true;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        backgroundMusic.stop();
                        TankGame.currentJFrame.remove(currentMainGame);
                        TankGame.currentJFrame.dispose();
                        System.gc();
                        currentMenu.setVisible(true);
                        break;
                    case KeyEvent.VK_SPACE:
                        if (this.equals(tank1)) tank1.pressedKeys[4] = true;
                        break;
                    case KeyEvent.VK_ENTER:
                        if (this.equals(tank2)) tank2.pressedKeys[4] = true;
                        break;
                    default:
                        break;
                }
            } else if (gameEvent.type == 2) {
                String msg = (String) gameEvent.event;
                if (msg.equals("tank2_hit_tank1") && this.equals(tank1)) {
                    tank1.health -= 25;
                    healthBar1.updateIncrement();
                    if (health > 0) boom1.play();
                    System.out.println("Player 1 health updated to: " + tank1.health);
                    if (tank1.health == 0) {
                        tank1.isExploded = true;
                        boom2.play();
                    }
                }
                if (msg.equals("tank1_hit_tank2") && this.equals(tank2)) {
                    tank2.health -= 25;
                    healthBar2.updateIncrement();
                    if (health > 0) boom1.play();
                    System.out.println("Player 2 health updated to: " + tank2.health);
                    if (tank2.health == 0) {
                        tank2.isExploded = true;
                        boom2.play();
                    }
                }
            } else if (gameEvent.type == 3) {
                KeyEvent keyEvent = (KeyEvent) gameEvent.event;
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (this.equals(tank2)) tank2.pressedKeys[0] = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (this.equals(tank2)) tank2.pressedKeys[1] = false;
                        break;
                    case KeyEvent.VK_UP:
                        if (this.equals(tank2)) tank2.pressedKeys[2] = false;
                        break;
                    case KeyEvent.VK_DOWN:
                        if (this.equals(tank2)) tank2.pressedKeys[3] = false;
                        break;
                    case KeyEvent.VK_A:
                        if (this.equals(tank1)) tank1.pressedKeys[0] = false;
                        break;
                    case KeyEvent.VK_D:
                        if (this.equals(tank1)) tank1.pressedKeys[1] = false;
                        break;
                    case KeyEvent.VK_W:
                        if (this.equals(tank1)) tank1.pressedKeys[2] = false;
                        break;
                    case KeyEvent.VK_S:
                        if (this.equals(tank1)) tank1.pressedKeys[3] = false;
                        break;
                    case KeyEvent.VK_SPACE:
                        if (this.equals(tank1)) tank1.pressedKeys[4] = false;
                        break;
                    case KeyEvent.VK_ENTER:
                        if (this.equals(tank2)) tank2.pressedKeys[4] = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // TODO Проверить правильность индексации
    public void drawBackGroundImage() {
        int TileWidth = backgroundImage.getWidth(this);
        int TileHeight = backgroundImage.getHeight(this);

        int NumberX = w / TileWidth;
        int NumberY = h / TileHeight;

        for (int i = -1; i <= NumberY; i++) {
            for (int j = 0; j <= NumberX; j++) {
                g2.drawImage(backgroundImage, j * TileWidth, i * TileHeight, TileWidth, TileHeight, this); // third param y: i * TileHeight + (move % TileHeight)
            }
        }
    }

    public void drawScene() throws IOException, InterruptedException, MalformedURLException, LineUnavailableException, UnsupportedAudioFileException, AWTException {
        drawBackGroundImage();
        for (Wall[] tempWallArray : layout)
            for (Wall tempWall : tempWallArray)
                if (tempWall != null) tempWall.update();

        // update bullet locations
        for (int i = 0; i < bulletsList.size(); i++)
            bulletsList.get(i).update(i);

        // update explosion frames to advance animation
        if (explode1 != null && explode1.frameNumber < explode1.framesCount) explode1.updateIncrement();
        if (explode2 != null && explode2.frameNumber < explode2.framesCount) explode2.updateIncrement();

        for (Wall[] tempWallArray : layout)
            for (Wall tempWall : tempWallArray)
                if (tempWall != null) tempWall.draw(this);

        // draw tanks while health > 0
        if (!tank1.isExploded) tank1.draw(this);
        if (!tank2.isExploded) tank2.draw(this);
        //TODO if (!tank777.isExploded) tank777.draw(this);

        // draw bullets
        for (Bullet tempBullet : bulletsList)
            tempBullet.draw(this);

        // remove tanks from screen upon death
        if (tank1.isExploded) {
            explode2 = new Explosion("/resources/explosion2_", 7, tank1.x, tank1.y, this);
            tank1.y = -100;
        }
        if (tank2.isExploded) {
            explode2 = new Explosion("/resources/explosion2_", 7, tank2.x, tank2.y, this);
            tank2.y = -100;
        }

        // draw explosion frame
        if (explode1 != null && explode1.frameNumber < explode1.framesCount) {
            explode1.draw(this);
        }
        if (explode2 != null && explode2.frameNumber < explode2.framesCount) {
            explode2.draw(this);
        }

        // Вычисление границ двух экранов игроков
        int x1, y1, x2, y2, width, height;
        x1 = tank1.x - 120;
        y1 = tank1.y - 300;
        x2 = tank2.x - 120;
        y2 = tank2.y - 300;
        width = 500;
        height = 600;
        if (x1 < 0) x1 = 0;
        if (x2 < 0) x2 = 0;
        if (y1 < 0) y1 = 0;
        if (y2 < 0) y2 = 0;
        if (x1 + width > 1590) x1 = 1095;
        if (x2 + width > 1590) x2 = 1095;
        if (y1 + height > 900) y1 = 300;
        if (y2 + height > 900) y2 = 300;

        // draw screen portions
        try {
            leftScreen = bufferedImg1.getSubimage(x1, y1, width, height);
            rightScreen = bufferedImg1.getSubimage(x2, y2, width, height);
        } catch (Exception e) {
            System.out.println("BufferedImage Exception!");
            System.exit(0);
        }

        leftScreen = leftScreen.getScaledInstance(800, 900, Image.SCALE_FAST);
        rightScreen = rightScreen.getScaledInstance(800, 900, Image.SCALE_FAST);
        miniMap = bufferedImg1.getScaledInstance(400, 225, Image.SCALE_FAST);

        // render single frame from parts (двойная буферизация)
        BufferedImage display = new BufferedImage(this.w, this.h, BufferedImage.TYPE_INT_RGB);
        Graphics tempGraphics = display.getGraphics();

        tempGraphics.drawImage(leftScreen, 0, 0, null);
        tempGraphics.drawImage(rightScreen, 800, 0, null);
        tempGraphics.drawImage(miniMap, 600, 660, null);

        g2.drawImage(leftScreen, 0, 0, this);
        g2.drawImage(rightScreen, 800, 0, this);
        g2.drawImage(miniMap, 600, 660, this);

        // draw health bars
        healthBar1.draw(this);
        healthBar2.draw(this);

        // game over logo and final score
        if (tank1.isExploded || tank2.isExploded) {
            tank1.isExploded = true;
            tank2.isExploded = true;
            tank1.speed = 0;
            tank2.speed = 0;
            game_over.draw(this);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.setColor(Color.CYAN);
            g2.drawString("ОЧКИ", 850, 125);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.drawString("Игрок 1", 750, 150);
            g2.drawString(Integer.toString(score1), 960, 150);
            g2.drawString("Игрок 2", 750, 180);
            g2.drawString(Integer.toString(score2), 960, 180);
        }
    }

    @Override
    public void paint(Graphics tempGraphics) {
        if (bufferedImg1 == null) {
            Dimension windowSize = getSize();
            bufferedImg1 = (BufferedImage) createImage(windowSize.width, windowSize.height);
            g2 = bufferedImg1.createGraphics();
        }

        try {
            drawScene();
        } catch (IOException | InterruptedException | LineUnavailableException | UnsupportedAudioFileException | AWTException ex) {
            Logger.getLogger(TankGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Двойная буферизация
        tempGraphics.drawImage(bufferedImg1, 0, 0, this);
    }

    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    @Override
    public void run() {
        Thread currThread = Thread.currentThread();
        while (thread == currThread) {
            repaint();
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static void startMainFrame() {
        // Main Frame
        final TankGame mainGame = new TankGame();
        mainGame.init();
        JFrame mainJFrame = new JFrame("CrazyTanks");
        TankGame.currentJFrame = mainJFrame;
        TankGame.currentMainGame = mainGame;
        ImageIcon img = new ImageIcon(System.getProperty("user.dir") + "/resources/BonusItem.png");
        mainJFrame.setIconImage(img.getImage());
        mainJFrame.addWindowListener(new WindowAdapter() {});
        mainJFrame.getContentPane().add("Center", mainGame);
        mainJFrame.pack();
        mainJFrame.setSize(new Dimension(1610, 930));
        mainJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(() -> mainJFrame.setVisible(true));
        mainJFrame.setResizable(false);
        mainJFrame.setLocationRelativeTo(null); // Окно по центру экрана
        mainGame.start();
    }

    public static void buttonActionPerformed(ActionEvent evt) {
        switch (evt.getActionCommand()) {
            case "Карта №1":
                TankGame.selectedMapNumber = 1;
                startMainFrame();
                currentMenu.setVisible(false);
                break;
            case "Карта №2":
                TankGame.selectedMapNumber = 2;
                startMainFrame();
                currentMenu.setVisible(false);
                break;
            case "Карта №3":
                TankGame.selectedMapNumber = 3;
                startMainFrame();
                currentMenu.setVisible(false);
                break;
            case "Выйти":
                System.exit(0);
                break;
        }
    }

    public static void startMenuFrame() {
        // Menu Frame
        JFrame menuJFrame = new JFrame("CrazyTanks");
        TankGame.currentMenu = menuJFrame;
        menuJFrame.setSize(new Dimension(250, 250));
        menuJFrame.setLocationRelativeTo(null);
        menuJFrame.setResizable(false);
        menuJFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        ImageIcon img = new ImageIcon(System.getProperty("user.dir") + "/resources/BonusItem.png");
        menuJFrame.setIconImage(img.getImage());

        JLabel menuLabel = new JLabel("ГЛАВНОЕ МЕНЮ");
        JLabel mapLabel = new JLabel("Выберите карту для игры:");
        JLabel emptyLabel1 = new JLabel("\n");
        JLabel emptyLabel2 = new JLabel("\n");
        JButton buttonMap1 = new JButton("Карта №1");
        JButton buttonMap2 = new JButton("Карта №2");
        JButton buttonMap3 = new JButton("Карта №3");
        JButton buttonExit = new JButton("Выйти");
        menuJFrame.setLayout(new BoxLayout(menuJFrame.getContentPane(), BoxLayout.Y_AXIS));
        menuJFrame.getContentPane().setBackground(new Color(110, 29, 255));

        menuJFrame.add(menuLabel);
        menuJFrame.add(emptyLabel1);
        menuJFrame.add(mapLabel);
        menuJFrame.add(buttonMap1);
        menuJFrame.add(buttonMap2);
        menuJFrame.add(buttonMap3);
        menuJFrame.add(emptyLabel2);
        menuJFrame.add(buttonExit);

        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mapLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonMap1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonMap2.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonMap3.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonExit.setAlignmentX(Component.CENTER_ALIGNMENT);

        mapLabel.setForeground(new Color(0, 180, 159));
        menuLabel.setForeground(new Color(0, 180, 159));
        buttonMap1.setBackground(new Color(0, 170, 0));
        buttonMap2.setBackground(new Color(248, 170, 0));
        buttonMap3.setBackground(new Color(230, 13, 12));
        buttonMap1.setForeground(new Color(73, 29, 167));
        buttonMap2.setForeground(new Color(73, 29, 167));
        buttonMap3.setForeground(new Color(73, 29, 167));

        mapLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        menuLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        buttonMap1.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        buttonMap2.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        buttonMap3.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        buttonExit.setFont(new java.awt.Font("Arial", Font.BOLD, 14));

        buttonMap1.addActionListener(TankGame::buttonActionPerformed);
        buttonMap2.addActionListener(TankGame::buttonActionPerformed);
        buttonMap3.addActionListener(TankGame::buttonActionPerformed);
        buttonExit.addActionListener(TankGame::buttonActionPerformed);

        SwingUtilities.invokeLater(() -> menuJFrame.setVisible(true));
    }

    public static void main(String[] argv) {
        startMenuFrame();
    }
}
