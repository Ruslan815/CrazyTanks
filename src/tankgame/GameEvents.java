package tankgame;

import java.awt.event.KeyEvent;
import java.util.Observable;

public class GameEvents extends Observable {
    int type;
    Object event;

    public void setValue(KeyEvent e, boolean isPressed) {
        if (isPressed) {
            type = 1;
        } else {
            type = 3;
        }
        event = e;
        setChanged();
        notifyObservers(this);
    }

    public void setValue(String msg) {
        type = 2;
        event = msg;
        setChanged();
        notifyObservers(this);
    }
}