package es.televoip.application.service;

import es.televoip.application.chat.ChatInfo;
import es.televoip.application.repository.ChatInfoRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatInfoService {

   @Autowired
   private ChatInfoRepository chatInfoRepository;

   public void saveChat(ChatInfo chatInfo) {
      chatInfoRepository.save(chatInfo);
   }

   public Integer getUnreadCount(String phone) {
      Optional<ChatInfo> chatInfoOptional = chatInfoRepository.findByPhone(phone);
      return chatInfoOptional.map(ChatInfo::getUnread).orElse(0);
   }

   public void updateUnreadCount(String phone, Integer unreadCount) {
      Optional<ChatInfo> chatInfoOptional = chatInfoRepository.findByPhone(phone);
      if (chatInfoOptional.isPresent()) {
         ChatInfo chatInfo = chatInfoOptional.get();
         chatInfo.setUnread(unreadCount);
         chatInfoRepository.save(chatInfo);
      }
   }

}
