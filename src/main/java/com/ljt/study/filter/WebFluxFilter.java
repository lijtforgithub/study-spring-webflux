package com.ljt.study.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author LiJingTang
 * @date 2024-05-30 15:20
 */

@Slf4j
@Component
public class WebFluxFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        log.info("拦截器 {}", request.getQueryParams());

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode != HttpStatus.OK) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange.mutate().response(new CacheResponseBody(exchange)).build());
    }


    public static class CacheResponseBody extends ServerHttpResponseDecorator {

        private final ServerWebExchange exchange;

        public CacheResponseBody(ServerWebExchange exchange) {
            super(exchange.getResponse());
            this.exchange = exchange;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
//            return super.writeWith(DataBufferUtils.join(body).doOnNext(db -> {
//                                String respBody = db.toString(StandardCharsets.UTF_8);
//                                log.info("resp: " + respBody);
//                            })
//            );


            Flux<DataBuffer> flux = Flux.from(body);
            return super.writeWith(flux.buffer().map(dataBuffers -> {
                DataBuffer db = getDelegate().bufferFactory().join(dataBuffers);
                byte[] content = new byte[db.readableByteCount()];
                db.read(content);
                DataBufferUtils.release(db);
                String respBody = new String(content);
                log.info("resp: " + respBody);
                return getDelegate().bufferFactory().wrap(content);
            }));
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return writeWith(Flux.from(body).flatMapSequential(p -> p));
        }

    }

}
