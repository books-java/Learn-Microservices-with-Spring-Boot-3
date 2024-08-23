package microservices.book.gamification.game.domain;

import lombok.*;
import jakarta.persistence.*;

/**
 * This class represents the Score linked to an attempt in the game,
 * with an associated user and the timestamp in which the score
 * is registered.
 * You’ll assign points to every correct answer that users submit. To keep it
 * simple,
 * you’ll give points only if they send a correct attempt—ten points each time.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreCard {
    // The default score assigned to this card, if not specified.
    public static final int DEFAULT_SCORE = 10;
    @Id
    @GeneratedValue
    private Long cardId;
    private Long userId;
    private Long attemptId;
    /*
     * this will make Lombok omit that field in the generated equals
     * and hashCode methods. The reason is that this will make your tests easier
     * when you
     * compare objects, and in fact, you don’t need the timestamp to determine
     * whether two
     * cards are equal.
     * 
     */
    @EqualsAndHashCode.Exclude
    private long scoreTimestamp;
    private int score;

    public ScoreCard(final Long userId, final Long attemptId) {
        this(null, userId, attemptId, System.currentTimeMillis(),
                DEFAULT_SCORE);
    }
}