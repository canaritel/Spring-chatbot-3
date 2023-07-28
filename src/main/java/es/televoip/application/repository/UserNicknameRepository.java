package es.televoip.application.repository;

import es.televoip.application.model.UserNickname;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNicknameRepository extends JpaRepository<UserNickname, Long> {

   Optional<UserNickname> findByPhone(String phone);

}
