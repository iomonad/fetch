/*
 * Copyright 2016-2018 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import cats.temp.par._
import cats.effect._

import monix.execution._
import monix.eval._
import monix.eval.instances._

import fetch._

import org.scalatest.{Matchers, WordSpec}

object DataSources {
  val NumberSource = new DataSource[Int, Int] {
    def fetch[F[_]](id: Int)(
        implicit C: ConcurrentEffect[F],
        P: Par[F]
    ): F[Option[Int]] =
      C.pure(Option(id))
  }

  def number[F[_]: ConcurrentEffect: Par](id: Int): Fetch[F, Int] =
    Fetch(id, NumberSource)
}

class MonixExample extends WordSpec with Matchers {
  import DataSources._

  implicit val scheduler: Scheduler   = Scheduler.io(name = "test-scheduler")
  implicit val t: Timer[Task]         = scheduler.timer
  implicit val cs: ContextShift[Task] = scheduler.contextShift

  "We can run a Fetch into a Monix Task" in {
    def fetch[F[_]: ConcurrentEffect: Par]: Fetch[F, Int] =
      number(1)

    val t: Task[Int] = Fetch.run[Task](fetch)

    // todo: actually test
    // val (env, result) = t.unsafeRunSync

    // result shouldEqual 1
    // env.rounds.size shouldEqual 1
    1 shouldEqual 2
  }

}
