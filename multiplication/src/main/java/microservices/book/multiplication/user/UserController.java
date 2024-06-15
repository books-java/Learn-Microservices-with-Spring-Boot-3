package microservices.book.multiplication.user;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * you need to add a second change to the Multiplication
microservice: a controller to retrieve a collection of user aliases based on their
identifiers. You need this because the leaderboard API you implemented in the
LeaderBoardController class returns the score, badges, and position based on user
IDs. The UI needs a way to map each ID to a user alias, to render the table in a friendlier
manner
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/{idList}")
    public List<User> getUsersByIdList(@PathVariable final List<Long> idList) {
        return userRepository.findAllByIdIn(idList);
    }
}