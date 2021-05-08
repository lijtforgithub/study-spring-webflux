package com.ljt.study.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author LiJingTang
 * @date 2021-05-07 16:37
 */
@Slf4j
@RestController
@RequestMapping("/mono")
public class MonoController {

    @GetMapping("/1")
    public String get() {
        log.info("开始");
        String result = getResult();
        log.info("结束");
        return result;
    }

    @GetMapping("/2")
    public Mono<String> get2() {
        log.info("开始");
        Mono<String> result = Mono.just(getResult());
        log.info("结束");
        return result;
    }

    private String getResult() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return "Mono: " + LocalDateTime.now().toString();
    }

}
