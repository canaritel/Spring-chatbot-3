package es.televoip.application.views.chat;

import es.televoip.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import es.televoip.application.model.UserEntity;
import es.televoip.application.service.ChatService;
import es.televoip.application.service.UserEntityService;
import es.televoip.application.views.join.JoinView;
import jakarta.annotation.PostConstruct;
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

   @Autowired
   private ServiceListener serviceListener;

   protected Registration broadcasterRegistration; // Recibir transmisiones Broadcaster
   private final ChatService chatService;  // Agrega el servicio para la entidad ChatEntity
   private final UserEntityService userService; // Agrega el servicio para la entidad UserNickname
   private final UI ui;
   private final MessageList messageGlobalList;
   private final Map<String, MessageList> chatsMap = new HashMap<>();
   private Tabs tabs;
   private Integer contador = 0;
   private ChatInfo selectedChat;
   private ChatTab selectedTab; // Mantener el tab seleccionado actualmente
   private ChatList chatList;
   private List<ChatInfo> allChats;

   private Notification notificationShown = new Notification();
   private boolean isNotificationShown = false;
   // Todas las "instancias/sesiones" de ChatView compartirán la misma variable textChat 
   // y podrán acceder a la última versión del mensaje enviado.
   private static String textChat = ""; // "static" sino solo se actualizaría en la instancia específica donde se envió el mensaje.

   //  Se ejecuta después de que se haya creado el bean de la vista y todas sus dependencias se hayan inyectado. 
   @PostConstruct
   private void init() {
      loadUserNicknames(); // Método para cargar los nicknames de los usuarios
   }

   public ChatView(ChatService chatService, UserEntityService userService) {
      this.chatService = chatService;
      this.userService = userService;
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

      //********************** CONTROL DE ERROR POR SESIÓN VACÍA ********************************
      // Verificar si el atributo "nickname" está presente en la sesión
      if (VaadinSession.getCurrent().getAttribute("nickname") == null) {
         // Si no hay un "nickname" en la sesión, redirige a una página de inicio de sesión
         // o muestra un mensaje de error indicando que la sesión ha caducado.
         UI.getCurrent().navigate(JoinView.class);
         //UI.getCurrent().getPage().executeJs("window.location.href = '/join';");
      }
      //*****************************************************************************************

      chatList = createChatList();
      allChats = chatList.getAllChats();

      for (ChatInfo chat : allChats) {
         MessageList localmessageList = new MessageList();
         localmessageList.setSizeFull();
         chatsMap.put(chat.getPhone(), localmessageList);
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
            selectedChat.resetUnread(); // ponmeos a 0 los mensajes pendientes de leer
            notificationShown.close();  // cerramos el Dialog de información
            Application.selectedChat = selectedChat; // guardamos el Chat seleccionado

            loadChats(); // cargamos los Chats de la BD

            tabs.getChildren().forEach(component -> { // Itera sobre los componentes hijos para acceder a cada objeto Tab
               // La palabra 'instanceof' es un operador que verifica si el objeto en cuestión es de un cierto tipo,
               // en este caso, si el componente es un ChatTab.
               if (component instanceof ChatTab) { // verifica si el componente actual es una instancia de ChatTab
                  ChatTab tab = (ChatTab) component;
                  tab.setVisibleEditButton(false); // hacemos todos invisibles para luego..
               }
            });
            selectedTab.setVisibleEditButton(true); // ..activar el botón del tab seleccionado
         }
      });
   }

   private ChatList createChatList() {
      if (chatList == null) {
         chatList = new ChatList();
         Integer unreadMessage = 0;
         chatList.addChat(new ChatInfo("34111", "uno", unreadMessage));
         chatList.addChat(new ChatInfo("34222", "dos", unreadMessage));
         chatList.addChat(new ChatInfo("34333", "tres", unreadMessage));
         chatList.addChat(new ChatInfo("34444", "cuatro", unreadMessage));
         chatList.addChat(new ChatInfo("34555", "cinco", unreadMessage));
      }

      return chatList;
   }

   private ChatTab createTab(ChatInfo chat) {

      ChatTab tab = new ChatTab(chat);
      tab.addClassNames(LumoUtility.JustifyContent.BETWEEN);

      Span badge = new Span();
      chat.setUnreadBadge(badge);
      badge.getElement().getThemeList().add("badge small contrast");
      //tab.add(new Span("#" + chat.getName() + "(" + chat.getPhone() + ")"), badge);
      tab.add(badge); // se realiza desde la clase ChatTab

      return tab;
   }

   private void sortTabsByUnread() {
      List<ChatTab> tabList = new ArrayList<>();
      tabs.getChildren().forEach(component -> { // Itera sobre los componentes hijos para acceder a cada objeto
         if (component instanceof ChatTab) { // verifica si el objeto en cuestión es de un cierto tipo
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

         String userChat = updateNickSender(); // Actualizamos el nombre del Nick antes de grabar
         ChatEntity chatSender = ChatEntity.builder()
                .phone(phoneUser)
                .text(textChat)
                .timestamp(newMessage.getTime())
                .sender(nameUser)
                .nickSender(userChat)
                .build();
         chatService.saveChat(chatSender);  // ***** GRABAMOS LOS MENSAJES DEl EMISOR/REMITENTE *****

         MessageList messageList = chatsMap.get(selectedChat.getPhone());
         List<MessageListItem> items = new ArrayList<>(messageList.getItems());
         items.add(newMessage);
         messageList.setItems(items);
         messageGlobalList.setItems(items);

         contador++; // añadimos un incremento a un identificador numérico de la respuesta
         String answer = textChat + " | ..respuesta" + contador;
         String nickname = VaadinSession.getCurrent().getAttribute("nickname").toString();
         MessageListItem botMessage = new MessageListItem(answer, Instant.now(), nickname);
         botMessage.setUserAbbreviation("Bot");
         botMessage.setUserColorIndex(2);
         botMessage.addThemeNames("chat-view-bubble-bot");

         userChat = updateNickSender(); // Actualizamos el nombre del Nick antes de grabar
         ChatEntity chatReceiver = ChatEntity.builder()
                .text(botMessage.getText())
                .timestamp(botMessage.getTime())
                .sender(nameUser)
                .receiver(botMessage.getUserName())
                .nickSender(userChat)
                .build();
         chatService.saveChat(chatReceiver); // ***** GRABAMOS MENSAJES DE LA RESPUESTA *****

         Application.selectedChat = selectedChat; // cuando creo un mensaje se guarda el chat de quien lo hizo
         items.add(botMessage);
         messageList.setItems(items);
         messageGlobalList.setItems(items);

         // ***** ENVIAMOS EL BROADCASTER *****
         Broadcaster.broadcast(items);
         //************************************
      });

      return input;
   }

   private String updateNickSender() {
      System.out.println("El sender es " + selectedTab.getNickUser());
      return selectedTab.getNickUser();
   }

   public void loadNickInAllTabs(String newNick, String userPhone) {
      tabs.getChildren().forEach(component -> {
         if (component instanceof ChatTab) {
            ChatTab tab = (ChatTab) component;
            //String currentNick = tab.getNickUser();
            if (userPhone.equals(tab.getChatInfo().getPhone())) {
               tab.setNickUser(newNick);
               tab.updateTabContent();
            }
         }
      });
   }

   private void loadChats() {
      // Procedemos a cargar de la BD los mensajes por Sender "Emisor"
      List<ChatEntity> chatEntities = chatService.searchBySender(selectedChat.getName());
      //List<ChatEntity> chatEntities = chatService.searchByPhone(selectedChat.getPhone());
      List<MessageListItem> newMessages = new ArrayList<>();

      for (ChatEntity objectChatEntity : chatEntities) {
         String objectPhone = objectChatEntity.getPhone();
         String objectSender = objectChatEntity.getSender();
         Instant objectTime = objectChatEntity.getTimestamp();
         String objectText = objectChatEntity.getText();
         String objectReceiver = objectChatEntity.getReceiver();
         String nickUser = objectChatEntity.getNickSender();
         String objectUser = selectedChat.getName();

         // Recorrer todos los Tab para ponerlo justo en el que es
//         if (objectPhone != null && nickUser != null) {
//            loadNickInAllTabs(nickUser, objectPhone);
//         }
         

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

      sendLoadChatToScreen(newMessages);
   }

   private void sendLoadChatToScreen(List<MessageListItem> newMessages) {
      MessageList messageList = chatsMap.get(selectedChat.getPhone());
      List<MessageListItem> items = new ArrayList<>();
      items.addAll(newMessages);

      messageList.setItems(items);
      messageGlobalList.setItems(items);

      System.out.println("Mensajes del Tab cargados: " + selectedChat.getName());
   }

   private void setMobile(boolean mobile) {
      tabs.setOrientation(mobile ? Tabs.Orientation.HORIZONTAL : Tabs.Orientation.VERTICAL);
   }

   private void setMobileIconSize(Icon icon, boolean isMobile) {
      if (isMobile) {
         icon.addClassNames(LumoUtility.IconSize.LARGE);
      } else {
         icon.addClassNames(LumoUtility.IconSize.MEDIUM);
      }
   }

   @Override
   protected void onAttach(AttachEvent attachEvent) {
      // Hacemos visible el Drawer cuando se accede a la View
      MainLayout.get().setDrawerOpened(true);

      Page page = attachEvent.getUI().getPage();
      page.addBrowserWindowResizeListener(e -> setMobile(e.getWidth() < 945));
      page.retrieveExtendedClientDetails(details -> {
         setMobile(details.getWindowInnerWidth() < 940);
      });

      if (!isNotificationShown && tabs.getSelectedTab() == null) {
         // Mostrar la notificación solo si no se ha mostrado antes y no hay un Tab seleccionado
         notificationShown.addThemeVariants(NotificationVariant.LUMO_WARNING);

         // Crear los componentes de texto y estilo
         Icon icon = VaadinIcon.ARROW_CIRCLE_UP.create();
         // Verificar si es resolución móvil
         page.retrieveExtendedClientDetails(details -> setMobileIconSize(icon, details.getWindowInnerWidth() < 940));
         Span text1 = new Span("Selecciona algún ");
         Span boldSpan = new Span("CHAT");
         boldSpan.getStyle().set("font-weight", "bold");
         Span text2 = new Span(" de la lista de usuarios");

         // Crear el Div y agregar los componentes de texto
         Div text = new Div(text1, boldSpan, text2);
         text.addClassNames(LumoUtility.FontSize.LARGE);

         HorizontalLayout layout = new HorizontalLayout(icon, text);
         layout.setAlignItems(Alignment.CENTER);

         notificationShown.add(layout);
         notificationShown.open();
         notificationShown.setPosition(Notification.Position.MIDDLE);
         isNotificationShown = true; // marcar la notificación como mostrada
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
                  sortTabsByUnread(); // Ordenamos los tabs por cantidad de mensajes no leídos
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

      // Nuevo Broadcaster para el cambio de nickName
      broadcasterRegistration = Broadcaster.registerNickChange(chageData -> {
         if (ui != null) {
            ui.access(() -> {
               System.out.println("LLEGA el broadcast de cambio de nick!!!! " + chageData.getUserPhone()
                      + " | " + chageData.getNewNick());
               updateNickInAllTabs2(chageData.getUserPhone(), chageData.getNewNick()); // actualizamos el nickname
            });
         }
      });
      // ****************** FIN BROADCASTER ***************************
      Set<UI> activeUIs = serviceListener.getActiveUIs();
      System.out.println("Sesiones activas UI Listener: " + activeUIs.size());
      System.out.println("Nick añadido: " + VaadinSession.getCurrent().getAttribute("nickname").toString());
   }

   public void updateNickInAllTabs2(String userPhone, String newNick) {
      tabs.getChildren().forEach(component -> {
         if (component instanceof ChatTab) {
            ChatTab tab = (ChatTab) component;
            //String currentNick = tab.getNickUser();
            if (userPhone.equals(tab.getChatInfo().getPhone())) {
               tab.setNickUser(newNick);
               tab.updateTabContent();
            }
         }
      });
      saveUserNickname(userPhone, newNick); // Guardar el nickname en la base de datos
   }

   // Método para cargar los nicknames de los usuarios desde la base de datos y actualizar los tabs
   private void loadUserNicknames() {
      List<UserEntity> userList = userService.searchAll();
      for (UserEntity userObject : userList) {
         String phone = userObject.getPhone();
         String nickname = userObject.getNickname();
         updateNickInAllTabs2(phone, nickname);
      }
   }

   // Método para guardar el nickname del usuario en la base de datos
   private void saveUserNickname(String phone, String nickname) {
      System.out.println("--llega al sistema de grabar el NICK!!!!!!!!!!!!!" + phone + " | " + nickname);
      UserEntity userObject = new UserEntity();
      userObject.setPhone(phone);
      userObject.setNickname(nickname);
      userService.updateUserNick(userObject);
   }

   @Override
   protected void onDetach(DetachEvent detachEvent) {
      broadcasterRegistration.remove();
      broadcasterRegistration = null;
      notificationShown.close();
   }

}
