package microservices.book.multiplication.challenge;

import lombok.*;
import microservices.book.multiplication.user.User;
import jakarta.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeAttempt {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private int factorA;

    private int factorB;

    private int resultAttempt;

    private boolean correct;
}