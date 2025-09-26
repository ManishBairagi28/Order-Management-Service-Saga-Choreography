package com.orderms.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class KafkaTopicInitializer implements ApplicationRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaTopicInitializer.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Override
    public void run(ApplicationArguments args) {
        createTopicsIfNotExists();
    }

    private void createTopicsIfNotExists() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(props)) {
            List<NewTopic> topics = List.of(
                    new NewTopic("order-events", 3, (short) 1),
                    new NewTopic("payment-events", 3, (short) 1),
                    new NewTopic("inventory-events", 3, (short) 1)
            );

            adminClient.createTopics(topics)
                    .all()
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            log.info("Kafka topics created successfully");
                        } else {
                            log.info("Kafka topics already exist or creation failed: {}",exception.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to create Kafka topics", e);
        }
    }
}