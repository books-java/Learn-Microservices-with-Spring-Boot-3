package microservices.book.gamification.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.challenge.ChallengeSolvedEvent;
import microservices.book.gamification.game.badgeprocessors.BadgeProcessor;
import microservices.book.gamification.game.domain.BadgeCard;
import microservices.book.gamification.game.domain.BadgeType;
import microservices.book.gamification.game.domain.ScoreCard;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
        private final ScoreRepository scoreRepository;
        private final BadgeRepository badgeRepository;
        /*
        Since you use constructor injection in GameServiceImpl with a list of 
BadgeProcessor objects, Spring will find all the beans that implement this interface and 
pass them to you. This is a flexible way of extending your game without interfering with 
other existing logic. You just need to add new BadgeProcessor implementations and 
annotate them with @Component so they are loaded in the Spring context.

        */
        // Spring injects all the @Component beans in this list
        private final List<BadgeProcessor> badgeProcessors;

        /*
         * The output after processing the attempt is a GameResult object, defined
         * within the
         * interface. It groups the score obtained from that attempt together with any
         * new badge
         * that the user may get. You could also consider not returning anything since
         * it’ll be the
         * leaderboard logic showing the results. However, it’s better to have a
         * response from your
         * method so you can test it.
         * 
         */
        @Override
        public GameResult newAttemptForUser(ChallengeSolvedEvent challenge) {
                /*
                 * You could remove the check for the correct attempt, but then you would depend
                 * too much on proper routing on the Multiplication microservice. If you keep
                 * it, it’s easier
                 * for everyone to read the code and know what it does without having to figure
                 * out that
                 * there is a filter logic based on routing keys. You can benefit from the
                 * broker’s routing, but
                 * remember that you don’t want to embed too much behavior inside the channel.
                 */
                // We give points only if it's correct
                if (challenge.isCorrect()) {
                        ScoreCard scoreCard = new ScoreCard(challenge.getUserId(),
                                        challenge.getAttemptId());
                        scoreRepository.save(scoreCard);
                        log.info("User {} scored {} points for attempt id {}",
                                        challenge.getUserAlias(), scoreCard.getScore(),
                                        challenge.getAttemptId());
                        List<BadgeCard> badgeCards = processForBadges(challenge);
                        return new GameResult(scoreCard.getScore(),
                                        badgeCards.stream().map(BadgeCard::getBadgeType)
                                                        .collect(Collectors.toList()));
                } else {
                        log.info("Attempt id {} is not correct. " +
                                        "User {} does not get score.",
                                        challenge.getAttemptId(),
                                        challenge.getUserAlias());
                        return new GameResult(0, List.of());
                }
        }

        /**
         * Checks the total score and the different score cards obtained
         * to give new badges in case their conditions are met.
         */
        private List<BadgeCard> processForBadges(
                        final ChallengeSolvedEvent solvedChallenge) {
                Optional<Integer> optTotalScore = scoreRepository.getTotalScoreForUser(solvedChallenge.getUserId());
                if (optTotalScore.isEmpty())
                        return Collections.emptyList();
                int totalScore = optTotalScore.get();

                // Gets the total score and existing badges for that user
                List<ScoreCard> scoreCardList = scoreRepository
                                .findByUserIdOrderByScoreTimestampDesc(solvedChallenge.getUserId());
                Set<BadgeType> alreadyGotBadges = badgeRepository
                                .findByUserIdOrderByBadgeTimestampDesc(solvedChallenge.getUserId())
                                .stream()
                                .map(BadgeCard::getBadgeType)
                                .collect(Collectors.toSet());

                // Calls the badge processors for badges that the user doesn't have yet
                List<BadgeCard> newBadgeCards = badgeProcessors
                                .stream()
                                .filter(bp -> !alreadyGotBadges.contains(bp.badgeType()))
                                .map(bp -> bp.processForOptionalBadge(totalScore, scoreCardList, solvedChallenge))
                                .flatMap(Optional::stream) // returns an empty stream if empty
                                // maps the optionals if present to new BadgeCards
                                .map(badgeType -> new BadgeCard(solvedChallenge.getUserId(), badgeType))
                                .collect(Collectors.toList());

                badgeRepository.saveAll(newBadgeCards);

                return newBadgeCards;
        }
}