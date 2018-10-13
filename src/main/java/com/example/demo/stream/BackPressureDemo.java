package com.example.demo.stream;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.stream.IntStream;

/**
 * @author zhouguobao
 * 2018/10/12
 */
public class BackPressureDemo {
    public static class FastProducer {
        private FluxSink fluxSink;
        public FastProducer(final FluxSink fluxSink) {
            this.fluxSink = fluxSink;
        }

        void generateMsg() {
            IntStream.range(1, 10).forEach(i -> fluxSink.next(i));
            fluxSink.complete();
        }
    }

    public static class SlowConsumer extends BaseSubscriber<Integer> {
        public SlowConsumer(){}

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            subscription.request(2);
        }

        @Override
        protected void hookOnNext(Integer value) {
            System.out.println("consumer:"+value);
        }
    }

    public static void main(String argv[]) {
        SlowConsumer slowConsumer = new SlowConsumer();
        Flux<Integer> integerFlux = Flux.create(fluxSink -> {
            FastProducer fastProducer = new FastProducer(fluxSink);
            fastProducer.generateMsg();
        }, FluxSink.OverflowStrategy.ERROR);
        integerFlux
                .onBackpressureBuffer()
                .subscribe(slowConsumer);

        slowConsumer.request(1);
    }
}
