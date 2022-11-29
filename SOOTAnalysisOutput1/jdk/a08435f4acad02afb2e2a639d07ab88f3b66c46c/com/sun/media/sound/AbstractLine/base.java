package com.sun.media.sound;

import java.util.Vector;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

abstract class AbstractLine implements Line {

    protected Line.Info info;

    protected Control[] controls;

    protected AbstractMixer mixer;

    private boolean open = false;

    private Vector listeners = new Vector();

    private static final EventDispatcher eventDispatcher;

    static {
        eventDispatcher = new EventDispatcher();
        eventDispatcher.start();
    }

    protected AbstractLine(Line.Info info, AbstractMixer mixer, Control[] controls) {
        if (controls == null) {
            controls = new Control[0];
        }
        this.info = info;
        this.mixer = mixer;
        this.controls = controls;
    }

    public Line.Info getLineInfo() {
        return info;
    }

    public boolean isOpen() {
        return open;
    }

    public void addLineListener(LineListener listener) {
        synchronized (listeners) {
            if (!(listeners.contains(listener))) {
                listeners.addElement(listener);
            }
        }
    }

    public void removeLineListener(LineListener listener) {
        listeners.removeElement(listener);
    }

    public Control[] getControls() {
        Control[] returnedArray = new Control[controls.length];
        for (int i = 0; i < controls.length; i++) {
            returnedArray[i] = controls[i];
        }
        return returnedArray;
    }

    public boolean isControlSupported(Control.Type controlType) {
        if (controlType == null) {
            return false;
        }
        for (int i = 0; i < controls.length; i++) {
            if (controlType == controls[i].getType()) {
                return true;
            }
        }
        return false;
    }

    public Control getControl(Control.Type controlType) {
        if (controlType != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controlType == controls[i].getType()) {
                    return controls[i];
                }
            }
        }
        throw new IllegalArgumentException("Unsupported control type: " + controlType);
    }

    protected void setOpen(boolean open) {
        if (Printer.trace)
            Printer.trace("> " + getClass().getName() + " (AbstractLine): setOpen(" + open + ")  this.open: " + this.open);
        boolean sendEvents = false;
        long position = getLongFramePosition();
        synchronized (this) {
            if (this.open != open) {
                this.open = open;
                sendEvents = true;
            }
        }
        if (sendEvents) {
            if (open) {
                sendEvents(new LineEvent(this, LineEvent.Type.OPEN, position));
            } else {
                sendEvents(new LineEvent(this, LineEvent.Type.CLOSE, position));
            }
        }
        if (Printer.trace)
            Printer.trace("< " + getClass().getName() + " (AbstractLine): setOpen(" + open + ")  this.open: " + this.open);
    }

    protected void sendEvents(LineEvent event) {
        eventDispatcher.sendAudioEvents(event, listeners);
    }

    public final int getFramePosition() {
        return (int) getLongFramePosition();
    }

    public long getLongFramePosition() {
        return AudioSystem.NOT_SPECIFIED;
    }

    protected AbstractMixer getMixer() {
        return mixer;
    }

    protected EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public abstract void open() throws LineUnavailableException;

    public abstract void close();
}
