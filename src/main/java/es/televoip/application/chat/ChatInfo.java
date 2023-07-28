package es.televoip.application.chat;

import com.vaadin.flow.component.html.Span;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class ChatInfo {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(unique = true)
   private final String phone; // identificador único

   @Transient // este campo no se grabará en la BD
   private String name; // una vez creado no se podrá modificar, es un identificador único

   @Column(nullable = false) // Asegura que el campo no sea nulo en la base de datos
   private Integer unread;

   @Transient // este campo no se grabará en la BD
   private Span unreadBadge = new Span("");

   public ChatInfo(String phone, String name, Integer unread) {
      this.phone = phone;
      this.name = name;
      this.unread = unread;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
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

   public void setUnread(Integer unread) {
      this.unread = unread;
      this.updateBadge();
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

   public String getPhone() {
      return phone;
   }

}
