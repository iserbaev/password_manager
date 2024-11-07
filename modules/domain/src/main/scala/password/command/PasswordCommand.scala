package password.command

import cats.derived.*
import cats.syntax.all.*
import cats.{Applicative, Eq, Show}
import io.circe.Codec
import monocle.Traversal
import password._

enum PasswordCommand derives Codec.AsObject, Eq, Show:
  def id: CommandId
  def cid: CorrelationId
  def passwordId: PasswordId
  def createdAt: Timestamp

  case Create(
      id: CommandId,
      cid: CorrelationId,
      passwordId: PasswordId,
      name: Name,
      comment: Comment,
      password: RawPassword,
      createdAt: Timestamp
  )

  case Update(
      id: CommandId,
      cid: CorrelationId,
      passwordId: PasswordId,
      name: Name,
      comment: Comment,
      password: RawPassword,
      createdAt: Timestamp
  )

  case Delete(
      id: CommandId,
      cid: CorrelationId,
      passwordId: PasswordId,
      createdAt: Timestamp
  )

object PasswordCommand:
  val _CommandId: Traversal[PasswordCommand, CommandId] = new:
    def modifyA[F[_]: Applicative](f: CommandId => F[CommandId])(s: PasswordCommand): F[PasswordCommand] =
      f(s.id).map { newId =>
        s match
          case c: PasswordCommand.Create => c.copy(id = newId)
          case u: PasswordCommand.Update => u.copy(id = newId)
          case d: PasswordCommand.Delete => d.copy(id = newId)
      }

  val _CorrelationId: Traversal[PasswordCommand, CorrelationId] = new:
    def modifyA[F[_]: Applicative](f: CorrelationId => F[CorrelationId])(s: PasswordCommand): F[PasswordCommand] =
      f(s.cid).map { newCId =>
        s match
          case c: PasswordCommand.Create => c.copy(cid = newCId)
          case u: PasswordCommand.Update => u.copy(cid = newCId)
          case d: PasswordCommand.Delete => d.copy(cid = newCId)
      }

  val _CreatedAt: Traversal[PasswordCommand, Timestamp] = new:
    def modifyA[F[_]: Applicative](f: Timestamp => F[Timestamp])(s: PasswordCommand): F[PasswordCommand] =
      f(s.createdAt).map { newTs =>
        s match
          case c: PasswordCommand.Create => c.copy(createdAt = newTs)
          case u: PasswordCommand.Update => u.copy(createdAt = newTs)
          case d: PasswordCommand.Delete => d.copy(createdAt = newTs)
      }
