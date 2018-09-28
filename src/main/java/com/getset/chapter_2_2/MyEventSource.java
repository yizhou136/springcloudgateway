package com.getset.chapter_2_2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyEventSource {

    private List<MyEventListener> listeners;

    public MyEventSource() {
        this.listeners = new ArrayList<>();
    }

    public void register(MyEventListener listener) {
        listeners.add(listener);
    }

    public void newEvent(MyEvent event) {
        for (MyEventListener listener :
                listeners) {
            listener.onNewEvent(event);
        }
    }

    public void eventStopped() {
        for (MyEventListener listener :
                listeners) {
            listener.onEventStopped();
        }
    }

    public static class MyEvent {
        private Date timeStemp;
        private String message;

        public MyEvent(Date timeStemp, String message) {
            this.timeStemp = timeStemp;
            this.message = message;
        }

        public Date getTimeStemp() {
            return timeStemp;
        }

        public void setTimeStemp(Date timeStemp) {
            this.timeStemp = timeStemp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getMessage()).append("____").append(getTimeStemp());
            return stringBuilder.toString();
        }
    }
}
