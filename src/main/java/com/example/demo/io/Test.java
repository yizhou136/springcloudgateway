package com.example.demo.io;

import sun.nio.ch.Interruptible;

import java.nio.channels.spi.AbstractInterruptibleChannel;

/**
 * Created by zhougb on 2016/9/6.
 */
public class Test {
    public static class MyThread extends Thread{
        public void run() {
            System.out.println("going to sleep.");
            try {
                Thread.sleep(3333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("wakeup");
        }
    }

    public static void main(String args[]){
        Interruptible  interruptible = new Interruptible() {
            public void interrupt(Thread thread) {
                System.out.println("The thread has interrupted;");
            }
        };

        MyThread myThread = new MyThread();
        sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(myThread, interruptible);

        myThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("do interrupted action");
        myThread.interrupt();
    }
}
