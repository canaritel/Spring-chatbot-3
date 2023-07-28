package es.televoip.application.repository;

import es.televoip.application.chat.ChatInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatInfoRepository extends JpaRepository<ChatInfo, Long> {

   Optional<ChatInfo> findByPhone(String phone);

}
