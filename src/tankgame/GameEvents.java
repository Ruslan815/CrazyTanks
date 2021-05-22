package tankgame;

import java.awt.event.KeyEvent;
import java.util.Observable;

public class GameEvents extends Observable {
    int type;
    Object event;

    public void setValue(KeyEvent e) {
        type = 1;
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
