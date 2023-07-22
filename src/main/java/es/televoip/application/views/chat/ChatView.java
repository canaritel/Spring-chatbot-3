package es.televoip.application.views.chat;

import es.televoip.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Aside;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
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
import es.televoip.application.chat.Broadcaster;
import es.televoip.application.chat.ChatInfo;
import es.televoip.application.chat.ChatList;
import es.televoip.application.chat.ChatTab;
import es.televoip.application.model.ChatEntity;
import es.televoip.application.service.ChatService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Chat2")
@Route(value = "chat2", layout = MainLayout.class)
public class ChatView extends HorizontalLayout {

   private final ChatService chatService;
   private UI ui;
   private final Tabs tabs;
   private final MessageList messageGlobalList;
   private final Map<String, MessageList> chatsMap = new HashMap<>();
   private Integer contador = 0;
   private ChatInfo selectedChat;

   Registration broadcasterRegistration; // Recibir transmisiones Broadcaster

   Notification messages;

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
      tabs.setOrientation(Tabs.Orientation.VERTICAL);
      tabs.addClassNames(LumoUtility.Flex.GROW,
             LumoUtility.Flex.SHRINK,
             LumoUtility.Overflow.HIDDEN);

      ChatList chatList = createChatList();
      ChatInfo currentChat = chatList.getChat();
      List<ChatInfo> allChats = chatList.getAllChats();

      for (ChatInfo chat : allChats) {
         MessageList localmessageList = new MessageList();
         localmessageList.setSizeFull();
         chatsMap.put(chat.getName(), localmessageList);
         if (currentChat != chat) {
            chat.incrementUnread();
         }
         ChatTab tab = createTab(chat);
         tabs.add(tab);
      }

      Aside side = createAside();
      VerticalLayout chatContainer = createChatContainer();

      add(chatContainer, side);
      setSizeFull();
      expand(chatContainer);

      tabs.addSelectedChangeListener(event -> {
         ChatTab selectedTab = (ChatTab) event.getSelectedTab();
         selectedChat = selectedTab.getChatInfo();
         selectedChat.resetUnread();

         loadChats();
      });

   }

   private ChatList createChatList() {
      ChatList chatList = new ChatList();
      chatList.addChat(new ChatInfo("0", "inicio-no-coge", 0));
      chatList.addChat(new ChatInfo("34111", "general", 0));
      chatList.addChat(new ChatInfo("34222", "support", 0));
      chatList.addChat(new ChatInfo("34333", "casual", 0));
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

      ChatTab currentTab = (ChatTab) tabs.getSelectedTab();
      selectedChat = currentTab.getChatInfo();
      selectedChat.resetUnread();

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
         String text = userMessage.getValue();

         MessageListItem newMessage = new MessageListItem(text, Instant.now(), nameUser);
         newMessage.setUserColorIndex(3);
         newMessage.addThemeNames("chat-view-bubble");

         ChatEntity chatEntity = ChatEntity.builder()
                .phone(phoneUser)
                .text(text)
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
         String answer = text + " | ..respuesta" + contador;
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

         ui.access(() -> {
            Notification notification;
            chatService.saveChat(chatReceiver);
            items.add(botMessage);
            messageList.setItems(items);
            messageGlobalList.setItems(items);
            //notification = Notification.show(botMessage.getText(), 3000, Notification.Position.TOP_CENTER);
            //notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            Broadcaster.broadcast(items); /////////////////////
         });

      });

      return input;
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
      //List<MessageListItem> items = new ArrayList<>(messageList.getItems()); // Lo dividimos en 2 funciones
      List<MessageListItem> items = new ArrayList<>();
      //items.addAll(messageList.getItems());  // Anulamos para evitar cargar los chats en memoria
      items.addAll(newMessages);

      messageList.setItems(items);
      messageGlobalList.setItems(items);
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

      // cargamos los chats de la BD
      loadChats();

      UI ui = attachEvent.getUI();
      broadcasterRegistration = Broadcaster.register(newMessage -> {
         ui.access(() -> {
            Notification.show("Recibiendo Broadcaster..", 3000, Notification.Position.TOP_CENTER);
            messageGlobalList.setItems(newMessage);
         });
         System.out.println("Recibiendo Broadcaster..");

      });
   }

   @Override
   protected void onDetach(DetachEvent detachEvent) {
      broadcasterRegistration.remove();
      broadcasterRegistration = null;
   }

}
