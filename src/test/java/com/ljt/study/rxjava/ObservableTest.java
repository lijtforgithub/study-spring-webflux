package com.ljt.study.rxjava;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author LiJingTang
 * @date 2021-05-07 13:44
 */
@Slf4j
class ObservableTest {

    /**
     * 被观察者
     * emitter 发射器，发射体
     */
    private static final Observable<Integer> OBSERVABLE = Observable.create(emitter -> {
        // onNext可以 无限次调用
        IntStream.rangeClosed(1, 5).forEach(i -> {
            // The Observable will not call this method again after it calls either onComplete or onError.
            log.info("emitter {}", i);
            emitter.onNext(i);
        });
    });

    /**
     * Observer 观察者
     */
    private static final Observer<Integer> OBSERVER = new Observer<Integer>() {
        @Override
        public void onSubscribe(Disposable d) {
            log.info("onSubscribe {}", d.isDisposed());
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
    void next() {
        OBSERVABLE
                .subscribe(i -> log.info("onNext {}", i));
    }

    @Test
    void onNext() {
        OBSERVABLE
                .doOnNext(System.out::println)
                .subscribe(i -> log.info("onNext {}", i));
    }

    @SneakyThrows
    @Test
    void async() {
        OBSERVABLE
                // 指定的是上游发送事件的线程 多次调用subscribeOn() 只有第一次的有效
                .subscribeOn(Schedulers.computation())
                // 指定的是下游接收事件的线程 多次指定下游的线程是可以的, 也就是说每调用一次observeOn() , 下游的线程就会切换一次.
                .observeOn(Schedulers.io())
                .subscribe(i -> log.info("onNext {}", i));
        TimeUnit.SECONDS.sleep(5);
    }

    /**
     * 上游可以发送无限个onNext, 下游也可以接收无限个onNext.
     * 当上游发送了一个onComplete后, 上游onComplete之后的事件将会继续发送, 而下游收到onComplete事件之后将不再继续接收事件.
     * 当上游发送了一个onError后, 上游onError之后的事件将继续发送, 而下游收到onError事件之后将不再继续接收事件.
     * 上游可以不发送onComplete或onError.
     * 最为关键的是onComplete和onError必须唯一并且互斥, 即不能发多个onComplete, 也不能发多个onError, 也不能先发一个onComplete, 然后再发一个onError, 反之亦然
     */
    @Test
    void completeOrError() {
        Consumer<ObservableEmitter<Integer>> complete = emitter -> {
            log.info("emitter complete");
            emitter.onComplete();
        };
        Consumer<ObservableEmitter<Integer>> error = emitter -> {
            log.info("emitter error");
            emitter.onError(new RuntimeException("Observable 产生错误"));
        };

        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            IntStream.rangeClosed(1, 5).forEach(i -> {
                log.info("emitter {}", i);
                emitter.onNext(i);
                if (i == 4) {
                    complete.accept(emitter);
                }
            });
        }).subscribe(OBSERVER);
    }

    /**
     * 调用dispose()并不会导致上游不再继续发送事件, 上游会继续发送剩余的事件.
     */
    @Test
    void dispose() {
        OBSERVABLE.subscribe(new Observer<Integer>() {

            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                log.info("onSubscribe {}", d.isDisposed());
                disposable = d;
            }

            @Override
            public void onNext(Integer i) {
                log.info("onNext {}", i);

                if (i == 3) {
                    disposable.dispose();
                    log.info("关闭通道 {}", disposable.isDisposed());
                }
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
    }

    @Test
    void map() {
        OBSERVABLE
                .map(i -> "map-" + i)
                .subscribe(log::info);
    }

    @SneakyThrows
    @Test
    void flatMap() {
        OBSERVABLE
                .flatMap(i -> {
                    List<String> list = IntStream.rangeClosed(1, 2).mapToObj(j -> "I am value " + i).collect(Collectors.toList());
                    return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
                })
                .subscribe(log::info);
        TimeUnit.SECONDS.sleep(5);
    }

    @SneakyThrows
    @Test
    void concatMap() {
        OBSERVABLE
                .concatMap(i -> {
                    List<String> list = IntStream.rangeClosed(1, 2).mapToObj(j -> "I am value " + i).collect(Collectors.toList());
                    return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
                })
                .subscribe(log::info);
        TimeUnit.SECONDS.sleep(5);
    }

    @SneakyThrows
    @Test
    void zip() {
        Observable<Character> observable = Observable.create((ObservableOnSubscribe<Character>) emitter -> {
            Stream.of('A', 'B', 'C').forEach(c -> {
                log.info("emitter {}", c);
                emitter.onNext(c);
            });
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());

        Observable.zip(OBSERVABLE, observable, (i, c) -> i + c.toString())
                .subscribe(log::info);
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void backpressure() {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> Stream.iterate(1, i -> ++i).forEach(emitter::onNext))
                // 如果发送和接收在同一线程则没效果
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("" + i);
                });
    }

    @SneakyThrows
    @Test
    void error() {
        Observable.just(1, 3, 0, 5)
                .observeOn(Schedulers.io())
//                .subscribe(i -> System.out.println(15 / i), e -> log.error("处理异常：后面的数据不处理了", e));
                .subscribe(i -> {
                    try {
                        System.out.println(15 / i);
                    } catch (Exception e) {
                        log.error("处理异常", e);
                    }
                });

        TimeUnit.SECONDS.sleep(5);
    }

}
