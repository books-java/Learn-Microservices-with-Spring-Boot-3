package microservices.book.multiplication.serviceclients;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import microservices.book.multiplication.challenge.ChallengeAttempt;
import microservices.book.multiplication.challenge.ChallengeSolvedEvent;

/*
 * Next, you create a new component to publish the event. This is the equivalent to
the REST client you already removed, but this time you communicate with the message broker
 */
@Service
public class ChallengeEventPub {

    /*
     * AmqpTemplate is just an interface that defines the AMQP standards. The
     * underlying
     * implementation is RabbitTemplate, and it uses the JSON converter you
     * configured
     * earlier
     */
    private final AmqpTemplate amqpTemplate;
    private final String challengesTopicExchange;

    public ChallengeEventPub(final AmqpTemplate amqpTemplate,
            @Value("${amqp.exchange.attempts}") final String challengesTopicExchange) {
        this.amqpTemplate = amqpTemplate;
        this.challengesTopicExchange = challengesTopicExchange;
    }

    public void challengeSolved(final ChallengeAttempt challengeAttempt) {
        /*
         * Conceptually, the Multiplication microservice owns the attempted exchange.
         * It’ll
         * use it to publish events that are related to attempts coming from the users.
         * In principle,
         * it’ll publish both correct and wrong items, since it doesn’t know anything
         * about the
         * consumers’ logic
         */

        ChallengeSolvedEvent event = buildEvent(challengeAttempt);
        // Routing Key is 'attempt.correct' or 'attempt.wrong'
        String routingKey = "attempt." + (event.isCorrect() ? "correct" : "wrong");
        // Convert a Java object to an Amqp Message and send it to a default exchange
        // with a default routing key.
        /*
         * AmqpTemplate is just an interface that defines the AMQP standards. The
         * underlying
         * implementation is RabbitTemplate, and it uses the JSON converter you
         * configured
         * earlier. We plan to call the challengeSolved method from the main Challenge
         * service
         * logic, within the ChallengeServiceImpl class. This method translates the
         * domain object
         * to the event object using the auxiliary method buildEvent, and it uses the
         * amqpTemplate
         * to convert (to JSON) and send the event with a given routing key. This one is
         * either
         * attempt.correct or attempt.wrong, depending on whether the user was right or
         * not.
         */
        /*
         * if the broker service is down , you’ll get an HTTP error response from the
         * server since you didn’t catch any potential exception within the publisher,
         * nor in the
         * main service logic located at ChallengeServiceImpl. You could add a try/catch
         * clause,
         * so you are still able to respond. The strategy would be to suppress the error
         * silently.
         * A possibly better approach is to implement a custom HTTP error handler to
         * return a
         * specific error response such as 503 SERVICE UNAVAILABLE to indicate that the
         * system is
         * not operational when you lose connection with the broker.
         */
        amqpTemplate.convertAndSend(challengesTopicExchange,
                routingKey,
                event);
    }

    private ChallengeSolvedEvent buildEvent(final ChallengeAttempt attempt) {
        return new ChallengeSolvedEvent(attempt.getId(),
                attempt.isCorrect(), attempt.getFactorA(),
                attempt.getFactorB(), attempt.getUser().getId(),
                attempt.getUser().getAlias());
    }
}