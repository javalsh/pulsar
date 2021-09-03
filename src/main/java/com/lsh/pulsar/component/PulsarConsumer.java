package com.lsh.pulsar.component;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author Liu Shaohua
 * Create at 2021/9/3
 */
@Component
public class PulsarConsumer {


    private static final Logger log = LoggerFactory.getLogger(PulsarConsumer.class);

    @Value("${pulsar.url}")
    private String url;
    @Value("${pulsar.topic}")
    private String topic;
    @Value("${pulsar.subscription}")
    private String subscription;


    private PulsarClient client = null;
    private Consumer consumer = null;

    @PostConstruct
    public void initPulsarConsumer() throws Exception {
        try {
            //构造Pulsar client
            client = PulsarClient.builder()
                                 .serviceUrl(url)
                                 .build();

            //创建consumer
            consumer = client.newConsumer()
                             .topic(topic.split(","))
                             .subscriptionName(subscription)
                             .subscriptionType(SubscriptionType.Shared)//指定消费模式，包含：Exclusive，Failover，Shared，Key_Shared。默认Exclusive模式
                             .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)//指定从哪里开始消费还有Latest，valueof可选，默认Latest
                             .negativeAckRedeliveryDelay(60, TimeUnit.SECONDS)//指定消费失败后延迟多久broker重新发送消息给consumer，默认60s
                             .subscribe();
            new Thread(
                    () -> {
                        try {
                            this.start();
                        } catch (Exception e) {
                            log.error("消费Pulsar数据异常，停止Pulsar连接：", e);
                            this.close();
                        }
                    }
            ).start();
        } catch (Exception e) {
            log.error("Pulsar初始化异常：",e);
            throw e;
        }
    }

    private void close() {
        try {
            consumer.close();
        } catch (PulsarClientException e) {
            log.error("关闭Pulsar消费者失败：",e);
        }
        try {
            client.close();
        } catch (PulsarClientException e) {
            log.error("关闭Pulsar连接失败：",e);
        }
    }

    private void start() throws Exception {
        while (true) {
            Message message = consumer.receive();
            String[] keyArr = message.getKey().split("_");
            String json = new String(message.getData());

            if (StringUtils.isNotEmpty(json)) {
                try{
                    log.info("消费Pulsar数据，key【{}】，json【{}】：", message.getKey(), json);
                }catch(Exception e){
                    log.error("消费Pulsar数据异常，key【{}】，json【{}】：", message.getKey(), json, e);
                }
            }
            consumer.acknowledge(message);
        }
    }
}
