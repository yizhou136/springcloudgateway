package com.example.demo.stream;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.nio.BufferOverflowException;
import java.time.Duration;
import java.util.stream.IntStream;
/**
 * @author zhouguobao
 * 2018/10/12
 */
public class BackPressureDemo {
    private static final Logger logger = LoggerFactory.getLogger(BackPressureDemo.class);

    public static class MsgProducer {
        protected FluxSink<Integer> fluxSink;

        public void setFluxSink(FluxSink<Integer> fluxSink) {
            this.fluxSink = fluxSink;
        }

        protected void generateMsg() {
            IntStream.range(1, 20).forEach(i -> {
                if (i < 10) {
                    sleep(1000);
                }
                this.fluxSink.next(i);
            });
            this.fluxSink.complete();
        }
        void onOverFlow(Integer overMsg) {
            logger.info("onOverFlow overMsg:{}", overMsg);
            sleep(1000);
            this.fluxSink.next(overMsg);
        }
    }

    public static class MsgConsumer extends BaseSubscriber<Integer> {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            subscription.request(1);
        }

        @Override
        protected void hookOnNext(Integer i) {
            logger.info("consumer  do something for item:{}", i);
            if (i >= 10) {
                sleep(1000);
            }
            request(1);
        }
        /*@Override
        protected void hookOnError(Throwable throwable) {
            logger.error("hookOnError throwable:{}", throwable);
        }

        @Override
        protected void hookOnComplete() {
            logger.info("completed.");
        }*/
    }

    public static void main(String argv[]) {
        MsgProducer msgProducer = new MsgProducer();
        MsgConsumer msgConsumer = new MsgConsumer();
        Flux<Integer> integerFlux = Flux
                .create(fluxSink -> {
                    logger.info("create fluxSink:{} for msgProducer", fluxSink);
                    msgProducer.setFluxSink(fluxSink);
                });
        integerFlux
                //.publishOn(Schedulers.newSingle("publishThread"), 1)
                .onBackpressureBuffer(5, overItem -> {
                    msgProducer.onOverFlow(overItem);
                }, BufferOverflowStrategy.DROP_LATEST)
                //.onBackpressureDrop()
                // .onBackpressureBuffer()
                //.onBackpressureError()
                //.onBackpressureLatest()
                .subscribeOn(Schedulers.newSingle("subscribeThread"), true)
                .subscribe(msgConsumer);

        msgProducer.generateMsg();
        sleep(10000000);
    }

    private static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}