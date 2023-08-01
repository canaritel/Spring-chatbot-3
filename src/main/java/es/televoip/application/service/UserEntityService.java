package es.televoip.application.service;

import es.televoip.application.model.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.televoip.application.repository.UserEntityRepository;

@Service
@Transactional
public class UserEntityService {

   @Autowired
   private UserEntityRepository userRepository;

   public void updateUserNick(UserEntity userNickname) {
      String phone = userNickname.getPhone();
      String nickname = userNickname.getNickname();
      Optional<UserEntity> objectUserOptional = userRepository.findByPhone(phone);
      if (objectUserOptional.isPresent()) {
         UserEntity objectUser = objectUserOptional.get();
         objectUser.setNickname(nickname);
         userRepository.save(objectUser);
      } else {
         UserEntity newUser = new UserEntity();
         newUser.setPhone(phone);
         newUser.setNickname(nickname);
         userRepository.save(newUser);
      }
   }

   public List<UserEntity> searchAll() {
      return userRepository.findAll();
   }

   public String getNickUserFromPhone(String phone) {
      Optional<UserEntity> objectUser = userRepository.findByPhone(phone);
      if (objectUser.isPresent()) {
         return objectUser.get().getNickname();
      } else {
         return "";
      }
   }

}
