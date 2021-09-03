package com.lsh.pulsar.controller;

import com.lsh.pulsar.component.PulsarProducer;
import com.lsh.pulsar.dto.MessageDTO;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Liu Shaohua
 * Create at 2021/9/3
 */
@RestController
@RequestMapping("/pulsar")
public class PulsarController {

    @Autowired
    private PulsarProducer pulsarProducer;

    @PostMapping("/send")
    public void send(@RequestBody MessageDTO message) throws PulsarClientException {
        pulsarProducer.sendMsg(message.getKey(), message.getData());
    }
}
