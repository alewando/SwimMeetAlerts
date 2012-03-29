package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdPk, StringPk}
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.BsonDSL._

/**
 * Mongo collection to track completed events.
 * Key (id field) is "Meet Name - EventUrl"
 */
class CompletedEvent private() extends MongoRecord[CompletedEvent] with ObjectIdPk[CompletedEvent] {
  def meta = CompletedEvent
  
  object meetName extends StringField[CompletedEvent](this,100)
  object eventUrl extends StringField[CompletedEvent](this,200)

}

object CompletedEvent extends CompletedEvent with MongoMetaRecord[CompletedEvent] {

  def eventCompleted_?(meetName: String, eventUrl: String): Boolean = {
    CompletedEvent.find(("meetName" -> meetName) ~ ("eventUrl" -> eventUrl)).isDefined
  }

  def markEventComplete(meetName: String, eventUrl: String) {
    CompletedEvent.createRecord.meetName(meetName).eventUrl(eventUrl).save
  }

  /**
   * Remove all CompletedEvent records for the specified meet
   */
  def deleteEventsForMeet(meetName:String) {
    CompletedEvent.findAll(("meetName" -> meetName)) map(_.delete_!)
  }
}
