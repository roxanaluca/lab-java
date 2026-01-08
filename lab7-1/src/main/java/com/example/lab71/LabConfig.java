package com.example.lab71;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.Id;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class LabConfig {
    private HikariConfig getDatasourceConfigFile(DBProperties dbProperties, String databaseName) {
        HikariConfig ds = new HikariConfig();
        ds.setJdbcUrl(dbProperties.url() + "/" + databaseName);
        ds.setUsername(dbProperties.username());
        ds.setPassword(dbProperties.password());
        ds.setDriverClassName(dbProperties.driverClassName());

        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(2);
        ds.setConnectionTestQuery("SELECT 1");

        return ds;
    }


    final private KafkaProperties kafkaProperties;

    public LabConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    @Primary
    public DataSource getDataSource(DBProperties dbProperties) {
        return new HikariDataSource(getDatasourceConfigFile(dbProperties, "appdb"));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GradeEvent>
    gradeEventKafkaListenerContainerFactory(
            ConsumerFactory<String, GradeEvent> gradeEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, GradeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(gradeEventConsumerFactory);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, GradeEventStudent> gradeEventStudentKafkaTemplate() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JacksonJsonSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public ConsumerFactory<String, GradeEvent> gradeEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        JacksonJsonDeserializer<GradeEvent> jsonDeserializer =
                new JacksonJsonDeserializer<>(GradeEvent.class);
        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages(
                "com.example.lab71"
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

}
