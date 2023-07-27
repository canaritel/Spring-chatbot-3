package es.televoip.application.views.chat;

import es.televoip.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Aside;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import es.televoip.application.Application;
import es.televoip.application.broadcast.Broadcaster;
import es.televoip.application.chat.ChatInfo;
import es.televoip.application.chat.ChatList;
import es.televoip.application.chat.ChatTab;
import es.televoip.application.listeners.ServiceListener;
import es.televoip.application.model.ChatEntity;
import es.televoip.application.service.ChatService;
import jakarta.servlet.ServletContextListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Chat2")
@Route(value = "chat2", layout = MainLayout.class)
public class ChatView extends HorizontalLayout implements ServletContextListener {

   private final ChatService chatService;
   private final UI ui;
   private Tabs tabs;
   private final MessageList messageGlobalList;
   private final Map<String, MessageList> chatsMap = new HashMap<>();
   private Integer contador = 0;
   private ChatInfo selectedChat;
   private ChatTab selectedTab; // Mantener el tab seleccionado actualmente
   private ChatList chatList;
   // Todas las "instancias/sesiones" de ChatView compartirán la misma variable textChat 
   // y podrán acceder a la última versión del mensaje enviado.
   private static String textChat = ""; // "static" sino solo se actualizaría en la instancia específica donde se envió el mensaje.
   protected Registration broadcasterRegistration; // Recibir transmisiones Broadcaster

   private Notification notificationShown = new Notification();
   private boolean isNotificationShown = false;

   @Autowired
   private ServiceListener serviceListener;

   public ChatView(ChatService chatService) {
      this.chatService = chatService;
      this.ui = UI.getCurrent();
      ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

      addClassNames("chat-view",
             LumoUtility.Width.FULL,
             LumoUtility.Display.FLEX,
             LumoUtility.Flex.AUTO);
      setSpacing(false);

      messageGlobalList = new MessageList();
      messageGlobalList.setSizeFull();

      tabs = new Tabs();
      tabs.setAutoselect(false);
      tabs.setOrientation(Tabs.Orientation.VERTICAL);
      tabs.addClassNames(LumoUtility.Flex.GROW,
             LumoUtility.Flex.SHRINK,
             LumoUtility.Overflow.HIDDEN);

      chatList = createChatList();
      ChatInfo currentChat = chatList.getChat();
      List<ChatInfo> allChats = chatList.getAllChats();

      for (ChatInfo chat : allChats) {
         MessageList localmessageList = new MessageList();
         localmessageList.setSizeFull();
         chatsMap.put(chat.getName(), localmessageList);
         if (currentChat != chat) {
            chat.incrementUnread();  ////////////////////////////////////////////////////////////
         }

         ChatTab tab = createTab(chat);
         tabs.add(tab);
      }

      Aside side = createAside();
      VerticalLayout chatContainer = createChatContainer();

      add(chatContainer, side); // Agrega el Label al layout
      setSizeFull();
      expand(chatContainer);

      tabs.addSelectedChangeListener(event -> {
         selectedTab = (ChatTab) event.getSelectedTab();
         if (selectedTab != null) {
            selectedChat = selectedTab.getChatInfo();
            selectedChat.resetUnread();

            notificationShown.close();
            loadChats();

            Application.selectedChat = selectedChat; // guardamos el Chat seleccionado
         }
      });
   }

   private ChatTab createTab(ChatInfo chat) {
      ChatTab tab = new ChatTab(chat);
      tab.addClassNames(LumoUtility.JustifyContent.BETWEEN);

      Span badge = new Span();
      chat.setUnreadBadge(badge);
      badge.getElement().getThemeList().add("badge small contrast");
      tab.add(new Span("#" + chat.getName()), badge);

      return tab;
   }

   private void sortTabsByUnread() {
      List<ChatTab> tabList = new ArrayList<>();
      tabs.getChildren().forEach(component -> {
         if (component instanceof ChatTab) {
            tabList.add((ChatTab) component);
         }
      });

      Collections.sort(tabList);

      // Creamos una lista auxiliar para almacenar los tabs ordenados
      List<ChatTab> sortedTabs = new ArrayList<>(tabList);

      // Limpiamos los tabs actuales del contenedor
      tabs.removeAll();

      // Agregamos los tabs ordenados al contenedor
      sortedTabs.forEach(tab -> tabs.add(tab));
   }

