package com.example.demo.stream;

import reactor.core.publisher.Flux;

import java.util.stream.Stream;

/**
 * https://blog.piasy.com/AdvancedRxJava/2017/05/12/operator-fusion-part-2/
 */
public class FusionDemo {


    public static void conditionalFusion() {
        Flux.just(1,2,3)
                .filter(i-> i > 1)
                .subscribe(System.out::println);
    }

    public static void syncFusion() {
        Flux.just(1,2,3)
                .map(i -> i+1)
                .concatMap(i->Flux.just((Integer)i))
                .subscribe(System.out::println);
    }

    public static void asyncFusion() {
        Flux.create(fluxSink -> Stream.of(1,2,3).forEach(i -> fluxSink.next(i)))
                .onBackpressureBuffer()
                .concatMap(i->Flux.just((Integer)i*2))
                .subscribe(System.out::println);
    }

    public static void noneFusion() {
        Flux.create(fluxSink -> Stream.of(1,2,3).forEach(i -> fluxSink.next(i)))
                .concatMap(i->Flux.just((Integer)i*2))
                .subscribe(System.out::println);
    }

    public static void main(String[] argvs) {
        //conditionalFusion();
        syncFusion();
        asyncFusion();
        noneFusion();
    }
}
