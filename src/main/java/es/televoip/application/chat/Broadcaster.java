package es.televoip.application.chat;

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

   public static synchronized Registration register(
          Consumer<List<MessageListItem>> listener) {
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

}
