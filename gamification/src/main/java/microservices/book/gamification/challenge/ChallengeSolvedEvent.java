package microservices.book.gamification.challenge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/*
 * Following domain-driven design practices, you could adjust this event’s deserialized
fields. For instance, you don’t need the userAlias for the Gamification’s business
logic, so you could remove it from the consumed event. Since Spring Boot configures
the ObjectMapper to ignore unknown properties by default, that strategy would work
without needing to configure anything else. Not sharing the code of this class across
microservices is a good practice because it also allows for loose coupling, backward
compatibility, and independent deployments. Imagine that the Multiplication
microservice would evolve and store extra data, for example, a third factor for harder
challenges. This extra factor would then be added to the published event’s code. The
good news is that, by using different representations of the event per domain and
configuring the mapper to ignore unknown properties, the Gamification microservice
would still work after such change without needing to update its event representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeSolvedEvent {
    long attemptId;
    boolean correct;
    int factorA;
    int factorB;
    long userId;
    String userAlias;
}