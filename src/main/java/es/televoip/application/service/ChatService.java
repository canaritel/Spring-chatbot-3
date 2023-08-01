package es.televoip.application.service;

import es.televoip.application.model.ChatEntity;
import es.televoip.application.repository.ChatRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatService {

   @Autowired
   private ChatRepository chatRepository;

   public void saveChat(ChatEntity chatEntity) {
      chatRepository.save(chatEntity);
   }

   public void updateChat(String phone, ChatEntity chatEntity) {
      Optional<ChatEntity> existingChat = chatRepository.findByPhone(phone);
      if (existingChat.isPresent()) {
         ChatEntity chatToUpdate = existingChat.get();
         chatToUpdate.setText(chatEntity.getText());
         chatToUpdate.setTimestamp(chatEntity.getTimestamp());
         chatToUpdate.setSender(chatEntity.getSender());
         chatRepository.save(chatToUpdate);
      } else {
         chatRepository.save(chatEntity);
      }
   }

   public void deleteChatByPhone(ChatEntity chatEntity) {
      chatRepository.delete(chatEntity);
   }

   public List<ChatEntity> searchAll() {
      return chatRepository.findAll();
   }

   public List<ChatEntity> searchBySender(String sender) {
      return chatRepository.findBySender(sender);
   }

   public List<ChatEntity> searchByPhone(String phone) {
      return chatRepository.getAllListByPhone(phone);
      //return chatRepository.findAllByPhone(phone);
   }

}
