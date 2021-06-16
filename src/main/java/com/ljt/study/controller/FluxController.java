package com.ljt.study.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * @author LiJingTang
 * @date 2021-05-10 17:53
 */
@Slf4j
@RestController
@RequestMapping("/flux")
public class FluxController {

    @GetMapping(produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> index() {
        return Flux.interval(Duration.ofSeconds(1)).take(10);
    }

}
