package es.televoip.application.views.chat;

import es.televoip.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
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
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import es.televoip.application.Application;
import es.televoip.application.broadcast.Broadcaster;
import es.televoip.application.chat.ChatInfo;
import es.televoip.application.chat.ChatList;
import es.televoip.application.chat.ChatTab;
import es.televoip.application.listeners.ServiceListener;
import es.televoip.application.model.ChatEntity;
import es.televoip.application.model.OperatorEntity;
import es.televoip.application.model.UserEntity;
import es.televoip.application.service.ChatService;
import es.televoip.application.service.UserEntityService;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PageTitle("Chat2")
@Route(value = "chat2", layout = MainLayout.class)
@PreserveOnRefresh // garantizas que si ocurre un evento de actualización en la página, la instancia actual se mantendrá
@UIScope // Asegura que cada instancia de ChatView esté asociada a una sesión específica
public class ChatView extends HorizontalLayout {//implements ServletContextListener {

   private Registration broadcasterRegistration; // Recibir transmisiones Broadcaster, sin static para evitar duplicados en Notific.
   private Registration broadcasterNickRegistration; // Recibir transmisiones Broadcaster
   private final ChatService chatService;  // Agrega el servicio para la entidad ChatEntity
   private final UserEntityService userService; // Agrega el servicio para la entidad UserNickname
   private final ServiceListener serviceListener; // Agrega el servicio para la gestión de sesiones UI
   private UI ui;
   private final MessageList messageGlobalList;
   private MessageInput input;
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
   private Notification notification = new Notification(); // notifica los chat que van llegando en la parte superior

   @PostConstruct  //  Se ejecuta después de que se haya creado el bean de la vista y todas sus dependencias se hayan inyectado. 
   private void init() {
      loadUserNicknames(); // Método para cargar los nicknames de los usuarios
      this.initializeRedirect();
   }

   public ChatView(ChatService chatService, UserEntityService userService, ServiceListener serviceListener) {
      this.chatService = chatService;
      this.userService = userService;
      this.serviceListener = serviceListener;

      this.ui = UI.getCurrent();

      //ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
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
            input.setVisible(isNotificationShown); // hacemos visible caja de inputText y button Send

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

      //input = createMessageInput();
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
      input = new MessageInput();
      input.setWidthFull();
      input.setVisible(isNotificationShown);

      input.addSubmitListener(userMessage -> {
         String nickSession = VaadinSession.getCurrent().getAttribute("nickname").toString();
         String phoneUser = selectedChat.getPhone();
         String nameUser = selectedChat.getName();
         textChat = userMessage.getValue();

         // Crear el mensaje del remitente
         MessageListItem senderMessage = createOperatorMessageListItem(textChat, nickSession, nameUser);
         saveChatMessage(phoneUser, nickSession, nameUser, textChat); // Guardar el mensaje del remitente

         // Crear el mensaje de la respuesta (bot)
         contador++;
         String nickUser = userService.getNickUserFromPhone(phoneUser);
         String answer = textChat + " | ..respuesta" + contador;
         MessageListItem botMessage = createUserMessageListItem(answer, nickSession, nickUser, nameUser);
         saveChatMessage(phoneUser, nameUser, null, answer); // Guardar el mensaje de la respuesta (bot)

         // Actualizar las listas de mensajes
         MessageList messageList = chatsMap.get(selectedChat.getPhone());
         List<MessageListItem> items = new ArrayList<>(messageList.getItems());
         items.add(senderMessage);
         items.add(botMessage);
         messageList.setItems(items);
         messageGlobalList.setItems(items);

         Application.selectedChat = selectedChat; // cuando creo un mensaje se guarda el chat de quien lo hizo

         // ***** ENVIAMOS EL BROADCASTER DE MENSAJES *****
         Broadcaster.broadcast(items);
         //************************************************
      });

      return input;

   }

   private MessageListItem createOperatorMessageListItem(String text, String sender, String receiver) {
      MessageListItem messageListItem = new MessageListItem(text, Instant.now(), receiver);
      messageListItem.setUserAbbreviation(sender);
      messageListItem.setUserImage(loadAvatarNickSession(sender));
      messageListItem.setUserName(sender);
      messageListItem.addThemeNames("chat-view-bubble-bot");
      return messageListItem;
   }

   private MessageListItem createUserMessageListItem(String text, String sender, String nickUser, String receiver) {
      MessageListItem botMessageListItem = new MessageListItem(text, Instant.now(), sender);
      botMessageListItem.setUserColorIndex(2);
      botMessageListItem.setUserAbbreviation(receiver);
      botMessageListItem.setUserName(nickUser + " - " + receiver + " - " + selectedChat.getPhone());
      botMessageListItem.addThemeNames("chat-view-bubble");
      return botMessageListItem;
   }

