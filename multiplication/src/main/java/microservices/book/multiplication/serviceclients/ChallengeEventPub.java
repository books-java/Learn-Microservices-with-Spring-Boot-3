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
        ChallengeSolvedEvent event = buildEvent(challengeAttempt);
        // Routing Key is 'attempt.correct' or 'attempt.wrong'
        String routingKey = "attempt." + (event.isCorrect() ? "correct" : "wrong");
        // Convert a Java object to an Amqp Message and send it to a default exchange
        // with a default routing key.
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