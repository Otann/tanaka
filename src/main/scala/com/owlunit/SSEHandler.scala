//package com.owlunit
//
//import akka.actor.{Props, ReceiveTimeout, Actor}
//import spray.http._
//import spray.http.HttpHeaders.`Cache-Control`
//import spray.http.HttpResponse
//import spray.routing.RequestContext
//import spray.http.ChunkedResponseStart
//import akka.actor.IO.Closed
//import scala.concurrent.duration._
//
///**
// * Server Side Event handler for incoming request
// * @param ctx incoming request context
// */
//class SSEHandler(ctx: RequestContext) extends Actor {
//
//  val responseStart = HttpResponse(
//    headers = `Cache-Control`(CacheDirectives.`no-cache`) :: Nil,
//    entity = ":" + (" " * 2049) + "\n" // 2k padding for IE using Yaffle
//  )
//
//  var closedHandlers: List[() => Unit] = Nil
//
//  ctx.responder ! ChunkedResponseStart(responseStart)
//
//  // Ask akka to send ReceiveTimeout for purpose of Keep-Alive
//  context.setReceiveTimeout(15 seconds)
//
//  def receive = {
//
//    case Message(data, event, id) =>
//      val idString    = id.map(id => s"id: $id\n").getOrElse("")
//      val eventString = event.map(ev => s"event: $ev\n").getOrElse("")
//      val dataString  = data.split("\n").map(d => s"data: $d\n").mkString
//      ctx.responder ! MessageChunk(s"$idString$eventString$dataString\n")
//
//    case CloseConnection => ctx.responder ! ChunkedMessageEnd
//
//    case ReceiveTimeout  => ctx.responder ! MessageChunk(":\n") // Comment to keep connection alive
//
//    case RegisterClosedHandler(handler) => closedHandlers ::= handler
//
//    case Closed(_, reason) =>
//      closedHandlers.foreach(_())
//      context.stop(self)
//
//  }
//
//}
//
//object SSEHandler {
//
//  def props(ctx: RequestContext) = Props(classOf[SSEHandler], ctx)
//
//}
//
//case class Message(data: String, event: Option[String] = None, id: Option[String] = None)
//case class RegisterClosedHandler(handler: () => Unit)
//object CloseConnection
