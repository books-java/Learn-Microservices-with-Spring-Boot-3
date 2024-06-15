package microservices.book.gamification.game;

import microservices.book.gamification.game.domain.LeaderBoardRow;
import microservices.book.gamification.game.domain.ScoreCard;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Handles CRUD operations with ScoreCards and other related score queries
 * For scorecards, you need other query types. There are three requirements thus far.
 * 1. Calculate the total score of a user.
 * 2. Get a list of users with the highest score, ordered, as
 * LeaderBoardRow objects.
 * 3. Read all ScoreCard records by user ID.
 */
public interface ScoreRepository extends CrudRepository<ScoreCard, Long> {
    /**
     * Gets the total score for a given user: the sum of the scores of all
     * their ScoreCards.
     * Unfortunately, Spring Data JPA’s query methods don’t support aggregations. The
     * good news is that JPQL, the JPA Query Language, does support them, so you can use
     * standard syntax to keep your code as database-agnostic as possible. You can get the total
     * score for a given user with this query:
     * @param userId the id of the user
     * @return the total score for the user, empty if the user doesn't exist
     */
    @Query("SELECT SUM(s.score) FROM ScoreCard s WHERE s.userId = :userId GROUP BY s.userId")
    Optional<Integer> getTotalScoreForUser(@Param("userId") Long userId);

    /**
     * Retrieves a list of {@link LeaderBoardRow}s representing the Leader Board
     * of users and their total score.
     * In JPQL, you can use the constructors available in
     * your Java classes. What you do in this example is an aggregation based on the total score,
     * and you construct LeaderBoardRow objects using the two-argument constructor you
     * defined (which sets an empty list of badges)
     *
     * @return the leader board, sorted by highest score first.
     */
    @Query("SELECT NEW microservices.book.gamification.game.domain.LeaderBoardRow(s.userId, SUM(s.score)) " +
            "FROM ScoreCard s " +
            "GROUP BY s.userId ORDER BY SUM(s.score) DESC")
    List<LeaderBoardRow> findFirst10();

    /**
     * Retrieves all the ScoreCards for a given user, identified by his user id.
     *
     * @param userId the id of the user
     * @return a list containing all the ScoreCards for the given user,
     *         sorted by most recent.
     */
    List<ScoreCard> findByUserIdOrderByScoreTimestampDesc(final Long userId);

}