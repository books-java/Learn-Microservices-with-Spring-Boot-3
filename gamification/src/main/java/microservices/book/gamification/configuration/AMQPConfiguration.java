package microservices.book.gamification.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import java.time.Duration;

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
/*
 * To declare the new queue and the binding, you’ll also use a configuration
 * class
 * named AMQPConfiguration. Bear in mind that you should also declare the
 * exchange
 * on the consumer’s side. Even though the subscriber doesn’t own the exchange
 * conceptually, you want your microservices to be able to start in any given
 * order. If you
 * don’t declare the exchange on the Gamification microservice and the broker’s
 * entities
 * have not been initialized yet, you’re forced to start the Multiplication
 * microservice
 * before. The exchange has to be there when you declare the queue. This applies
 * only the
 * first time since you make the exchange durable, yet it’s a good practice to
 * declare all
 * exchanges and queues that a microservice requires in its code, so it doesn’t
 * rely on any
 * other. Note that the declaration of RabbitMQ entities is an idempotent
 * operation; if the
 * entity is there, the operation doesn’t have any effect.
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
     * RabbitMQ allows you to configure how long the messages can
     * be kept in a queue before discarding them (time-to-live, TTL). You can also
     * configure
     * a maximum length for the queue if you prefer so. By default, these parameters
     * are not
     * set, but you can enable them per message (at publishing time) or when you
     * declare the
     * queue
     */
    // configure queue to have a custom TTL of six hours and a max length of 25000
    // messages
    @Bean
    public Queue gamificationQueue(
            @Value("${amqp.queue.gamification}") final String queueName) {
        return QueueBuilder.durable(queueName).ttl((int) Duration.ofHours(6).toMillis())
                .maxLength(2500)
                .build();
    }

    @Bean
    public Binding correctAttemptsBinding(final Queue gamificationQueue,
            final TopicExchange attemptsExchange) {
        // this routing key is used as a filter to receive only correct attempts
        return BindingBuilder.bind(gamificationQueue)
                .to(attemptsExchange)
                .with("attempt.correct");
    }

    /*
     * You also need some configuration on the consumer side to deserialize the
     * messages
     * using JSON, instead of the format provided by the default’s message
     * converter.
     */
    /*
     * you set up a MessageHandlerMethodFactory bean to replace the
     * default one. You actually use the default factory as a baseline but then
     * replace its
     * message converter with a MappingJackson2MessageConverter instance, which
     * handles
     * the message deserialization from JSON to Java classes. You fine-tune its
     * included
     * ObjectMapper and add the ParameterNamesModule to avoid having to use empty
     * constructors for your event classes
     */
    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();

        final MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.getObjectMapper().registerModule(
                new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

        factory.setMessageConverter(jsonConverter);
        return factory;
    }

    /*
     * This time, you won’t use the AmqpTemplate to receive messages since that’s
     * based
     * on polling, which consumes network resources unnecessarily. Instead, you want
     * the
     * broker to notify subscribers when there are messages, so you’ll use an
     * asynchronous
     * option. The AMQP abstraction doesn’t support this, but the spring-rabbit
     * component
     * offers two mechanisms for consuming messages asynchronously. The simplest,
     * most
     * popular one is the @RabbitListener annotation, which you’ll use to get the
     * events from
     * the queue. To configure the listeners to use a JSON deserialization, you have
     * to override
     * the RabbitListenerConfigurer bean with an implementation that uses the custom
     * MessageHandlerMethodFactory
     */
    @Bean
    public RabbitListenerConfigurer rabbitListenerConfigurer(
            final MessageHandlerMethodFactory messageHandlerMethodFactory) {
        return c -> c.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
    }
}