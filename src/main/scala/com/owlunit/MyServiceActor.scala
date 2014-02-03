package com.owlunit

import akka.actor.Actor
import akka.event.Logging._
import spray.routing.directives.LogEntry
import spray.http._
import spray.routing._
import html._
import spray.http.HttpRequest
import scala.Some
import spray.http.HttpResponse
import com.owlunit.sse.SSEDirectives
import com.owlunit.authentication._
import scala.concurrent.ExecutionContext


class MyServiceActor extends Actor with MainRoute {

  def actorRefFactory = context
  def receive = runRoute(route)

}


// this trait defines our service behavior independently from the service actor
trait MainRoute extends HttpService with SSEDirectives with UserAuthentication {
  import ExecutionContext.Implicits.global

  import spray.httpx.TwirlSupport._
  val marshaller = twirlHtmlMarshaller

//  import SSEHandler._
//  val sseProcessor = actorRefFactory.actorOf(Props { new Actor {
//    implicit val ec = akka.dispatch.ExecutionContexts.global()
//    def receive = {
//      case (channel: ActorRef, _) =>
//        // Simulate some work
//        for(msgId <- 1 to 10) {
//          context.system.scheduler.scheduleOnce(msgId.seconds, channel, Message((msgId * 10).toString))
//        }
//
//    }
//  }})

//      pathPrefix("sse") {
//        respondWithHeader(`Cache-Control`(`no-cache`)) {
//          sse { (channel, lastEventID) =>
//          // Register a closed event handler
//            channel ! RegisterClosedHandler( () => println("Connection closed !!!") )
//
//            // Use the channel
//            sseProcessor ! (channel, lastEventID)
//          }
//        }
//      } ~

  val route =
    getFromResourceDirectory("static/") ~
    path("favicon.ico") {
      complete(StatusCodes.NotFound) // fail early in order to prevent error response logging
    } ~
    path("auth") {
      authenticate(authenticateUser) { user =>
        complete(s"Welcome, ${user.name}")
      }
    } ~
    path("doauth") {
      loginUser
    } ~
    path("test") {
      complete("ok")
    } ~
    logRequestResponse(showErrorResponses _) {
      path("") {
        complete(index())
      }
    }

  implicit val customRejectionHandlers = RejectionHandler {
    case (x: LoginRejection) :: _ => ctx => ctx.redirect("/auth", StatusCodes.SeeOther)
  }

  def showErrorResponses(request: HttpRequest): Any => Option[LogEntry] = {
    case HttpResponse(StatusCodes.OK, _, _, _)       => Some(LogEntry("200: " + request.uri, InfoLevel))
    case HttpResponse(StatusCodes.NotFound, _, _, _) => Some(LogEntry("404: " + request.uri, WarningLevel))
    case response => Some(LogEntry("Non-200 response for\n  Request : " + request + "\n  Response: " + response, WarningLevel))
  }

}