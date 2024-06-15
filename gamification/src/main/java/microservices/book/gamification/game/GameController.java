package microservices.book.gamification.game;

import lombok.RequiredArgsConstructor;
import microservices.book.gamification.challenge.ChallengeSolvedDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/*
 * While designing the Gamification domain, you agreed on a contract with the
 *Multiplication service. It’ll send each attempt to a REST endpoint on the gamification
 *side. It’s time to build that controller
 */

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
/*
 * There is a REST API available on POST /attempts that accepts a JSON object
 * containing data about the user and the challenge. In this case, you don’t need to return
 * any content, so you can use the ResponseStatus annotation to configure Spring to
 * return a 200 OK status code. Actually, this is the default behavior when a controller’s
 * method returns void and has been processed without errors. In any case, it’s good to
 * add it explicitly for better readability. Remember that if there is an error like a thrown
 * exception, for example, Spring Boot’s default error handling logic will intercept it and
 * return an error response with a different status code
 */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    void postResult(@RequestBody ChallengeSolvedDTO dto) {
        gameService.newAttemptForUser(dto);
    }
}