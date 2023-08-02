package es.televoip.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import es.televoip.application.chat.ChatInfo;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "myapp")
@Push(PushMode.AUTOMATIC)

public class Application implements AppShellConfigurator {

   public static ChatInfo selectedChat; // guardamos el Chat-Tab de la sesión que esté chateando
   public static Set<UI> selectedUISession = new HashSet<>();

   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }

}
