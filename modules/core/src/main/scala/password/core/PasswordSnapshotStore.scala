package password.core

import cats.NonEmptyTraverse
import cats.data.Chain
import cats.effect.IO
import cats.effect.kernel.{ Ref, Resource }
import cats.syntax.all.*
import password.PasswordId
import password.command.PasswordCommand
import password.domain.PasswordStatusRow
import password.event.PasswordEvent

trait PasswordSnapshotWriter:
  def encryptDecryptor: PasswordEncryptDecryptor

  def write(s: PasswordStatusRow): IO[Unit]
  def writeBatch[R[_]: NonEmptyTraverse](s: R[PasswordStatusRow]): IO[Unit]
  def delete(id: PasswordId): IO[Unit]

  def writeEvent(s: PasswordEvent): IO[Unit] =
    s.command match
      case PasswordCommand.Create(id, cid, passwordId, name, comment, password, createdAt) =>
        encryptDecryptor.encrypt(password).flatMap(e =>
          write(PasswordStatusRow(passwordId, name, e, comment, createdAt, None))
        )
      case PasswordCommand.Update(id, cid, passwordId, name, comment, password, createdAt) =>
        encryptDecryptor.encrypt(password).flatMap(e =>
          write(PasswordStatusRow(passwordId, name, e, comment, createdAt, None))
        )
      case PasswordCommand.Delete(id, cid, passwordId, createdAt) =>
        delete(passwordId)

  def writeBatchEvents[R[_]: NonEmptyTraverse](s: R[PasswordEvent]): IO[Unit] =
    s.traverse_(writeEvent)

object PasswordSnapshotWriter:
  def local(
      ref: Ref[IO, Map[PasswordId, PasswordStatusRow]],
      passwordEncryptDecryptor: PasswordEncryptDecryptor
  ): Resource[IO, PasswordSnapshotWriter] =
    Resource.make[IO, PasswordSnapshotWriter](
      IO.pure(
        new:
          val encryptDecryptor: PasswordEncryptDecryptor                            = passwordEncryptDecryptor
          def write(s: PasswordStatusRow): IO[Unit]                                 = ref.update(_.updated(s.id, s))
          def delete(id: PasswordId): IO[Unit]                                      = ref.update(_.removed(id))
          def writeBatch[R[_]: NonEmptyTraverse](s: R[PasswordStatusRow]): IO[Unit] = s.traverse_(write)
      )
    )(_ => IO.unit)

trait PasswordSnapshotReader:
  def read(id: PasswordId): IO[Option[PasswordStatusRow]]
  def readBatch[R[_]: NonEmptyTraverse](ids: R[PasswordId]): IO[Chain[PasswordStatusRow]]

object PasswordSnapshotReader:
  def local(ref: Ref[IO, Map[PasswordId, PasswordStatusRow]]): Resource[IO, PasswordSnapshotReader] =
    Resource.make[IO, PasswordSnapshotReader](
      IO.pure(
        new:
          def read(id: PasswordId): IO[Option[PasswordStatusRow]] = ref.get.map(_.get(id))
          def readBatch[R[_]: NonEmptyTraverse](ids: R[PasswordId]): IO[Chain[PasswordStatusRow]] =
            ref.get.map(m => Chain.fromSeq(ids.map(m.get).toList.flatten))
      )
    )(_ => IO.unit)
