package org.xxts.testwindow.volatiletest.demo;

public class TestTread2 extends Thread {

    private volatile boolean flag = false;

    public boolean isFlag() {
        return flag;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        flag = true;
        System.out.println("flag=" + flag);
    }

}
