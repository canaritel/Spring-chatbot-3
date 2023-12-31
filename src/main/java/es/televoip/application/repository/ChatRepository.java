package es.televoip.application.repository;

import es.televoip.application.model.ChatEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

   List<ChatEntity> findBySender(String sender);

   @Query("SELECT c FROM ChatEntity c WHERE c.phone = :phone")
   List<ChatEntity> getAllListByPhone(String phone);

   List<ChatEntity> findAllByPhone(String phone);

   Optional<ChatEntity> findByPhone(String phone);
   
   //@Modifying solo se usa en métodos personalizados que realizan operaciones de tipo Actualización o Eliminación!!!!

}
