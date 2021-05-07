package com.ljt.study;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;

/**
 * @author LiJingTang
 * @date 2021-05-07 13:44
 */
class RxJavaTest {

    @Test
    void testSync() {
        // 被观察者
        // emitter 发射器，发射体
        Observable<String> girl = Observable.create(emitter -> {
            // onNext可以 无限次调用
            System.out.println(Thread.currentThread().getName());
            emitter.onNext("1");
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName());
            emitter.onNext("2");
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName());
            emitter.onNext("3");
            Thread.sleep(1000);
            emitter.onNext("4");
            Thread.sleep(1000);
            emitter.onNext("5");
            Thread.sleep(1000);
            emitter.onComplete();
        });

        // Observer 观察者
        Observer<String> man = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe" + d);
            }

            @Override
            public void onNext(String t) {
                System.out.println(Thread.currentThread().getName());
                System.out.println("onNext " + t);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError " + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        girl.subscribe(man);
    }

    @Test
    void testAsync() throws InterruptedException {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            emitter.onNext("1");
            emitter.onNext("2");
            emitter.onNext("3");
            emitter.onNext("4");
            emitter.onNext("5");
            emitter.onComplete();
        })
        .observeOn(Schedulers.computation())
        .subscribeOn(Schedulers.computation())
        .subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe...");
            }

            @Override
            public void onNext(String t) {
                System.out.println("onNext");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }

        });

        Thread.sleep(10000);
    }

}
