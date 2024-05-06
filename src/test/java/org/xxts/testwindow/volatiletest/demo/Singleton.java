package org.xxts.testwindow.volatiletest.demo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 双重检查单例
 */
public class Singleton {
    // 保证可见性和禁止指令重排序
    private volatile static Singleton instance = null;

    private Singleton() {

    }

    public static Singleton getInstance() {
        // 第一重检查锁定
        if (instance == null) {
            // 同步锁定代码块
            synchronized (Singleton.class) {
                // 第二重检查锁定
                if (instance == null) {
                    // 注意：非原子操作
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
