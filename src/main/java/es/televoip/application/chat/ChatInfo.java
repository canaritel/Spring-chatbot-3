package es.televoip.application.chat;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageListItem;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ChatInfo {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private final String phone; // identificador único
   private String name; // una vez creado no se podrá modificar, es un identificador único
   private String nick; // nick del acceso a  la sesión
   private final List<MessageListItem> listMessages;
   private Integer unread;
   private Span unreadBadge = new Span("");

   public ChatInfo(String phone, String name, Integer unread, String nick) {
      this.phone = phone;
      this.name = name;
      this.listMessages = new ArrayList<>();
      this.unread = unread;
      this.nick = nick;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<MessageListItem> getMessages() {
      return listMessages;
   }

   public synchronized void resetUnread() {
      unread = 0;
      updateBadge();
   }

   public synchronized void incrementUnread() {
      unread++;
      updateBadge();
   }

   public synchronized Integer getUnread() {
      return unread;
   }

   public synchronized void updateBadge() {
      unreadBadge.setText(unread + "");
      unreadBadge.setVisible(unread != 0);
   }

   public synchronized void setUnreadBadge(Span unreadBadge) {
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
