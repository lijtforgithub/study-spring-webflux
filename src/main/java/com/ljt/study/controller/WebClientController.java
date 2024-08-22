package com.ljt.study.controller;

import com.alibaba.fastjson2.JSONObject;
import com.ljt.study.core.R;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author LiJingTang
 * @date 2024-05-28 16:19
 */
@Slf4j
@RestController
@RequestMapping("/webclient")
public class WebClientController {

    @Autowired
    private WebClient webClient;

    @GetMapping("/questionnaireType")
    Mono<R<List<QuestionnaireTypeVO>>> questionnaireType(String query) {
        log.info("请求方法");
        Assert.isTrue(!"0".equals(query));

        return webClient.post().uri("/external/applet/questionnaireType")
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(new ParameterizedTypeReference<R<List<QuestionnaireTypeVO>>>() {
                        });
                    } else {
                        return response.createException().flatMap(e -> {
                            log.error("错误", e);
                            return Mono.error(new Exception(e.getMessage()));
                        });
                    }
                }).timeout(Duration.ofSeconds(60));

    }

    @Data
    public static class QuestionnaireTypeVO {

        private String typeId;
        private String typeName;

    }

    public static void main(String[] args) throws Exception {
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=82_3zChOUxZbaP11p-BF9UwEfC1o4Q6U4OCM2-AtZO1iRY84_cQO3WFNaaeWIST4xAa_zPNdQF3zQq-gOf5SokR59DwPaK9XOP2fbb4AVTcnXXUeRDmW4SGr4xXF4gPKBcAEADYP";

        JSONObject param = new JSONObject();
        param.put("scene", "id=1379156591456480257");
        param.put("page", "pages_activity/activityInfo/index");
        param.put("check_path", false);
        param.put("env_version", "trial");

//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, param, byte[].class);
//        System.out.println(response.getHeaders().getContentType());

        WebClient webClient1 = WebClient.builder().build();
        webClient1.post().uri(url).bodyValue(param).retrieve()
                .bodyToMono(byte[].class).timeout(Duration.ofSeconds(10)).subscribe(bytes -> {
            try {
                FileUtils.writeByteArrayToFile(new File("/Users/lijingtang/Downloads/1.jpg"), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

//        webClient1.post().uri(url).bodyValue(param).exchangeToMono(response -> {
//            System.out.println(response.statusCode());
//            System.out.println(response.headers().contentType().orElse(null));
//            return response.bodyToMono(String.class);
//        }).subscribe(bytes -> {
//            System.out.println(new String(bytes));
//        });

        TimeUnit.SECONDS.sleep(5);

//        byte[] body = response.getBody();

    }

}
