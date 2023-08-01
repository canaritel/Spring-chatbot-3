package es.televoip.application.repository;

import es.televoip.application.model.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

   Optional<UserEntity> findByPhone(String phone);
   
   //@Modifying solo se usa en métodos personalizados que realizan operaciones de tipo Actualización o Eliminación!!!!

}
