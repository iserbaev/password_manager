package password.core

import cats.effect.std.Queue
import cats.effect.{ IO, Resource }

trait Producer[A]:
  def send(a: A): IO[Unit]

object Producer:
  def local[A](queue: Queue[IO, Option[A]]): Resource[IO, Producer[A]] =
    Resource.make[IO, Producer[A]](IO.pure((a: A) => queue.offer(Some(a)) <* IO.println(s"Send $a")))(_ => queue.offer(None))
