package com.example.demo.stream;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zhouguobao
 * 2018/8/20
 */
public class ReactorDemo {
    private static final Logger logger = LoggerFactory.getLogger(ReactorDemo.class);
    static {
        Hooks.onOperatorDebug();
    }
    private static Function<Integer, Publisher<Integer>> delayFun = (x) -> {
        long millis = 0;
        if (x == 2) {
            millis = 100;
        }
        logger.info("delayFun x:{}", x);
        return Mono.just(x).delayElement(Duration.ofMillis(millis));
    };

    public static void flux() {
        Flux.just("Hello", "World").subscribe(System.out::println);
        Flux.fromArray(new Integer[] {1, 2, 3}).subscribe(System.out::println);
        Flux.range(1, 10).subscribe(System.out::println);
        Flux.just("a","b","c","d").zipWith(Flux.range(1, 10))
                .subscribe(System.out::println);
        Flux.just(1, 2, 3)
                //.flatMap(delayFun)
                //.subscribe(i -> logger.info("flatMap i:{}", i));
                .concatMap(delayFun)
                .subscribe(i -> logger.info("concatMap i:{}", i));
        if (true) return;
        //long l = Flux.interval(Duration.ofSeconds(10)).take(2).blockFirst();
        //Flux.interval(Duration.ofMillis(1000)).subscribe(System.out::println);

        Flux<Integer> flux = Flux.just(1,2,3,4);
        flux.subscribe(System.out::println);
        flux.subscribe(System.out::println);
        Flux<Integer> untilFlux = flux.skipUntil(i -> { if (i > 2) return true; return false;});
        untilFlux.take(1).subscribe(System.out::println);
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Mono<Integer> integerMono = Flux.fromIterable(list)
                .concatMap(mapping -> {
                    logger.info("concatMap mapping:{}", mapping);
                    if (mapping == 4) {
                        return Mono.just(mapping);
                    }else {
                        return Mono.empty();
                    }
                }, 1)
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("ddd")))
                .flatMap(i -> Mono.just(i*2))
                .flatMap(i -> Mono.just(i*3));
        integerMono.subscribe(i -> logger.info("i:{}", i));
    }

    public static void firstEmitting() {
        Mono<String> a = Mono.just("oops I'm late")
                .delayElement(Duration.ofMillis(450));
        Flux<String> b = Flux.just("let's get", "the party", "started")
                .delayElements(Duration.ofMillis(400));

        Flux.first(a, b)
                .toIterable()
                .forEach(System.out::println);
    }

    public static void schedulers() {
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
    public static void generate() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3*i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state));
        flux.subscribe(System.out::println);
    }

    public static void mono() {
        Mono.fromSupplier(() -> "Hello").subscribe(System.out::println);
        Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
        Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);
    }

    public static void forDebug() {
        Flux.just(1, 0).map(x -> 1 / x).checkpoint("test").subscribe(System.out::println);
        Flux.range(1, 2).log("Range").subscribe(System.out::println);
    }

    public static void main(String argv[]) throws InterruptedException {
        flux();
        //mono();
        //generate();
        //firstEmitting();
        //connect();

        //schedulers();
        //forDebug();
        Thread.sleep(100000);
    }
}
