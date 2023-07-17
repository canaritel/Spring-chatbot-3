package es.televoip.application.views;

import com.vaadin.flow.component.Component;
import es.televoip.application.views.join.JoinView;
import com.vaadin.flow.component.UI;
import es.televoip.application.views.about.AboutView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import es.televoip.application.views.chat.ChatView;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

   private H2 viewTitle;
   private DrawerToggle toggle;
   private SideNav nav;
   private SideNavItem joinNavItem;

   public MainLayout() {
      setPrimarySection(Section.DRAWER);
      addDrawerContent();
      addHeaderContent();
      setDrawerOpened(false); // ocultar drawer al inicio
   }

   private void addHeaderContent() {
      toggle = new DrawerToggle();
      toggle.setAriaLabel("Menu toggle");

      viewTitle = new H2();
      viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

      addToNavbar(true, toggle, viewTitle);
   }

   private void addDrawerContent() {
      H1 appName = new H1("My App");
      appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
      Header header = new Header(appName);

      Scroller scroller = new Scroller(createNavigation());

      addToDrawer(header, scroller, createFooter());
   }

   private SideNav createNavigation() {
      nav = new SideNav();

      joinNavItem = new SideNavItem("Join", JoinView.class, LineAwesomeIcon.JAVA.create());
      SideNavItem chatNavItem = new SideNavItem("Chat2", ChatView.class, LineAwesomeIcon.BOOTSTRAP.create());
      SideNavItem aboutNavItem = new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create());

      nav.addItem(joinNavItem, chatNavItem, aboutNavItem);

      return nav;
   }

   private Footer createFooter() {
      Footer layout = new Footer();

      return layout;
   }

   @Override
   protected void afterNavigation() {
      super.afterNavigation();
      viewTitle.setText(getCurrentPageTitle());

      // Si el atributo "nickname" es null lo enviamos a la vista JoinView. 
      if (VaadinSession.getCurrent().getAttribute("nickname") == null) {
         System.out.println("Es nulo");
         UI.getCurrent().navigate(JoinView.class);
         toggle.setVisible(false); // Comenzamos con la barra superior invisible
      } else {
         System.out.println("No es nulo");
         toggle.setVisible(true); //
         nav.remove(joinNavItem);
      }

      System.out.println(getCurrentClassName());

   }

   private String getCurrentClassName() {
      Component currentContent = getContent();
      if (currentContent != null) {
         return currentContent.getClass().getSimpleName();
      }
      return "";
   }

   private String getCurrentPageTitle() {
      PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
      //return title == null ? "" : title.value();
      if (title == null) {
         return "";
      } else {
         return title.value();
      }
   }

   // Esta método si debe estar en el MainLayout, permite acceder a componentes llamados fuera del método MainLayout
   public static MainLayout get() {
      return (MainLayout) UI.getCurrent().getChildren()
             .filter(component -> component.getClass() == MainLayout.class)
             .findFirst().get();
   }

}
