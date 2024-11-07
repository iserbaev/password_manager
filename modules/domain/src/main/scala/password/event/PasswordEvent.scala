package password.event

import cats.derived.*
import cats.syntax.all.*
import cats.{Applicative, Eq, Show}
import io.circe.Codec
import monocle.Traversal
import password.*
import password.command.PasswordCommand

enum PasswordEvent derives Codec.AsObject, Show:
  def id: EventId
  def cid: CorrelationId
  def command: PasswordCommand
  def createdAt: Timestamp

  case CommandExecuted(
      id: EventId,
      cid: CorrelationId,
      command: PasswordCommand,
      createdAt: Timestamp
  )

  case CommandRejected(
      id: EventId,
      cid: CorrelationId,
      command: PasswordCommand,
      reason: Reason,
      createdAt: Timestamp
  )

object PasswordEvent:
  given Eq[PasswordEvent] = Eq.and(Eq.by(_.cid), Eq.by(_.command))

  val _CorrelationId: Traversal[PasswordEvent, CorrelationId] = new:
    def modifyA[F[_] : Applicative](f: CorrelationId => F[CorrelationId])(s: PasswordEvent): F[PasswordEvent] =
      f(s.cid).map { newCid =>
        s match
          case c: CommandExecuted => c.copy(cid = newCid)
          case c: CommandRejected => c.copy(cid = newCid)
      }
  
  