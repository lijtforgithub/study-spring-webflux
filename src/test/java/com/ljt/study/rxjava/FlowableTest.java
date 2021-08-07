package com.ljt.study.rxjava;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author LiJingTang
 * @date 2021-05-07 13:44
 */
@Slf4j
class FlowableTest {

    private Flowable<Integer> flowable(int max, BackpressureStrategy strategy) {
        return Flowable.create((FlowableEmitter<Integer> emitter) -> {
            IntStream.rangeClosed(1, max).forEach(i -> {
                log.info("emitter {}", i);
                emitter.onNext(i);
            });
        }, strategy);
    }

    private Subscriber<Integer> subscriber(Long req) {
        return new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.info("onSubscribe {}", s);
                // 接收能力 去掉此行代码会报错
                if (Objects.nonNull(req)) {
                    s.request(req);
                }
            }

            @Override
            public void onNext(Integer i) {
                log.info("onNext {}", i);
            }

            @Override
            public void onError(Throwable e) {
                log.info("onError {}", e.getMessage());
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }
        };
    }

    @Test
    void req() {
        flowable(3, BackpressureStrategy.ERROR)
                .subscribe(subscriber(Long.MAX_VALUE));
    }

    @Test
    void noReq() {
        flowable(3, BackpressureStrategy.ERROR)
                .subscribe(subscriber(null));
    }

    @Test
    void noReqAsync() {
        flowable(128, BackpressureStrategy.ERROR)
                .observeOn(Schedulers.io())
                .subscribe(subscriber(null));
    }

    @Test
    void noReqAsync129() {
        flowable(129, BackpressureStrategy.ERROR)
                .observeOn(Schedulers.io())
                .subscribe(subscriber(null));
    }

    /**
     * MISSING 通过Create方法创建的Flowable没有指定背压策略，不会对通过OnNext发射的数据做缓存或丢弃处理，
     * 需要下游通过背压操作符（onBackpressureBuffer()/onBackpressureDrop()/onBackpressureLatest()）指定背压策略
     *
     * BUFFER 默认策略，Flowable的异步缓存池同Observable的一样，没有固定大小，可以无限制向里添加数据，不会抛出MissingBackpressureException异常，但会导致OOM。
     */
    @Test
    void buffer() {
        flowable(200, BackpressureStrategy.BUFFER)
//                .onBackpressureBuffer(100)
                .observeOn(Schedulers.io())
                .subscribe(subscriber(null));
    }

    private static final Flowable FLOWABLE = Flowable.create((FlowableOnSubscribe<Integer>) emitter -> {
        IntStream.rangeClosed(1, 5).forEach(i -> {
            log.info("emitter {} , requested = {}", i, emitter.requested());
            emitter.onNext(i);
        });
    }, BackpressureStrategy.ERROR);

    private static final Subscriber<Integer> SUBSCRIBER = new Subscriber<Integer>() {

        @Override
        public void onSubscribe(Subscription s) {
            log.info("onSubscribe {}", s);
            s.request(96);
        }

        @Override
        public void onNext(Integer i) {
            log.info("onNext {}", i);
        }

        @Override
        public void onError(Throwable e) {
            log.info("onError {}", e.getMessage());
        }

        @Override
        public void onComplete() {
            log.info("onComplete");
        }
    };

    @Test
    void setReqSync() {
        FLOWABLE.subscribe(SUBSCRIBER);
    }

    /**
     * 异步缓冲池 128
     */
    @Test
    void setReqAsync() {
        FLOWABLE
                .subscribeOn(Schedulers.trampoline()).observeOn(Schedulers.io())
                .subscribe(SUBSCRIBER);
    }

    @SneakyThrows
    @Test
    void backpressure() {
        Flowable.create((FlowableOnSubscribe<Integer>) emitter -> {
            log.info("First requested = {}", emitter.requested());

            for (int i = 1; i <= 5000; i++) {
                boolean flag = false;
                /*
                 * 因为缓存池中数据的清理，并不是Subscriber接收一条，便清理一条，而是每累积到95条清理一次。也就是Subscriber接收到第96条数据时，缓存池才开始清理数据，之后Flowable发射的数据才得以放入。
                 * 当下游消费掉第96个（0.75）事件之后，上游又开始发事件了，而且可以看到当前上游的requested的值是96
                 */
                while (!emitter.isCancelled() && emitter.requested() == 0) {
                    if (!flag) {
                        log.info("缓冲池满了 暂停发送");
                        flag = true;
                    }

                    // 线程睡眠等待下游接收
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                log.info("emitter {} , requested = {}", i, emitter.requested());
                emitter.onNext(i);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<Integer>() {

                    private Subscription mSubscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        log.info("onSubscribe {}", s);
                        mSubscription = s;
                        mSubscription.request(1);
                    }

                    @SneakyThrows
                    @Override
                    public void onNext(Integer i) {
                        log.info("onNext {}", i);
                        TimeUnit.MILLISECONDS.sleep(200);
                        mSubscription.request(1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        log.info("onError {}", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        log.info("onComplete");
                    }
                });
        TimeUnit.SECONDS.sleep(100);
    }

}
