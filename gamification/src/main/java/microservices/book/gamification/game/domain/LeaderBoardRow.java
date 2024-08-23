package microservices.book.gamification.game.domain;

import lombok.*;
import java.util.List;

@Value
@AllArgsConstructor
public class LeaderBoardRow {
  Long userId;

  Long totalScore;

  /*
   * The @With annotation added to the badges field is provided by Lombok and
   * generates a method for you to clone an
   * object and add a new field value to the copy (in this case, withBadges). This
   * is a good
   * practice when you work with immutable classes since they don’t have setters.
   * You’ll use
   * this method when
   * you create the business logic to merge the score and the badges for each
   * leaderboard row
   * will generate code like this
   * 
   * @Generated
   * public LeaderBoardRow withBadges(final List<String> badges) {
   * return this.badges == badges ? this : new LeaderBoardRow(this.userId,
   * this.totalScore, badges);
   * }
   */
  @With
  List<String> badges;

  public LeaderBoardRow(final Long userId, final Long totalScore) {
    this.userId = userId;
    this.totalScore = totalScore;
    this.badges = List.of();
  }
}
