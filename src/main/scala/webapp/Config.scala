package webapp

/**
 * App configuration
 */
object Config {
  val DEFAULT_DB_URL = "mongodb://kobe:27017/meetResults"
  val BASE_URL = Option(System.getenv("BASE_URL")) getOrElse "http://results.teamunify.com"

  val SMTP_USER = System.getenv("SENDGRID_USERNAME")
  val SMTP_PASSWORD = System.getenv("SENDGRID_PASSWORD")
  val SMTP_SERVER = Option(System.getenv("SMTP_SERVER")) getOrElse "localhost"
  val EMAIL_FROM_ADDRESS = "alert@swimmeetalerts.com";


  def DATABASE_URL = {
    val strUri = Option(System.getenv().get("MONGOLAB_URI")) getOrElse DEFAULT_DB_URL
    new com.mongodb.MongoURI(strUri)
  }

}
