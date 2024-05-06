package org.xxts.testwindow.memoryleak;


import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

class WeakHashMapTest {
    Map<String, String> wMap = new WeakHashMap<>();
    Map<String, String> map = new HashMap<>();

    @Test
    void entry() {
        init();
        testWeakHashMap();
        testHashMap();
    }

    void init() {
        String ref1 = "obejct1";
        String ref2 = "obejct2";
        String ref3 = "obejct3";
        String ref4 = "obejct4";
        wMap.put(ref1, "cacheObject1");
        wMap.put(ref2, "cacheObject2");
        map.put(ref3, "cacheObject3");
        map.put(ref4, "cacheObject4");
        System.out.println("String 引用 ref1，ref2，ref3，ref4...");
    }

    void testWeakHashMap() {
        System.out.println("WeakHashMap GC之前：");
        for (Object o : wMap.entrySet()) {
            System.out.println(o);
        }
        try {
            System.gc();
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("WeakHashMap GC之后：");
        for (Object o : wMap.entrySet()) {
            System.out.println(o);
        }
    }

    void testHashMap() {
        System.out.println("HashMap GC之前：");
        for (Object o : map.entrySet()) {
            System.out.println(o);
        }
        try {
            System.gc();
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("HashMap GC之后：");
        for (Object o : map.entrySet()) {
            System.out.println(o);
        }
    }
}

