package es.televoip.application.chat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class EditButton extends Button {

   public EditButton() {
      super(new Icon("lumo", "edit")); // otro icono de Lumo
      addClassNames(LumoUtility.IconSize.MEDIUM, LumoUtility.Background.TRANSPARENT); // Clase CSS para dar estilo al botón si lo deseas
      setIconAfterText(false); // Coloca el icono después del texto en el botón
   }
}
