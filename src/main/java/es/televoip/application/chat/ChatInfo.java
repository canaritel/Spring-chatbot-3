package es.televoip.application.chat;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageListItem;
import java.util.ArrayList;
import java.util.List;

public class ChatInfo {

   private final String phone; // identificador único
   private final String name; // una vez creado no se podrá modificar, es un identificador único
   private String nick; // se usará cuando se hagan modificacione del nombre del usuario
   private final List<MessageListItem> listMessages;
   private int unread;
   private Span unreadBadge = new Span("");

   public ChatInfo(String phone, String name, int unread) {
      this.phone = phone;
      this.name = name;
      this.listMessages = new ArrayList<>();
      this.unread = unread;
   }

   public String getName() {
      return name;
   }

   public List<MessageListItem> getMessages() {
      return listMessages;
   }

   public void resetUnread() {
      unread = 0;
      updateBadge();
   }

   public void incrementUnread() {
      unread++;
      updateBadge();
   }

   private void updateBadge() {
      unreadBadge.setText(unread + "");
      unreadBadge.setVisible(unread != 0);
   }

   public void setUnreadBadge(Span unreadBadge) {
      this.unreadBadge = unreadBadge;
      updateBadge();
   }

   public String getCollaborationTopic() {
      return "chat/" + name;
   }

   public String getNick() {
      return nick;
   }

   public void setNick(String nick) {
      this.nick = nick;
   }

   public String getPhone() {
      return phone;
   }

}