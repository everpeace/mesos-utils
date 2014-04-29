package mesosphere.util

import language.postfixOps
import org.scalatest.FlatSpec
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.concurrent.CancellationException

/**
 * Author: Shingo Omura.
 */
class InterruptibleFutureTest extends FlatSpec {

  import InterruptibleFuture._
  import scala.concurrent.ExecutionContext.Implicits.global

  it should "be able to interrupted when thread didn't finish" in {

    val (f, interrupt) = interruptibleFuture {
      // sleep raises InterruptedException if it was interrupt()-ed.
      Thread.sleep(1000 * 1000)
    }

    assert(interrupt() == true)
    assert(f.isCompleted)
    assert(Await.result(f.failed, 1 second).isInstanceOf[CancellationException])
  }

  it should "not be able to interrupted when thread finished" in {

    val (f, interrupt) = interruptibleFuture {
      3
    }
    Thread.sleep(100)

    assert(interrupt() == false)
    assert(f.isCompleted)
    assert(Await.result(f, 1 second) == 3)
  }


}
