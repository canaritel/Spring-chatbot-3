package es.televoip.application.broadcast;

import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.shared.Registration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Broadcaster {

   static Executor executor = Executors.newSingleThreadExecutor();

   static LinkedList<Consumer<List<MessageListItem>>> listeners = new LinkedList<>();
   static LinkedList<Consumer<NickChangeData>> listenersNickChange = new LinkedList<>();

   public static synchronized Registration register(Consumer<List<MessageListItem>> listener) {
      listeners.add(listener);

      return () -> {
         synchronized (Broadcaster.class) {
            listeners.remove(listener);
         }
      };
   }

   public static synchronized void broadcast(List<MessageListItem> message) {
      for (Consumer<List<MessageListItem>> listener : listeners) {
         executor.execute(() -> listener.accept(message));
      }
   }

   // Nuevo método para enviar el cambio de nick
   public static synchronized Registration registerNickChange(Consumer<NickChangeData> listener) {
      listenersNickChange.add(listener);

      return () -> {
         synchronized (Broadcaster.class) {
            listenersNickChange.remove(listener);
         }
      };
   }

   // Nuevo método para enviar el cambio de nick con la clase NickChangeData
   public static synchronized void broadcastNickChange(String userPhone, String newNick) {
      NickChangeData nickChangeData = new NickChangeData(userPhone, newNick);
      for (Consumer<NickChangeData> listener : listenersNickChange) {
         executor.execute(() -> listener.accept(nickChangeData));
      }
   }

}
