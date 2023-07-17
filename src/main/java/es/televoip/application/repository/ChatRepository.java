package es.televoip.application.repository;

import es.televoip.application.model.ChatEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

   List<ChatEntity> findBySender(String sender);

   Optional<ChatEntity> findByPhone(String phone);

}
