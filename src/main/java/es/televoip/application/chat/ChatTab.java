package es.televoip.application.chat;

import com.vaadin.flow.component.tabs.Tab;

public class ChatTab extends Tab implements Comparable<ChatTab> {

   private ChatInfo chatInfo;

   public ChatTab(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
   }

   public ChatInfo getChatInfo() {
      return chatInfo;
   }

   public void setChatInfo(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
   }

   @Override
   public int compareTo(ChatTab otherTab) {
      // Compara los chats por la cantidad de mensajes no leídos en orden descendente
      return Integer.compare(otherTab.getChatInfo().getUnread(), chatInfo.getUnread());
   }

}
