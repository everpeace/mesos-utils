package mesosphere.util

import scala.concurrent._
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference

/**
 * Author: Shingo Omura
 */
object InterruptibleFuture {

  /**
   * This provides interruptible future by returning future itself and
   * a function which you can interrupt to the future's thread.
   *
   * example:
   *   import scala.concurrent._
   *   import mesosphere.util.InterruptibleFuture._
   *   import ExecutionContext.Implicits.global
   *
   *   val (f, interrupt) = interruptibleFuture { Thread.sleep(10 * 1000) }
   *   try{
   *     Await.result(f, 10 seconds)
   *   } catch {
   *     case e => interrupt()
   *   }
   *
   * @param block body
   * @param ec execution context
   * @tparam T return value type
   * @return (f, () => Boolean): f is future,
   *         () => Boolean is for interrupting the future.
   *         the interrupting function returns `false` if f has already been completed,
   *         `true` otherwise.
   */
  def interruptibleFuture[T](block: => T)(implicit ec: ExecutionContext): (Future[T], () => Boolean) = {
    val p = Promise[T]()
    val currentThread  = new AtomicReference[Thread](null)

    p tryCompleteWith future {
      val thread = Thread.currentThread()
      currentThread.set(thread)
      // you can interrupt during block runs.
      try block finally { currentThread.set(null) }
    }

    (p.future , () => Option(currentThread.getAndSet(null)) exists {
      t =>
        t.interrupt()
        p.tryFailure(new CancellationException())
    })
  }
}
