package es.televoip.application.chat;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;

public class ChatTab extends Tab implements Comparable<ChatTab> {
   
   private ChatInfo chatInfo;
   private String nickUser;  // Nick para el usuario cuando lo registramos
   private EditButton editButton;
   
   public ChatTab(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
      this.editButton = new EditButton();
      editButton.setVisible(false);
      
      HorizontalLayout contentLayout = new HorizontalLayout();
      contentLayout.add(new Span("#" + chatInfo.getName()), editButton);
      contentLayout.setPadding(false);
      contentLayout.setMargin(false);
      contentLayout.setSpacing(false);
      contentLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
      
      add(contentLayout);
   }
   
   public void setVisibleEditButton(boolean visible) {
      this.editButton.setVisible(visible);
   }
   
   public ChatInfo getChatInfo() {
      return chatInfo;
   }
   
   public void setChatInfo(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
   }
   
   public String getNickUser() {
      return nickUser;
   }
   
   public void setNickUser(String nickUser) {
      this.nickUser = nickUser;
   }
   
   @Override
   public int compareTo(ChatTab otherTab) {
      // Permite comparar los chats por la cantidad de mensajes no leídos en orden descendente
      return Integer.compare(otherTab.getChatInfo().getUnread(), chatInfo.getUnread());
   }
   
}
