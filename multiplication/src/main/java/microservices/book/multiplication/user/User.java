package microservices.book.multiplication.user;

import lombok.*;
import jakarta.persistence.*;

/**
 * Stores information to identify the user.
 */
@Entity(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;
    
    private String alias;

    public User(final String userAlias) {
        this(null, userAlias);
    }
}