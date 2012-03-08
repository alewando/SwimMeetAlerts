/**
 * App configuration
 */
package config
object Config {
  val DEFAULT_DB_URL = "mongodb://kobe:27017/meetResults"
  val BASE_URL = Option(System.getenv().get("BASE_URL")) getOrElse "http://results.teamunify.com"
  val DEFAULT_MEET_ID = Option(System.getenv().get("MEET_ID")) getOrElse "meet1.1"

  val SMTP_USER = System.getenv("SENDGRID_USERNAME")
  val SMTP_PASSWORD = System.getenv("SENDGRID_PASSWORD")
  val SMTP_SERVER = if (SMTP_USER == null) "192.168.0.1" else "smtp.sendgrid.net"
  val EMAIL_FROM_ADDRESS = "alert@swimmeetalerts.com";

}
