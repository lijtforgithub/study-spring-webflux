package com.ljt.study.webflux.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author LiJingTang
 * @date 2021-05-07 16:37
 */
@RestController
@RequestMapping("/mono")
public class MonoController {

    @GetMapping("/1")
    public String get() {
        System.out.println("----1");
        String result = getResult();
        System.out.println("----2");
        return result;
    }

    @GetMapping("/2")
    public Mono<String> get2() {
        System.out.println("----1");
        Mono<String> result = Mono.create(sink -> getResult());
        System.out.println("----2");
        return result;
    }

    private String getResult() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "webflux:mono";
    }

}
