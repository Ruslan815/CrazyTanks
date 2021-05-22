package tankgame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyControl extends KeyAdapter {
    private final TankGame outer;

    public KeyControl(final TankGame outer) {
        this.outer = outer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        outer.gameEvents.setValue(e);
    }
}