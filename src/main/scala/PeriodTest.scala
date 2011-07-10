import org.joda.time.format.{PeriodParser, PeriodFormatterBuilder}
import org.joda.time.{MutablePeriod, Period}


object PeriodTest {
  def main(args: Array[String]) {
    val parser = new PeriodFormatterBuilder().printZeroRarelyLast().appendMinutes().appendSeparatorIfFieldsAfter(":").appendSeconds.toParser();
    val seed = new MutablePeriod()
    parser.parseInto(seed, "45",0,null)
    println("seed: "+seed)
  }
}