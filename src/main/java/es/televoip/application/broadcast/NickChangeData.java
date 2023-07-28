package es.televoip.application.broadcast;

public class NickChangeData {

   private String userPhone;
   private String newNick;

   public NickChangeData(String userPhone, String newNick) {
      this.userPhone = userPhone;
      this.newNick = newNick;
   }

   public String getUserPhone() {
      return userPhone;
   }

   public String getNewNick() {
      return newNick;
   }

}
