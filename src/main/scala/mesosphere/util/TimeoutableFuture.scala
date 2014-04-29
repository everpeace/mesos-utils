package mesosphere.util

import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException
import scala.util.Try

/**
 * Author: Shingo Omura
 */
object TimeoutableFuture {

  import InterruptibleFuture._

  /**
   * Timeout-able future.
   * This does NOT raise TimeoutException.
   * Instead, this future ends with Failure(TimeoutException).
   *
   * // this doesn't raise TimeoutException after 3 seconds.
   * val f = future(3 seconds){
   *  Thread.sleep(300 * 1000)
   * }
   * // this is true
   * Await(f.failed, 10 seconds).isInstanceOf[TimeoutException]
   *
   * @param timeout timeout duration
   * @param block   code block
   * @tparam T      return value type
   * @return        future with auto timeout
   *                if block is timeout, the thread processing block will be interrupted.
   */
  def future[T](timeout: Duration)(block: => T)(implicit ec: ExecutionContext): Future[T] = {
    val p = Promise[T]()

    scala.concurrent.future {
      val (f, interrupt) = interruptibleFuture(block)
      try {
        val v = Await.result(f, timeout)
        p.tryComplete(Try(v))
      } catch {
        case t: Throwable =>
          interrupt()
          p.tryFailure(t)
      }
    }

    p.future
  }
}
