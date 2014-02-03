package com.owlunit.authentication

import spray.routing.Rejection

/**
 * All possible authentication rejections
 */
trait LoginRejection extends Rejection
object FacebookTokenMismatch extends LoginRejection
object FacebookFail extends LoginRejection