package es.televoip.application.listeners;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceListener implements VaadinServiceInitListener {

   private Set<UI> activeUIs = new HashSet<>();
   private AtomicInteger intAttached;// = new AtomicInteger(0);
   private AtomicInteger intClosing; // = new AtomicInteger(0);
   private AtomicInteger intEnabled; // = new AtomicInteger(0);
   private AtomicInteger intVisible; // = new AtomicInteger(0);

   @Override
   public void serviceInit(ServiceInitEvent event) {

//      event.getSource().addSessionInitListener(
//             initEvent -> LoggerFactory.getLogger(getClass())
//                    .info("****************  A new Session has been initialized!"));
      event.getSource().addUIInitListener(
             initEvent -> {
                LoggerFactory.getLogger(getClass())
                       .info("****************  A new UI has been initialized!");
                UI ui = UI.getCurrent();
                activeUIs.add(ui);
                System.out.println("Sesiones activas UI Listener: " + activeUIs.size());
                activeUIs();
             });
   }

   // Método para obtener las UI activas
   public Set<UI> getActiveUIs() {
      return activeUIs;
   }

   public void activeUIs() {
      intAttached = new AtomicInteger(0);
      intClosing = new AtomicInteger(0);
      intEnabled = new AtomicInteger(0);
      intVisible = new AtomicInteger(0);
      activeUIs.forEach(action -> {
         if (action.isAttached()) {
            intAttached.incrementAndGet();
         }
         if (action.isClosing()) {
            intClosing.incrementAndGet();
         }
         if (action.isEnabled()) {
            intEnabled.incrementAndGet();
         }
         if (action.isVisible()) {
            intVisible.incrementAndGet();
         }
      });

      System.out.println("UI is attached: " + intAttached.get());
      System.out.println("UI is closing: " + intClosing.get());
      System.out.println("UI is enabled: " + intEnabled.get());
      System.out.println("UI is visible: " + intVisible.get());
   }

}
