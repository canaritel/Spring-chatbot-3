package es.televoip.application.chat;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.server.VaadinSession;
import java.util.HashSet;
import java.util.Set;

public class ChatTab extends Tab {

   private ChatInfo chatInfo;
   private final Set<VaadinSession> selectedSessions;

   public ChatTab(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
      this.selectedSessions = new HashSet<>();
   }

   public ChatInfo getChatInfo() {
      return chatInfo;
   }

   public void setChatInfo(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
   }

   public void addSelectedSession(VaadinSession session) {
      selectedSessions.add(session);
   }

   public void removeSelectedSession(VaadinSession session) {
      selectedSessions.remove(session);
   }
   
   // Obtener la lista de sesiones seleccionadas en este Tab
   public Set<VaadinSession> getSelectedSessions() {
      return selectedSessions;
   }

}