   private void saveChatMessage(String phone, String sender, String receiver, String text) {
      ChatEntity chatEntity = ChatEntity.builder()
             .phone(phone)
             .text(text)
             .timestamp(Instant.now())
             .sender(sender)
             .receiver(receiver)
             .build();
      chatService.saveChat(chatEntity);
   }

//   private String updateNickSender() {
//      System.out.println("El sender es " + selectedTab.getNickUser());
//      return selectedTab.getNickUser();
//   }
   // En su momento cargar X mensajes realizando una carga Lazy y paginación por scroll
   private void loadChats() {
      if (selectedChat != null) {
         // Procedemos a cargar de la BD los mensajes por el Teléfono del usuario
         List<ChatEntity> chatEntities = chatService.searchByPhone(selectedChat.getPhone());
         List<MessageListItem> newMessages = new ArrayList<>();

         for (ChatEntity objectChatEntity : chatEntities) {
            String objectPhone = objectChatEntity.getPhone();
            String objectSender = objectChatEntity.getSender();
            Instant objectTime = objectChatEntity.getTimestamp();
            String objectText = objectChatEntity.getText();
            String objectReceiver = objectChatEntity.getReceiver();
            String nickUser = userService.getNickUserFromPhone(objectPhone);
            String objectUser = selectedChat.getName();

            MessageListItem newMessage = new MessageListItem();
            newMessage.setText(objectText);
            newMessage.setTime(objectTime);

            if (objectReceiver == null) {
               newMessage.addThemeNames("chat-view-bubble");
               newMessage.setUserColorIndex(2);
               newMessage.setUserAbbreviation(objectSender);
               newMessage.setUserName(nickUser + " - " + objectUser + " - " + objectPhone);
            } else {
               newMessage.addThemeNames("chat-view-bubble-bot");
               newMessage.setUserColorIndex(2);
               newMessage.setUserAbbreviation(objectSender);
               newMessage.setUserImage(loadAvatarNickSession(objectSender));
               newMessage.setUserName(objectSender);
            }

            newMessages.add(newMessage);
         }
         sendLoadChatToScreen(newMessages);
      }
   }

   private void sendLoadChatToScreen(List<MessageListItem> newMessages) {
      MessageList messageList = chatsMap.get(selectedChat.getPhone());
      List<MessageListItem> items = new ArrayList<>();
      items.addAll(newMessages);
      messageList.setItems(items);
      messageGlobalList.setItems(items);

      System.out.println("Mensajes del Tab cargados: " + selectedChat.getName());
   }

   public void updateNickInAllTabs(String userPhone, String newNick) {
      Span badge = new Span();
      badge.getElement().getThemeList().add("badge small contrast");
      tabs.getChildren().forEach(component -> {
         if (component instanceof ChatTab) {
            ChatTab tab = (ChatTab) component;

            if (userPhone.equals(tab.getChatInfo().getPhone()) && tab.getChatInfo().getPhone() != null) {
               tab.setNickUser(newNick); // seteamos el nuevo nickname
               tab.getChatInfo().setUnreadBadge(badge); // añadimos el nº de chat no leidos
               tab.updateTabContent();
               tab.add(badge);
            }
         }
      });
      // Guardar el nickname en la base de datos
      userService.updateUserNick(userPhone, newNick);
   }

   // Método para cargar los nicknames de los usuarios desde la base de datos y actualizar los tabs
   private void loadUserNicknames() {
      List<UserEntity> userList = userService.searchAll();
      for (UserEntity userObject : userList) {
         String phone = userObject.getPhone();
         String nickname = userObject.getNickname();
         updateNickInAllTabs(phone, nickname);
      }
   }

