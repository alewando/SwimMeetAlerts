package model

import net.liftweb.mongodb.record.field.StringPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}

/**
 * Mongo collection to track completed events.
 * Key (id field) is "Meet Name - EventUrl"
 */
class CompletedEvent private() extends MongoRecord[CompletedEvent] with StringPk[CompletedEvent] {
  def meta = CompletedEvent

}

object CompletedEvent extends CompletedEvent with MongoMetaRecord[CompletedEvent] {

  def eventCompleted_?(meetName: String, eventUrl: String): Boolean = {
    CompletedEvent.find(key(meetName, eventUrl)).isDefined
  }

  def markEventComplete(meetName: String, eventUrl: String) {
    CompletedEvent.createRecord.id(key(meetName, eventUrl)).save
  }

  private def key(meetName: String, eventUrl: String) = meetName + "-" + eventUrl
}
