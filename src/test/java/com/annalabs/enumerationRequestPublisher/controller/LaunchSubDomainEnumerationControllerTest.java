package com.annalabs.enumerationRequestPublisher.controller;

import com.annalabs.enumerationRequestPublisher.request.LaunchEnumerationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LaunchSubDomainEnumerationControllerTest {

    public static final String TEST_D = "fnx.co.il";
    private static final String TOPIC = "domains";

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    static {
        kafkaContainer.start();
    }
    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    LaunchSubDomainEnumerationController controller;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    @Test
    void testLaunch() throws InterruptedException {
        // Send a message to the topic
        controller.launchEnumeration(new LaunchEnumerationRequest(TEST_D));
        // Wait for the message to be consumed
        boolean messageConsumed = latch.await(10, TimeUnit.SECONDS);
        // Verify the received message
        assertTrue(messageConsumed, "Message was not consumed in time");
        assertEquals(TEST_D, receivedMessage, "The consumed message does not match the produced message");
    }

    @KafkaListener(topics = TOPIC, groupId = "test-group")
    public void listen(String message) {
        this.receivedMessage = message;
        latch.countDown();
    }
}