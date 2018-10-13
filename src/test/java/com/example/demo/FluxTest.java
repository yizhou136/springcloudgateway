package com.example.demo;

import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

/**
 * @author zhouguobao
 * 2018/10/12
 */
public class FluxTest {

    @Test
    public void testFlux() {
        Flux.just("Hello ", " World").subscribe(System.out::print);
        System.out.println();
        Flux.fromArray(new Integer[]{1,2,3}).subscribe(System.out::print);
        System.out.println();
        Disposable disposable = Flux.interval(Duration.of(500, ChronoUnit.MILLIS))
                .subscribe(System.out::println);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (true) return;
        //disposable.dispose();
        System.out.println();

        //for generate
        System.out.println("for generate");
        Flux.generate(sink -> {
            sink.next(1);
            //sink.next(2);
            sink.complete();
        }).subscribe(System.out::print);

        System.out.println("for generate stat suplier");

        Random random = new Random(System.currentTimeMillis());
        Flux.generate(ArrayList::new, (list, sink) -> {
            int i = random.nextInt(100);
            list.add(i);
            sink.next(i);

            if (list.size() == 10) {
                sink.complete();
            }

            return list;
        }).subscribe(System.out::println);

        System.out.println("for create");
        //for create
        Flux.create(sink -> {
            for (int i=0; i<10; i++) {
                sink.next(i);
            }

            sink.complete();
        }).subscribe(System.out::print);
    }

    @Test
    public void testMono() {
        Mono.justOrEmpty(Optional.of("hello")).subscribe(System.out::println);
        Mono.create(monoSink -> {
            monoSink.success("world");
        }).subscribe(System.out::println);
    }

    @Test
    public void testBufferFilter(){
        Flux.range(1, 100).buffer(20).subscribe(System.out::println);
        System.out.println("for 5.....");
        Flux.interval(Duration.of(1, ChronoUnit.MILLIS))
                .take(5).toStream().forEach(System.out::println);
        System.out.println("for 5.....");
        Flux.interval(Duration.of(100, ChronoUnit.MILLIS)).buffer(Duration.of(201, ChronoUnit.MILLIS))
                .take(4).toStream().forEach(System.out::println);
        Flux.range(1, 10).bufferUntil(i -> i % 2 == 0).subscribe(System.out::println);
        Flux.range(1, 10).bufferWhile(i -> i % 2 == 0).subscribe(System.out::println);

        System.out.println("for filter");
        Flux.range(0, 10).filter(i-> i % 2 == 0).subscribe(System.out::print);

        System.out.println("for window");
        Flux.range(1, 100).window(20).flatMap((flux)->{
            flux.subscribe(System.out::print);
            System.out.println("----");
            return Flux.empty();
        }).subscribe(System.out::println);
        //Flux.intervalMillis(100).windowMillis(1001).take(2).toStream().forEach(System.out::println);


        System.out.println("for zipWith");
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"))
                .subscribe(System.out::println);
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);

        System.out.println("for take");
        Flux.range(1, 1000).take(10).subscribe(System.out::println);
        /*Flux.range(1, 1000).takeLast(10).subscribe(System.out::println);
        Flux.range(1, 1000).takeWhile(i -> i < 10).subscribe(System.out::println);
        Flux.range(1, 1000).takeUntil(i -> i == 10).subscribe(System.out::println);*/

        System.out.println("for reduce");
        Flux.range(1, 100).reduce((x, y) -> x + y).subscribe(System.out::println);
        Flux.range(1, 100).reduceWith(() -> 100, (x, y) -> x + y).subscribe(System.out::println);
    }

    @Test
    public void testSubscribe() {
        Flux.just(1,2,3)
                .concatWith(Mono.error(new IllegalStateException("iLLegalState")))
                .subscribe(System.out::println, System.err::println);

        Flux.just(1, 2)
                .concatWith(Mono.error(new IllegalArgumentException()))
                .retry(1)
                .onErrorResume(e -> {
                    if (e instanceof IllegalStateException) {
                        return Mono.just(0);
                    } else if (e instanceof IllegalArgumentException) {
                        return Mono.just(-1);
                    }
                    return Mono.empty();
                })
                .subscribe(System.out::println);



        Flux.create(sink -> {
            sink.next(Thread.currentThread().getName());
            sink.complete();
        })
                .publishOn(Schedulers.single())
                .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
                .publishOn(Schedulers.elastic())
                .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
                .subscribeOn(Schedulers.parallel())
                .toStream()
                .forEach(System.out::println);


        Flux.range(1, 2).log("Range").subscribe(System.out::println);

        final Flux<Long> source = Flux.interval(Duration.ofSeconds(1))
                .take(10)
                .publish()
                .autoConnect();
        source.subscribe();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        source.toStream().forEach(System.out::println);
    }

    @Test
    public void testVerfiy(){
        StepVerifier.create(Flux.just(1,2,3))
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectComplete();

        StepVerifier.withVirtualTime( ()-> Flux.interval(Duration.of(4, ChronoUnit.HOURS), Duration.ofDays(1)).take(2))
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(4))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .verifyComplete();

        final TestPublisher<String> testPublisher = TestPublisher.create();
        testPublisher.next("a");
        testPublisher.next("b");
        testPublisher.complete();

        StepVerifier.create(testPublisher)
                .expectNext("a")
                .expectNext("b")
                .expectComplete();




    }
}
