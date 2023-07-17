package es.televoip.application.chat;

import com.vaadin.flow.component.tabs.Tab;

public class ChatTab extends Tab {

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

}
