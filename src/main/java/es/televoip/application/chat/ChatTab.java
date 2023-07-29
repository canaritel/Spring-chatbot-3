package es.televoip.application.chat;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import es.televoip.application.broadcast.Broadcaster;

public class ChatTab extends Tab implements Comparable<ChatTab> {

   private ChatInfo chatInfo;
   private String nickUser;  // Nick para el usuario cuando lo registramos
   private EditButton editButton;

   public ChatTab(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
      this.editButton = new EditButton();
      this.nickUser = "";
      editButton.setVisible(false);

      // Creamos el botón de edición
      editButton.addClickListener(event -> showEditDialog());

      updateTabContent();
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

//   public void updateNickUser(String nickUser) {
//      this.nickUser = nickUser;
//      updateTabContent(); //////////////
//   }
   private void showEditDialog() {
      Dialog editDialog = new Dialog();
      editDialog.setCloseOnEsc(true);
      editDialog.setCloseOnOutsideClick(true);

      // Creamos el componente para el ingreso de texto
      TextField nickInput = new TextField();
      nickInput.setLabel("Ingrese su nick de usuario");
      nickInput.setValue(chatInfo.getName());

      // Creamos el botón para guardar el nick
      Button saveButton = new Button("Guardar", event -> {
         String newNick = nickInput.getValue();
         setNickUser(newNick);
         editDialog.close();

         // Enviamos el nuevo nick a través del Broadcaster
         Broadcaster.broadcastNickChange(chatInfo.getPhone(), newNick);
      });

      saveButton.addClickShortcut(Key.ENTER);

      // Agregamos los componentes al diálogo
      editDialog.add(nickInput, saveButton);
      editDialog.open();
   }

   public void updateTabContent() {
      // Actualizamos el contenido del Tab con el nuevo nick
      HorizontalLayout contentLayout = new HorizontalLayout();
      if (this.nickUser.isBlank()) {
         contentLayout.add(new Span("#" + chatInfo.getName()), editButton);
      } else {
         contentLayout.add(new Span(nickUser), editButton);
      }
      contentLayout.setPadding(false);
      contentLayout.setMargin(false);
      contentLayout.setSpacing(false);
      contentLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

      removeAll();
      add(contentLayout);
   }

   @Override
   public int compareTo(ChatTab otherTab) {
      // Permite comparar los chats por la cantidad de mensajes no leídos en orden descendente
      return Integer.compare(otherTab.getChatInfo().getUnread(), chatInfo.getUnread());
   }

}
