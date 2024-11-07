package password.server

import cats.effect.IO
import cats.syntax.all.*
import password.command.PasswordCommand
import password.core.*
import password.event.PasswordEvent
import password.{ EventId, PasswordState }

object Engine:
  def fsm(
      producer: Producer[PasswordEvent],
      passwordAcker: Consumer[PasswordCommand]
  ): FSM[IO, PasswordState, Consumer.Msg[PasswordCommand], Unit] =
    def sendEvent(
        ack: IO[Unit],
        st: PasswordState,
        cmd: PasswordCommand
    ): IO[(PasswordState, Unit)] = {
      val (nst, evt) = PasswordEngine.fsm.run(st, cmd)
      (GenUUID[IO].make[EventId], Time[IO].timestamp).mapN{(id, ts) =>
        evt(id, ts)
      }.flatMap(e => IO.println(s"Try to send $e") *> producer.send(e)) *> ack.tupleLeft(nst)
    }
      .handleErrorWith { e =>
        IO.println(s"Transaction failed: ${e.getMessage}").tupleLeft(st)
      }

    FSM {
      case (st, Consumer.Msg(msgId, cmd)) =>
        IO.println(s"$st $msgId $cmd") *>
        sendEvent(passwordAcker.acknowledgement(msgId), st, cmd)
    }
