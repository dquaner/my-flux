package org.xxts.testwindow.volatiletest.demo;

public class Test {
    public static void main(String[] args) {
        test1();
    }

    public static void test() {
        TestTread testTread = new TestTread();
        testTread.start();
        for (;;) {
            if (testTread.isFlag()) {
                System.out.println("nice");
            }
        }
    }

    public static void test1() {
        TestTread testTread = new TestTread();
        testTread.start();
        for (;;) {
            synchronized (testTread) {
                if (testTread.isFlag()) {
                    System.out.println("nice");
                }
            }
        }
    }
}
