package microservices.book.gamification.game;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.challenge.ChallengeSolvedEvent;

@RequiredArgsConstructor
@Slf4j
@Service
public class GameEventHandler {

    private final GameService gameService;

    /*
     * you’ll use the @RabbitListener annotation for this. You can add this
     * annotation to a method
     * to make
     * it act as the processing logic of a message when it arrives. In this case,
     * you only need to
     * specify the queue name to subscribe to, since you already declared all
     * RabbitMQ entities
     * in a separate configuration file. There are options to embed these
     * declarations in this
     * annotation, but the code doesn’t look that clean anymore (see
     * https://docs.spring.io/spring-amqp/reference/amqp/receiving-messages/async-
     * consumer.html if you’re
     * curious).
     */
    @RabbitListener(queues = "${amqp.queue.gamification}")
    void handleMultiplicationSolved(final ChallengeSolvedEvent event) {
        log.info("Challenge Solved Event received: {}", event.getAttemptId());
        /*
         * By default, the logic that Spring builds based on the RabbitListener
         * annotations will
         * send the acknowledgment to the broker when the method finalizes without
         * exceptions.
         * In Spring Rabbit, this is called the AUTO acknowledgment mode. You could
         * change it
         * to NONE if ou want the ACK signal to be sent even before processing it, or to
         * MANUAL if
         * you want to be fully in control (then you have to inject an extra parameter
         * to send this
         * signal). You can set up this parameter and other configuration values at the
         * factory
         * level (global configuration) or at the listener level (via passing extra
         * parameters to the
         * RabbitListener annotation).
         */
        try {
            gameService.newAttemptForUser(event);
        } catch (final Exception e) {
            /*
             * The error strategy here is to use the default value AUTO but catch any
             * possible
             * exception, log the error, and then rethrow an
             * AmqpRejectAndDontRequeueException.
             * This is a shortcut provided by Spring AMQP to reject the message and tell the
             * broker
             * not to requeue it. That means that if there is an unexpected error in the
             * Gamification’s
             * consumer logic, we’ll lose the message. That is acceptable in this case. If
             * you want to
             * avoid this situation, you could also set up the code to retry a few times by
             * rethrowing an
             * exception with the opposite meaning, ImmediateRequeueAmqpException, or use
             * some
             * tools available in Spring AMQP like an error handler or message recoverer to
             * process
             * these failed messages. See the Exception Handling section
             * (https://docs.spring.io/spring-amqp/reference/amqp/exception-handling.html) in the Spring
             * AMQP docs for more detailed information.
             */
            log.error("Error when trying to process ChallengeSolvedEvent", e);
            // Avoids the event to be re-queued and reprocessed.
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}