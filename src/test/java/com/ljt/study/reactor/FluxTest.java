package com.ljt.study.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author LiJingTang
 * @date 2021-05-07 13:54
 */
class FluxTest {

    @Test
    void testJust() {
        String[] s = new String[]{"xx", "oo"};
        Flux<String> flux1 = Flux.just(s);
        flux1.subscribe(System.out::println);

        Flux<String> flux2 = Flux.just("xx", "xxx");
        flux2.subscribe(System.out::println);
    }

    @Test
    void testFrom() {
        List<String> list = Arrays.asList("hello", "world");
        Flux<String> flux1 = Flux.fromIterable(list);
        flux1.subscribe(System.out::println);

        Stream<String> stream = Stream.of("hi", "hello");
        Flux<String> flux2 = Flux.fromStream(stream);
        flux2.subscribe(System.out::println);
    }

    @Test
    void testRange() {
        Flux<Integer> flux = Flux.range(0, 5);
        flux.subscribe(System.out::println);
    }

    @Test
    void testInterval() throws InterruptedException {
        // interval方法, take方法限制个数为5个
        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1)).take(5);
        flux.subscribe(System.out::println);

        Thread.sleep(10000);
    }

    @Test
    void testCreate() {
        // 同步动态创建，next 只能被调用一次
        Flux.generate(sink -> {
            sink.next("xx");
            sink.complete();
        }).subscribe(System.out::print);

        Flux.create(sink -> {
            for (int i = 0; i < 100; i++) {
                sink.next("xxoo:" + i);
            }
        }).subscribe(System.out::println);
    }

}
