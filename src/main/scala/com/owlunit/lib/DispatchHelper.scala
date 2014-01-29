package com.owlunit.lib

import dispatch._
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s._
import org.json4s.native.JsonMethods._

/**
 * Helper for wrapping common cases of using Dispatch
 */
trait DispatchHelper {

  /**
   * Make a request and process the output with the given function
   *
   * @param request built request i.e. host("example.com").secure / "sample" / "path"
   * @param func function to applied to result
   * @tparam T return type of func
   * @return result of the func applied to result
   */
  def doRequest[T](request: Req)(func: String => Try[T]): Future[Try[T]] = Http(request OK as.String).map(func)

  
  /**
   * Make a request and process the output as json
   *
   * @param request built request i.e. host("example.com").secure / "sample" / "path"
   * @return json value
   */
  def completeJsonRequest(request: Req): Future[Try[JValue]] = doRequest(request)( out => Try { parse(out) } )

}
