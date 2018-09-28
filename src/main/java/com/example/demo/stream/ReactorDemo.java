package com.example.demo.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * @author zhouguobao
 * 2018/8/20
 */
public class ReactorDemo {
    private static final Logger logger = LoggerFactory.getLogger(ReactorDemo.class);


    public static void firstEmitting() {
        Mono<String> a = Mono.just("oops I'm late")
                .delayElement(Duration.ofMillis(450));
        Flux<String> b = Flux.just("let's get", "the party", "started")
                .delayElements(Duration.ofMillis(400));

        Flux.first(a, b)
                .toIterable()
                .forEach(System.out::println);
    }

    public static void connect() throws InterruptedException {
        Flux<Integer> source = Flux.range(1, 3)
                .doOnSubscribe(s -> System.out.println("subscribed to source"));

        ConnectableFlux<Integer> co = source.publish();

        co.subscribe(System.out::println, e -> {}, () -> {});
        co.subscribe(System.out::println, e -> {}, () -> {});

        System.out.println("done subscribing");
        Thread.sleep(500);
        System.out.println("will now connect");

        co.connect();
    }

    public static void zip(){
        Flux.just("a","b","c","d").zipWith(Flux.range(1, 10))
        .subscribe(System.out::println);
    }

    public static void generate() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3*i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state));
        /*flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3*state);
                    if (state == 10) sink.complete();
                    return state + 1;
                });*/
        flux.subscribe(System.out::println);
    }

    public static void mono() {
        //Flux.interval(Duration.ofMillis(100)).take(x)
        Flux.just(5, 10)
                .flatMap(x -> Flux.just(x))
                .subscribe(i -> logger.info("concatMap i:{}", i));
        Flux.just(5, 10)
                .concatMap(x -> Flux.just(x))
                //.next()
                .subscribe(i -> logger.info("concatMap i:{}", i));
        Flux<Integer> flux = Flux.just(1,2,3,4);
        flux.subscribe(System.out::println);
        flux.subscribe(System.out::println);
        Flux<Integer> untilFlux = flux.skipUntil(i -> { if (i > 2) return true; return false;});
        untilFlux.take(1).subscribe(System.out::println);
        if (true) {
            return;
        }
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Flux.fromIterable(list)
                .concatMap(mapping -> {
                    logger.info("concatMap mapping:{}", mapping);
                    //if (true) return Mono.empty();
                    if (mapping == 4) {
                        return Mono.just(mapping);
                    }else {
                        return Mono.empty();
                    }
                }, 1)
                .next()
                //.subscribe(i -> logger.info("i:{}", i))
                .switchIfEmpty(Mono.error(new RuntimeException("ddd")))
                //.subscribe(i -> logger.info("i:{}", i));
                .flatMap(i -> Mono.just(i*2))
                .flatMap(i -> Mono.just(i*3))
                .doOnNext(i -> logger.info("onNextCall1 i:{}", i))
                .doOnNext(i -> logger.info("onNextCall2 i:{}", i))
                .subscribe(i -> logger.info("i:{}", i));
    }


    public static void main(String argv[]) throws InterruptedException {
        /*mono();
        generate();
        zip();*/
        //firstEmitting();
        connect();
    }
}
