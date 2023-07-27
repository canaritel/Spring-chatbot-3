package es.televoip.application.listeners;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceListener implements VaadinServiceInitListener {

   private Set<UI> activeUIs = new HashSet<>();

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
             });
   }

   // Método para obtener las UI activas
   public Set<UI> getActiveUIs() {
      return activeUIs;
   }

}
