package com.owlunit.lib

import dispatch._
import scala.util.Try
import scala.concurrent.ExecutionContext

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
  def doRequest[T](request: Req)(func: String => T)(implicit executor: ExecutionContext): Future[T] =
    Http(request OK as.String).map(func)

  
  /**
   * Make a request and process the output as json
   *
   * @param request built request i.e. host("example.com").secure / "sample" / "path"
   * @return json value
   */
  def completeJsonRequest(request: Req)(implicit executor: ExecutionContext): Future[JValue] =
    doRequest(request)( out => parse(out) )

}
