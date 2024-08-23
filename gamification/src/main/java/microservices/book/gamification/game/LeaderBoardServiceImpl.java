package microservices.book.gamification.game;

import lombok.RequiredArgsConstructor;
import microservices.book.gamification.game.domain.LeaderBoardRow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class LeaderBoardServiceImpl implements LeaderBoardService {

    private final ScoreRepository scoreRepository;
    private final BadgeRepository badgeRepository;

    @Override
    public List<LeaderBoardRow> getCurrentLeaderBoard() {
        // Get score only
        List<LeaderBoardRow> scoreOnly = scoreRepository.findFirst10();
        // Combine with badges
        /*
         * this code used the withBadges method to copy an immutable object with a new
         * value
         * The first time you generate the leaderboard, all rows have an empty list of
         * badges. When you collect the badges, you can replace (using stream’s map)
         * each object
         * with a copy with the corresponding badge list
         */
        return scoreOnly
                .stream()
                .map(row -> {
                    List<String> badges = badgeRepository
                            .findByUserIdOrderByBadgeTimestampDesc(row.getUserId())
                            .stream()
                            .map(b -> b.getBadgeType().getDescription())
                            .collect(Collectors.toList());
                    return row.withBadges(badges);
                })
                .collect(Collectors.toList());
    }
}