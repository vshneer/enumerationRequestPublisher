package com.annalabs.enumerationRequestPublisher.controller;


import com.annalabs.enumerationRequestPublisher.request.LaunchEnumerationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LaunchSubDomainEnumerationController {

    @Value("${kafka.topics.domain}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/launchEnumeration")
    public ResponseEntity<String> launchEnumeration(@RequestBody LaunchEnumerationRequest launchEnumerationRequest) {
        try {
            kafkaTemplate.send(topic, launchEnumerationRequest.getDomain());
            return new ResponseEntity<>("Published to Kafka", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to publish to Kafka", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