   private ChatList createChatList() {
      if (chatList == null) {
         chatList = new ChatList();
         Integer atomicInt = 0;
         chatList.addChat(new ChatInfo("0", "inicio-no-coge", atomicInt));
         chatList.addChat(new ChatInfo("34111", "uno", atomicInt));
         chatList.addChat(new ChatInfo("34222", "dos", atomicInt));
         chatList.addChat(new ChatInfo("34333", "tres", atomicInt));
         chatList.addChat(new ChatInfo("34444", "cuatro", atomicInt));
         chatList.addChat(new ChatInfo("34555", "cinco", atomicInt));
      }

      return chatList;
   }

   private Aside createAside() {
      Aside side = new Aside();
      side.addClassNames(LumoUtility.Display.FLEX,
             LumoUtility.FlexDirection.COLUMN,
             LumoUtility.Flex.GROW_NONE,
             LumoUtility.Flex.SHRINK_NONE,
             LumoUtility.Background.CONTRAST_5);
      side.setWidth("18rem");

      Header header = new Header();
      header.addClassNames(LumoUtility.Display.FLEX,
             LumoUtility.FlexDirection.ROW,
             LumoUtility.Width.FULL,
             LumoUtility.AlignItems.CENTER,
             LumoUtility.Padding.MEDIUM,
             LumoUtility.BoxSizing.BORDER);

      H3 channels = new H3("Usuarios");
      channels.addClassNames(LumoUtility.Flex.GROW,
             LumoUtility.Margin.NONE);

      //  ChatTab currentTab = (ChatTab) tabs.getSelectedTab();
      //    if (tabs.getSelectedTab() != null) {
      //    selectedChat = currentTab.getChatInfo();
      //    selectedChat.resetUnread(); ////////////// RESETERO EL CONTADOR DEL TAB SELECCIONADO
      //    }
      MessageInput input = createMessageInput();

      header.add(channels);
      side.add(header, tabs);

      return side;
   }

   private VerticalLayout createChatContainer() {
      VerticalLayout chatContainer = new VerticalLayout();
      chatContainer.addClassNames(LumoUtility.Flex.AUTO, LumoUtility.Overflow.HIDDEN);
      chatContainer.add(messageGlobalList, createMessageInput());

      return chatContainer;
   }

   private MessageInput createMessageInput() {
      MessageInput input = new MessageInput();
      input.setWidthFull();

      input.addSubmitListener(userMessage -> {
         String phoneUser = selectedChat.getPhone();
         String nameUser = selectedChat.getName();
         textChat = userMessage.getValue();

         MessageListItem newMessage = new MessageListItem(textChat, Instant.now(), nameUser);
         newMessage.setUserColorIndex(3);
         newMessage.addThemeNames("chat-view-bubble");

         ChatEntity chatEntity = ChatEntity.builder()
                .phone(phoneUser)
                .text(textChat)
                .timestamp(newMessage.getTime())
                .sender(nameUser)
                .build();
         chatService.saveChat(chatEntity);

         MessageList messageList = chatsMap.get(selectedChat.getName());
         List<MessageListItem> items = new ArrayList<>(messageList.getItems());
         items.add(newMessage);
         messageList.setItems(items);
         messageGlobalList.setItems(items);

         contador++;
         String answer = textChat + " | ..respuesta" + contador;
         String nickname = VaadinSession.getCurrent().getAttribute("nickname").toString();
         MessageListItem botMessage = new MessageListItem(answer, Instant.now(), nickname);
         botMessage.setUserAbbreviation("Bot");
         botMessage.setUserColorIndex(2);
         botMessage.addThemeNames("chat-view-bubble-bot");

         ChatEntity chatReceiver = ChatEntity.builder()
                .text(botMessage.getText())
                .timestamp(botMessage.getTime())
                .sender(nameUser)
                .receiver(botMessage.getUserName())
                .build();

        // ui.access(() -> {
            Application.selectedChat = selectedChat; // cuando creo un mensaje se guarda el chat de quien lo hizo
            chatService.saveChat(chatReceiver);
            items.add(botMessage);
            messageList.setItems(items);
            messageGlobalList.setItems(items);

            // ***** ENVIAMOS EL BROADCASTER *****
            Broadcaster.broadcast(items);
        // });
         //***************************************
      });

      return input;
   }

   private void loadChats() {
      List<ChatEntity> chatEntities = chatService.searchBySender(selectedChat.getName());
      List<MessageListItem> newMessages = new ArrayList<>();

      for (ChatEntity objectChatEntity : chatEntities) {
         String objectPhone = objectChatEntity.getPhone();
         String objectSender = objectChatEntity.getSender();
         Instant objectTime = objectChatEntity.getTimestamp();
         String objectText = objectChatEntity.getText();
         String objectReceiver = objectChatEntity.getReceiver();
         String objectUser = selectedChat.getName();

         MessageListItem newMessage = new MessageListItem();
         newMessage.setUserName(objectSender);
         newMessage.setText(objectText);
         newMessage.setTime(objectTime);

         if (objectReceiver == null) {
            newMessage.addThemeNames("chat-view-bubble");
            newMessage.setUserColorIndex(3);
         } else {
            newMessage.addThemeNames("chat-view-bubble-bot");
            newMessage.setUserColorIndex(2);
            newMessage.setUserAbbreviation("Bot");
            newMessage.setUserName(objectReceiver);
         }

         newMessages.add(newMessage);
      }

      sendChatToScreen(newMessages);
   }

