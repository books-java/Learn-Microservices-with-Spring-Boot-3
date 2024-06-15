package microservices.book.multiplication.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByAlias(final String alias);

    /*
     * perform a select in the users
     * table, filtering those users whose identifiers are in the passed list
     */
    List<User> findAllByIdIn(final List<Long> ids);
}