package org.xxts;

import reactor.core.publisher.Flux;

public class Main {

    public static void main(String[] args) {
        Flux.<String>create(emitter -> {
                    emitter.next("test");
                    emitter.complete();
                })
                .filter(s -> !s.isEmpty())
                .map(s -> s + "_map")
                .subscribe(System.out::println);
    }
}
