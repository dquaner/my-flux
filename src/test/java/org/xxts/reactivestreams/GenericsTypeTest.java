package org.xxts.reactivestreams;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericsTypeTest {

    static class Fruit {
        protected String name;

        public Fruit(String name) {
            this.name = name;
        }
    }

    static class Apple extends Fruit {
        private final String type;

        public Apple(String name, String type) {
            super(name);
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    @Test
    void test() {
        List<Fruit> list = new ArrayList<>();
        list.add(new Fruit("banana"));
        list.add(new Apple("apple", "red"));
        System.out.println(Arrays.toString(list.toArray()));
    }
}
