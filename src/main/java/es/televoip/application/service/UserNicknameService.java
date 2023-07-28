package es.televoip.application.service;

import es.televoip.application.model.UserNickname;
import es.televoip.application.repository.UserNicknameRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserNicknameService {

   @Autowired
   private UserNicknameRepository userNicknameRepository;

   public void updateUserNick(UserNickname userNickname) {
      String phone = userNickname.getPhone();
      String nickname = userNickname.getNickname();
      Optional<UserNickname> objectNickOptional = userNicknameRepository.findByPhone(phone);
      if (objectNickOptional.isPresent()) {
         UserNickname userNick = objectNickOptional.get();
         userNick.setNickname(nickname);
         userNicknameRepository.save(userNick);
      } else {
         UserNickname newUserNickname = new UserNickname();
         newUserNickname.setPhone(phone);
         newUserNickname.setNickname(nickname);
         userNicknameRepository.save(newUserNickname);
      }
   }

   public List<UserNickname> searchAll() {
      return userNicknameRepository.findAll();
   }

}
