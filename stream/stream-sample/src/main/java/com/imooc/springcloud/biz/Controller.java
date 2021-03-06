package com.imooc.springcloud.biz;

import com.imooc.springcloud.topics.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller {

    @Autowired
    private MyTopic producer;

    @Autowired
    private GroupTopic groupTopicProducer;

    @Autowired
    private DelayedTopic delayedTopicProducer;

    @Autowired
    private ErrorTopic errorTopicProducer;

    @Autowired
    private RequeueTopic requeueTopicProducer;

    @Autowired
    private DLQTopic dlqTopicProducer;

    @Autowired
    private FallBackTopic fallBackTopicProducer;

    @PostMapping("send")
    public void sendMessage(@RequestParam String body){
        producer.output().send(MessageBuilder.withPayload(body).build());
    }

    @PostMapping("sendToGroup")
    public void sendMessageToGroup(@RequestParam String body){
        groupTopicProducer.output().send(MessageBuilder.withPayload(body).build());
    }

//    @PostMapping("sendDM")
//    public void sendDelayedMessage(@RequestParam() String body, Integer seconds){
//        MessageBean msg = new MessageBean();
//        msg.setPayLoad(body);
//        log.info("redy to send delayed message");
//
//        delayedTopicProducer.output().send(MessageBuilder
//                .withPayload(msg)
//                .setHeader("x-delay",1000 * seconds)
//                .build());
//    }

    //异常重试(单机版)
    @PostMapping("sendError")
    public void sendErrorMessage(@RequestParam String body){
        errorTopicProducer.output().send(MessageBuilder.withPayload(body).build());
    }

    //异常重试(联机版)
    @PostMapping("sendErrorToMQ")
    public void sendErrorMessageToMQ(@RequestParam String body){
        requeueTopicProducer.output().send(MessageBuilder.withPayload(body).build());
    }

    //死信队列
    @PostMapping("sendMessageToDLQ")
    public void sendMessageToDLQ(@RequestParam String body){
        dlqTopicProducer.output().send(MessageBuilder.withPayload(body).build());
    }

    //fallback+升版
    @PostMapping("fallback")
    public void sendMessageToFallback(@RequestParam String body, @RequestParam(defaultValue = "1.0") String version){
        fallBackTopicProducer.output().send(MessageBuilder
                .withPayload(body)
                .setHeader("version",version)
                .build());
    }
}
