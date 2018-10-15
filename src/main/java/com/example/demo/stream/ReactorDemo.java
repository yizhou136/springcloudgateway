package com.example.demo.stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.*;
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
import java.util.stream.Stream;

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
        printLine("flux");
        Flux<String> stringFlux = Flux.just("Hello", "World");
        stringFlux.subscribe(new Subscriber<String>() {
            private Subscription s;
            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(1);  //拉模式
                //s.request(Long.MAX_VALUE);  //推模式
            }
            @Override
            public void onNext(String string) {
                logger.info("subscribe:{}", string);
                this.s.request(1);
            }
            @Override
            public void onError(Throwable t) {
                logger.error(t.getMessage(),t);
            }
            @Override
            public void onComplete() {
                logger.info("onComplete");
            }
        });
        stringFlux.subscribe(System.out::println, null, System.out::println);
        Flux.range(1, 10)
                .skipUntil(i -> { return i>2 ? true:false;})
                .take(1)
                .subscribe(System.out::println,null, System.out::println);
        Flux.just("a","b","c","d")
                .map(i -> {return Integer.valueOf(i, 16);})
                .filter(i -> i > 10 )
                .zipWith(Flux.range(1, 10))
                .subscribe(System.out::println);
        Flux.fromArray(new Integer[] {1, 2, 3})
                .flatMap(delayFun)
                .subscribe(i -> logger.info("flatMap i:{}", i));
                //.concatMap(delayFun)
                //.subscribe(i -> logger.info("concatMap i:{}", i));
        long l = Flux.interval(Duration.ofMillis(500)).take(2).blockLast();
        logger.info("blockLast result:{}", l);

        //动态同步one by one
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3*i);
                    //sink.next("3 x " + i + " = " + 3*i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state));
        flux.subscribe(System.out::println);
        //动态可异步产生多个元素
        Flux.create(fluxSink -> {
            fluxSink.next(1);
            fluxSink.next(2);
            fluxSink.next(3);
            fluxSink.complete();
        }).reduce((f,s) -> (Integer)f + (Integer) s).subscribe(System.out::println);

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
                .switchIfEmpty(Mono.error(new RuntimeException("Not Found Mapping Handler")))
                .flatMap(i -> Mono.just(i*2))
                .flatMap(i -> Mono.just(i*3));
        integerMono.subscribe(i -> logger.info("integerMono:{}", i));
    }

    public static void mono() {
        printLine("mono");
        Mono.fromSupplier(() -> "Hello").subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }
            @Override
            public void onNext(String string) {
                logger.info("subscribe:{}", string);
            }
            @Override
            public void onError(Throwable t) {
                logger.error(t.getMessage(),t);
            }
            @Override
            public void onComplete() {
                logger.info("onComplete");
            }
        });
        Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
        Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);
    }

    public static void firstEmitting() {
        printLine("firstEmitting");
        Mono<String> a = Mono.just("oops I'm late")
                .delayElement(Duration.ofMillis(450));
        Flux<String> b = Flux.just("let's get", "the party", "started")
                .delayElements(Duration.ofMillis(400));

        Flux.first(a, b)
                .toIterable()
                .forEach(System.out::println);
    }

    public static void schedulers() {
        printLine("schedulers");
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
        printLine("connect");
        /*Flux<Integer> source = Flux.range(1, 3)
                .doOnSubscribe(s -> System.out.println("subscribed to source"));

        ConnectableFlux<Integer> co = source.publish();

        co.subscribe(System.out::println, e -> {}, () -> {});
        co.subscribe(System.out::println, e -> {}, () -> {});

        System.out.println("done subscribing");
        Thread.sleep(500);
        System.out.println("will now connect");

        co.connect();*/

        final Flux<Long> source = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .publish()
                .autoConnect();
        source.subscribe();
        Thread.sleep(500);
        source.toStream().forEach(System.out::println);

        UnicastProcessor<String> hotSource = UnicastProcessor.create();
        Flux<String> hotFlux = hotSource.publish()
                .autoConnect()
                .map(String::toUpperCase);
        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: "+d));
        hotSource.onNext("blue");
        hotSource.onNext("green");
        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: "+d));
        hotSource.onNext("orange");
        hotSource.onNext("purple");
        hotSource.onComplete();
    }

    public static void forDebug() {
        printLine("forDebug");
        Flux.just(1, 0).map(x -> 1 / x).checkpoint("test").subscribe(System.out::println);
        Flux.range(1, 2).log("Range").subscribe(System.out::println);
    }

    private static final void printLine(String msg){
        logger.info("============================{}==============================================", msg);
    }

    public static void main(String argv[]) throws InterruptedException {
        flux();
        mono();
        firstEmitting();
        connect();
        schedulers();
        forDebug();
        Thread.sleep(100000);
    }
}
