package microservices.book.multiplication.configuration;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures RabbitMQ via AMQP abstraction to use events in our
 * application.
 */
/*
 * amqp.exchange.attempts=attempts.topic is a custom property defined to
 * manage exchanges. Custom properties are not recognized by Spring Boot unless
 * you
 * implemented code to handle them. Now you add the Exchange declaration to a
 * separate
 * configuration file for AMQP. The Spring module has a convenient builder for
 * this, called
 * Chapter 7 Event-Driven Architectures271
 * ExchangeBuilder. You add a bean of the topic type you want to declare in the
 * broker.
 * Besides, you’ll use this configuration class to switch the predefined
 * serialization format
 * to JSON
 */
@Configuration
public class AMQPConfiguration {
    /*
     * The topic is durable, so it’ll remain in the broker after RabbitMQ restarts.
     * Also, you
     * declare it a topic exchange since that’s the solution that was envisioned in
     * this eventdriven system. The name is picked up from configuration thanks to
     * the already known @
     * Value annotation.
     */
    @Bean
    public TopicExchange challengesTopicExchange(
            @Value("${amqp.exchange.attempts}") final String exchangeName) {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    /*
     * By injecting a bean of type Jackson2JsonMessageConverter, you’re overriding
     * the
     * default Java object serializer by a JSON object serializer. You do this to
     * avoid various
     * pitfalls of Java object serialization.
     * • It’s not a proper standard that you can use between programming
     * languages. If you introduced a consumer that’s not written in Java,
     * you would have to look for a specific library to perform crosslanguage
     * deserialization.
     * • It uses a hard-coded, fully qualified type name in the header of the
     * message. The deserializer expects the Java bean to be located in the
     * same package and to have the same name and fields. This is not
     * flexible at all, since you may want to deserialize only some properties
     * and keep your own version of the event data, following good domaindriven
     * design practices.
     * The Jackson2JsonMessageConverter uses a Jackson’s ObjectMapper preconfigured
     * in Spring AMQP. This bean will be used then by the RabbitTemplate
     * implementation,
     * the class that serializes and sends objects as AMQP messages to the broker.
     * On the
     * subscriber side, you can benefit from the popularity of the JSON format to
     * deserialize
     * the contents using any programming language. You could also use your own
     * object
     * representation and ignore properties you don’t need on the consumer side,
     * thereby
     * reducing the coupling between microservices. If the publisher includes new
     * fields in the
     * payload, the subscribers don’t need to change anything.
     */
    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}