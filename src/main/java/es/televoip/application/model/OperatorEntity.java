package es.televoip.application.model;

import com.vaadin.flow.server.StreamResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // es equivalente a usar @ToString, @EqualsAndHashCode, @Getter, @Setter, @RequiredArgsConstrutor al mismo tiempo
@NoArgsConstructor  // genera un constructor sin parámetros
@AllArgsConstructor  // genera un constructor con un parámetro para cada campo en su clase
@Builder  // se utiliza en clases, constructores y métodos para proporcionarle API de compilador complejas
//@Entity
public class OperatorEntity {

//   @Id
//   @GeneratedValue(strategy = GenerationType.IDENTITY)
//   private Long id;
   //@Column(unique = true, nullable = false)
   private String nickSession;

   //@Column(nullable = true)
   private String stringResource;

   private StreamResource image;

   // Getters y setters (generados automáticamente por @Data)
   public void setImage(String streamResource) {
      this.image = new StreamResource("myimage.png",
             () -> getClass().getResourceAsStream(streamResource));
   }

   public StreamResource getImage() {
      return image;
   }
   
  

}
