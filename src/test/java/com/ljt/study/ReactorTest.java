package com.ljt.study;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/**
 * @author LiJingTang
 * @date 2021-05-07 13:54
 */
class ReactorTest {

    @Test
    void testFluxCreate() {
        Flux.create(sink -> {
            for (int i = 0; i < 100; i++) {
                sink.next("xxoo:" + i);
            }
            sink.complete();
        }).subscribe(System.out::println);
    }

}
