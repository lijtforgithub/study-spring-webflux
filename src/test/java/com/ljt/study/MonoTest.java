package com.ljt.study;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author LiJingTang
 * @date 2021-05-08 09:18
 */
class MonoTest {

    @Test
    void testJust() {
        Mono.just("hello").subscribe(System.out::println);
    }

    @Test
    void testCreate() {
        Mono.create(sink -> sink.success("word")).subscribe(System.out::println);
    }

}
