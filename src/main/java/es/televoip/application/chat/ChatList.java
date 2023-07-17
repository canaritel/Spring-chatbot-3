package es.televoip.application.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChatList {

   private final LinkedList<ChatInfo> chats = new LinkedList<>();

   public synchronized void addChat(ChatInfo chat) {
      // check if chat with same user already exists
      for (int i = 0; i < chats.size(); i++) {
         ChatInfo currentChat = chats.get(i);
         if (currentChat.getName().equals(chat.getName())) {
            // chat with same user already exists, remove and add at top
            chats.remove(i);
            break;
         }
      }

      // add chat at top
      chats.addFirst(chat);
   }

   public synchronized ChatInfo getChat() {
      return chats.pollLast();
   }

   public synchronized List<ChatInfo> getAllChats() {
      return new ArrayList<>(chats);
   }

   public synchronized boolean hasChats() {
      return !chats.isEmpty();
   }

   public synchronized void clearChats() {
      chats.clear();
   }

}
