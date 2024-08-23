package microservices.book.multiplication.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import microservices.book.multiplication.serviceclients.ChallengeEventPub;
import microservices.book.multiplication.user.User;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeEventPubTest {

    private ChallengeEventPub challengeEventPub;

    @Mock
    private AmqpTemplate amqpTemplate;

    @BeforeEach
    public void setUp() {
        challengeEventPub = new ChallengeEventPub(amqpTemplate,
                "test.topic");
    }

    /*
     * You want to check that the AmqpTemplate, which
     * you’ll mock, is called with the desired routing key and event object, but you
     * can’t access
     * that data from outside the method. Making the method return an object with
     * these
     * values seems like adapting the code too much to your tests. What you can do
     * in this case
     * is use Mockito’s ArgumentCaptor<T> class (see https://www.javadoc.io/doc/org.
     * mockito/mockito-core/latest/org/mockito/ArgumentCaptor.html) to capture the
     * arguments passed to a mock, so you can assert these values later.
     */
    /*
     * we
     * introduce another JUnit feature: parameterized tests (see
     * https://junit.org/junit5/
     * docs/current/user-guide/#writing-tests-parameterized-tests). The test cases
     * to verify correct and wrong attempts are similar, so you can write a generic
     * test for both
     * cases and use a parameter for the assertion. See the ChallengeEventPubTest
     * source
     * code in Listing 7-7.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void sendsAttempt(boolean correct) {
        // given
        ChallengeAttempt attempt = createTestAttempt(correct);

        // when
        challengeEventPub.challengeSolved(attempt);

        // then
        var exchangeCaptor = ArgumentCaptor.forClass(String.class);
        var routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(ChallengeSolvedEvent.class);

        verify(amqpTemplate).convertAndSend(exchangeCaptor.capture(),
                routingKeyCaptor.capture(), eventCaptor.capture());
        then(exchangeCaptor.getValue()).isEqualTo("test.topic");
        then(routingKeyCaptor.getValue()).isEqualTo("attempt." +
                (correct ? "correct" : "wrong"));
        then(eventCaptor.getValue()).isEqualTo(solvedEvent(correct));
    }

    private ChallengeAttempt createTestAttempt(boolean correct) {
        return new ChallengeAttempt(1L, new User(10L, "john"), 30, 40,
                correct ? 1200 : 1300, correct);
    }

    private ChallengeSolvedEvent solvedEvent(boolean correct) {
        return new ChallengeSolvedEvent(1L, correct, 30, 40, 10L, "john");
    }

}