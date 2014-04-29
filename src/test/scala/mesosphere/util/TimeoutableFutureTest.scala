package mesosphere.util

import language.postfixOps
import org.scalatest.FlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await
import java.util.concurrent.TimeoutException

/**
 * Author: Shingo Omura
 */
class TimeoutableFutureTest extends FlatSpec{
  import scala.concurrent.ExecutionContext.Implicits.global
  import TimeoutableFuture._

  it should "Timeout(3 second){...} makes sure to be timed out in 3 seconds." in {

    val f = future (3 seconds){
      Thread.sleep(30 * 1000)
    }

    assert(Await.result(f.failed, 30 seconds).isInstanceOf[TimeoutException])
  }
}
