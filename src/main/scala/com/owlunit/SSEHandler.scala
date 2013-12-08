package com.owlunit

import akka.actor.{ActorLogging, Props, ReceiveTimeout, Actor}
import spray.http._
import spray.http.HttpResponse
import spray.routing.RequestContext
import spray.http.ChunkedResponseStart
import scala.concurrent.duration._
import java.util.UUID
import spray.can.Http.ConnectionClosed

/**
* Server Side Event handler for incoming request
* @param ctx incoming request context
*/
class SSEHandler(ctx: RequestContext) extends Actor with ActorLogging {
  import SSEHandler._

  val responseStart = {
    val mediaType   = MediaType.custom("text/event-stream")
    val contentType = ContentType(mediaType, None)
    val body        = ":" + (" " * 2049) + "\n" // 2k padding for IE using Yaffle
    val entity      = HttpEntity(contentType, body)
    HttpResponse(StatusCodes.OK, entity)
  }

  var closedHandlers: List[() => Unit] = Nil

  log.debug(s"SSEHandler sending responseStart: $responseStart")
  ctx.responder ! ChunkedResponseStart(responseStart)

  // Ask akka to send ReceiveTimeout for purpose of Keep-Alive
  context.setReceiveTimeout(15 seconds)

  def receive = {

    case Message(data, id) =>
      val dataString  = data.split("\n").map(d => s"data: $d\n").mkString
      val content     = s"id: $id\n$dataString\n"
      val chunk       = MessageChunk(content)
      ctx.responder ! chunk

    case CloseConnection => ctx.responder ! ChunkedMessageEnd

    case ReceiveTimeout  => ctx.responder ! MessageChunk(":\n") // Comment to keep connection alive

    case RegisterClosedHandler(handler) => closedHandlers ::= handler

    case msg: ConnectionClosed =>
      closedHandlers.foreach(_())
      context.stop(self)

  }

}

object SSEHandler {

  def props(ctx: RequestContext) = Props(classOf[SSEHandler], ctx)

  case class Message(data: String, id: String = UUID.randomUUID().toString)
  case class RegisterClosedHandler(handler: () => Unit)
  object CloseConnection

}