   private void sendChatToScreen(List<MessageListItem> newMessages) {
      MessageList messageList = chatsMap.get(selectedChat.getName());
      List<MessageListItem> items = new ArrayList<>();
      //items.addAll(messageList.getItems());  // Anulamos para evitar cargar los chats en memoria
      items.addAll(newMessages);

      messageList.setItems(items);
      messageGlobalList.setItems(items);

      System.out.println("Tab seleccionado : " + selectedChat.getName());
   }

   private void setMobile(boolean mobile) {
      tabs.setOrientation(mobile ? Tabs.Orientation.HORIZONTAL : Tabs.Orientation.VERTICAL);
   }

   @Override
   protected void onAttach(AttachEvent attachEvent) {
      // Hacemos visible el Drawer cuando se accede a la View
      MainLayout.get().setDrawerOpened(true);

      Page page = attachEvent.getUI().getPage();
      page.retrieveExtendedClientDetails(details -> setMobile(details.getWindowInnerWidth() < 940));
      page.addBrowserWindowResizeListener(e -> setMobile(e.getWidth() < 945));

      if (!isNotificationShown && tabs.getSelectedTab() == null) {
         // Mostrar la notificación solo si no se ha mostrado antes y no hay un Tab seleccionado
         notificationShown.addThemeVariants(NotificationVariant.LUMO_WARNING);

         Div text = new Div(
                new Text("Pulse sobre algún "),
                new Html("<b>usuario</b>"),
                new Text(" de la lista."),
                new HtmlComponent("br"),
                new Text("Esta ventana se cerrará automaticamente.")
         );

         Icon icon = VaadinIcon.ARROW_CIRCLE_UP.create();
         HorizontalLayout layout = new HorizontalLayout(icon, text);
         layout.setAlignItems(Alignment.CENTER);

         notificationShown.add(layout);
         notificationShown.open();
         notificationShown.setPosition(Notification.Position.MIDDLE);

         isNotificationShown = true; // Marcar la notificación como mostrada
      }

      // ******************* RECIBIMOS EL BROADCASTER *******************
      broadcasterRegistration = Broadcaster.register(newMessage -> {
         if (ui != null) {
            ChatInfo currentSelectedChat = selectedChat; // Obtenemos el chat seleccionado en la sesión activa
            ChatInfo applicationSelectedChat = Application.selectedChat; // Obtenemos el chat seleccionado del que envía

            ui.access(() -> {
               // Comparamos los phones de los chats seleccionados para NOTIFICAR solo en los mismos Tab-Chat
               if (currentSelectedChat != null && applicationSelectedChat != null
                      && currentSelectedChat.getPhone().equals(applicationSelectedChat.getPhone())) {
                  messageGlobalList.setItems(newMessage); // Si los chats seleccionados son iguales actualizamos los mensajes
               } else {
                  // Incrementar contador de mensajes no leídos
                  List<ChatInfo> allChats = chatList.getAllChats();
                  for (ChatInfo chat : allChats) {
                     if (chat.getPhone().equals(applicationSelectedChat.getPhone())) {
                        chat.incrementUnread();  // incrementamos en 1 el chat no leido 
                     }
                  }

                  sortTabsByUnread(); // Ordenar los tabs por cantidad de mensajes no leídos
               }

               // Notificamos en todas las sesiones
               Notification notification;
               String firstTenCharacters = textChat.substring(0, Math.min(textChat.length(), 20));
               String text = applicationSelectedChat.getName() + ": " + firstTenCharacters + "...";
               notification = Notification.show(text, 3000, Notification.Position.TOP_CENTER);
               notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            );
         }
      });
      // ****************** FIN BROADCASTER ***************************

      Set<UI> activeUIs = serviceListener.getActiveUIs();
      System.out.println("Sesiones activas UI Listener: " + activeUIs.size());
      System.out.println("Nick añadido: " + VaadinSession.getCurrent().getAttribute("nickname").toString());
   }

   @Override
   protected void onDetach(DetachEvent detachEvent) {
      broadcasterRegistration.remove();
      broadcasterRegistration = null;
      notificationShown.close();
   }

}
