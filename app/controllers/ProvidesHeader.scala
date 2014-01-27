package controllers

import models.Swimmer
import jp.t2v.lab.play2.auth.AuthElement
import jp.t2v.lab.play2.stackc.RequestWithAttributes
import auth.AuthenticationConfig
import com.typesafe.scalalogging.slf4j.Logging

case class HeaderData(followedSwimmers: List[Swimmer])

trait ProvidesHeader extends Logging {
  controller: AuthElement with AuthenticationConfig =>

  implicit def header[A](implicit request: RequestWithAttributes[A]) = {
    logger.error("!!!!!!!!!!!!!!!!!!!!!! IMPLICIT!!!!!!!!!!!!!!!")
    val watched = controller.loggedIn.watching.map { id =>
      Swimmer.findById(id) // map { s => s.name.firstName + " " + s.name.lastName }
    }.flatten
    HeaderData(watched)
  }
}