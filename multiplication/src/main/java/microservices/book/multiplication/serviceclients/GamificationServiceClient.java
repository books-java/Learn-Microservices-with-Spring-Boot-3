package microservices.book.multiplication.serviceclients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import microservices.book.multiplication.challenge.ChallengeAttempt;
import microservices.book.multiplication.challenge.ChallengeSolvedDTO;

/*
 * Now you have
to integrate both microservices together by communicating Multiplication with the
new one.
Previously, you created a few REST APIs on the server’s side. This time, you have
to build a REST API Client instead. The Spring Web module offers a tool for that
purpose: the RestTemplate class. Spring Boot provides an extra layer on top: the
RestTemplateBuilder. This builder is injected by default when you use the Spring Boot
Web starter, and you can use its methods to create RestTemplate objects in a fluent
way with multiple configuration options. You can add specific message converters,
security credentials if you need them to access the server, HTTP interceptors, and
so on. In this case, you can use the default settings since both applications are using
Spring Boot’s predefined configuration. That means that the serialized JSON object
sent by the RestTemplate can be deserialized without problems on the server’s side (the
Gamification microservice).
To keep this implementation modular, you need to create the Gamification’s REST
client in a separate class: GamificationServiceClient
 */
/*
 * This new Spring @Service can be injected into your existing ones. It uses the builder
to initialize the RestTemplate with defaults (just calling build()). It also accepts in the
constructor the host URL of the gamification service, which you want to extract as a
configuration parameter
 */
@Slf4j
@Service
public class GamificationServiceClient {
    private final RestTemplate restTemplate;
    private final String gamificationHostUrl;

    public GamificationServiceClient(
            final RestTemplateBuilder builder,
            @Value("${service.gamification.host}") final String gamificationHostUrl) {
        restTemplate = builder.build();
        this.gamificationHostUrl = gamificationHostUrl;
    }

    public boolean sendAttempt(final ChallengeAttempt attempt) {
        try {
            ChallengeSolvedDTO dto = new ChallengeSolvedDTO(attempt.getId(),
                    attempt.isCorrect(), attempt.getFactorA(),
                    attempt.getFactorB(), attempt.getUser().getId(),
                    attempt.getUser().getAlias());
            ResponseEntity<String> r = restTemplate.postForEntity(
                    gamificationHostUrl + "/attempts", dto,
                    String.class);
            log.info("Gamification service response: {}", r.getStatusCode());
            return r.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("There was a problem sending the attempt.", e);
            return false;
        }
    }
}
