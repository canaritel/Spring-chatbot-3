package es.televoip.application.session;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import es.televoip.application.views.join.JoinView;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/*
@WebFilter("/*")
public class SessionFilter implements Filter {

   @Override
   public void init(FilterConfig filterConfig) throws ServletException {
      // Puedes realizar alguna inicialización si es necesario.
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

// Verificar si hay una instancia activa de UI antes de continuar.
      UI ui = UI.getCurrent();
      if (ui == null) {
         // Si no hay una instancia activa de UI, detén el procesamiento de la solicitud o realiza alguna otra acción.
         return;
      }

      // Verificar si la sesión es válida y contiene el "nickname" antes de continuar.
      VaadinSession session = VaadinSession.getCurrent();
      if (session == null || session.getAttribute("nickname") == null) {
         // Si no hay una sesión activa o no hay un "nickname" en la sesión, redirige a la vista JoinView.
         ui.navigate(JoinView.class);
         return; // Detiene el procesamiento de la solicitud.
      }

      // Si la sesión es válida y contiene el "nickname", continúa con la solicitud.
      chain.doFilter(request, response);
   }

   @Override
   public void destroy() {
      // Puedes realizar alguna limpieza si es necesario.
   }

   /*
   private boolean isPublicUrl(String url) {
      // Lista de URLs públicas permitidas (sin autenticación requerida)
      List<String> publicUrls = Arrays.asList(
             "/join", // Página de inicio de sesión
             "/" // Página de registro
      //    "/public-page"       // Página de información pública, reemplaza con tus URLs reales
      );

      // Verifica si la URL se encuentra en la lista de URLs públicas
      return publicUrls.contains(url);
   }
    */
   // Implementa los métodos de la interfaz Filter (init y destroy) si es necesario.
//}