   private String loadAvatarNickSession(String nickSession) {
      OperatorEntity operator = new OperatorEntity();
      if (nickSession.equalsIgnoreCase("Antonio")) {
         operator.setStringResource("https://canaritel.es/descargas/antonio-avatar2.jpg");
      } else if (nickSession.equalsIgnoreCase("Pepe")) {
         operator.setStringResource("https://canaritel.es/descargas/avatar_24.png");
      } else {
         operator.setStringResource("https://canaritel.es/descargas/avatar_21.png");
      }

      return operator.getStringResource();
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

   private void initializeRedirect() {
      UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
         WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
         boolean isMobile = webBrowser != null && (webBrowser.isAndroid() || webBrowser.isIPhone()
                || webBrowser.isWindowsPhone() || webBrowser.isWindows() || details.isIPad());

         System.out.println("********** DETALLES DEL NAVEGADOR ***********");
         System.out.println("WebBrowser " + webBrowser.getBrowserApplication());
         System.out.println("WebBrowser Android: " + webBrowser.isAndroid());
         System.out.println("WebBrowser Iphone: " + webBrowser.isIPhone());
         System.out.println("WebBrowser Phone: " + webBrowser.isIPhone());
         System.out.println("WebBrowser Windows: " + webBrowser.isWindows());
         System.out.println("WebBrowser Mac: " + webBrowser.isMacOSX());
         System.out.println("WebBrowser Linux: " + webBrowser.isLinux());
         System.out.println("*********************************************");
      });
   }

   // El método 'onAttach' se ejecutará cada vez que se muestre la vista en la interfaz de usuario,
   // por lo que es un buen lugar para inicializar componentes, cargar datos o realizar otras 
   // acciones que deban ocurrir cuando la vista esté presente.
   // Más información: https://vaadin.com/docs/latest/create-ui/creating-components/lifecycle-callbacks
   @Override // El método 'onAttach' se invoca cuando el Component se ha adjuntado al UI
   protected void onAttach(AttachEvent attachEvent) {
      MainLayout.get().setDrawerOpened(true);  // Hacemos visible el Drawer cuando se accede a la View

      Page page = attachEvent.getUI().getPage();
      page.addBrowserWindowResizeListener(e -> setMobile(e.getWidth() < 945));
      page.retrieveExtendedClientDetails(details -> {
         setMobile(details.getWindowInnerWidth() < 940);
      });

      //if (ui.isAttached()) {
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

      // **************** RECIBIMOS BROADCASTER DE MENSAJES ******************
      broadcasterRegistration = Broadcaster.register(newMessage -> {
         try {
            if (ui != null) {
               ui.access(() -> {
                  ChatInfo currentSelectedChat = selectedChat; // Obtenemos el chat seleccionado en la sesión activa
                  ChatInfo applicationSelectedChat = Application.selectedChat; // Obtenemos el chat seleccionado del que envía
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
                  String firstTenCharacters = textChat.substring(0, Math.min(textChat.length(), 20));
                  String text = applicationSelectedChat.getName() + ": " + firstTenCharacters + "...";

                  if (ui.isAttached()) {
                     notification = Notification.show(text, 3000, Notification.Position.TOP_CENTER);
                     notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                  }

//                     System.out.println("UI: " + ui.getCsrfToken());
               });
               System.out.println("UI: " + ui.getCsrfToken());
            }
         } catch (UIDetachedException e) {
            // Solucionamos el error que sucede al refrescar de forma manual el navegador con la sesión iniciada
            System.err.println("Error en la UI al duplicarse" + e.getMessage());
            //ui.removeFromParent();
         }
      });

      // ************** RECIBIMOS BROADCASTER PARA NICKNAME *******************
      broadcasterNickRegistration = Broadcaster.registerNickChange(changeData -> {
         try {
            if (ui != null) {
               ui.access(() -> {
                  updateNickInAllTabs(changeData.getUserPhone(), changeData.getNewNick()); // actualizamos el nickname
                  loadChats();
               });
            }
         } catch (UIDetachedException e) {
            // Solucionamos el error que sucede al refrescar de forma manual el navegador con la sesión iniciada
            System.err.println("Error en la UI nick al duplicarse" + e.getMessage());
            // ui.removeFromParent();
         }
      });
      // ********************** FIN BROADCASTER *****************************

      Set<UI> activeUIs = serviceListener.getActiveUIs();
      System.out.println("Sesiones activas UI Listener: " + activeUIs.size());
      System.out.println("Sesión Nick añadido: " + VaadinSession.getCurrent().getAttribute("nickname").toString());
      // }
   }

   //  El método 'onDetach' se invoca justo antes de que el componente se separe del UI, ideal para liberar recursos
   @Override
   protected void onDetach(DetachEvent detachEvent) {
      if (broadcasterRegistration != null) {
         broadcasterRegistration.remove();
         broadcasterRegistration = null;
      }

      if (broadcasterNickRegistration != null) {
         broadcasterNickRegistration.remove();
         broadcasterNickRegistration = null;
      }

      if (notificationShown != null) {
         notificationShown.close();
      }

      if (notification != null) { //&& ui.isAttached()) {
         notification.close();
      }
   }

}
