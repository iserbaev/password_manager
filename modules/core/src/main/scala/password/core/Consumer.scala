package password.core

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.std.Queue
import fs2.Stream
import password.{ CommandId, EventId }

trait Consumer[A]:
  def receiveM: Stream[IO, Consumer.Msg[A]]
  def receive: Stream[IO, A]
  def acknowledgement(msgId: Consumer.MsgId): IO[Unit]

object Consumer:
  enum MsgId:
    case Dummy
    case Command(c: CommandId)
    case Event(e: EventId)

  final case class Msg[A](id: MsgId, payload: A)

  def local[A](queue: Queue[IO, Option[A]]): Resource[IO, Consumer[A]] =
    Resource.make[IO, Consumer[A]](
      IO.pure(
        new:
          def receiveM: Stream[IO, Msg[A]] = receive.map(Msg(MsgId.Dummy, _))
          def receive: Stream[IO, A] =
            Stream.fromQueueNoneTerminated(queue).evalTap(msg => IO.println(s"Received $msg"))
          def acknowledgement(msgId: Consumer.MsgId): IO[Unit] = IO.println(s"Send ack $msgId") *> IO.unit
      )
    )(_ => queue.offer(None))
