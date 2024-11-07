package password.core

import cats.NonEmptyTraverse
import cats.data.{ Chain, NonEmptyChain }
import cats.effect.IO
import cats.effect.kernel.Fiber
import cats.effect.std.Supervisor
import cats.syntax.all.*
import password.*
import password.command.PasswordCommand
import password.domain.{ PasswordStatus, PasswordStatusRow }
import password.event.PasswordEvent

import scala.concurrent.duration.*

class PasswordManager(
    supervisor: Supervisor[IO],
    passwordSnapshotWriter: PasswordSnapshotWriter,
    passwordSnapshotReader: PasswordSnapshotReader,
    commandConsumer: Consumer[PasswordCommand],
    eventConsumer: Consumer[PasswordEvent],
    commandProducer: Producer[PasswordCommand],
    eventProducer: Producer[PasswordEvent],
    passwordEncryptDecryptor: PasswordEncryptDecryptor
):
  def handleCommand(pc: PasswordCommand): IO[Unit] =
    commandProducer.send(pc) *> commandConsumer.acknowledgement(Consumer.MsgId.Command(pc.id))
  def handleEvent(pe: PasswordEvent): IO[Unit] =
    eventProducer.send(pe) *> eventConsumer.acknowledgement(Consumer.MsgId.Event(pe.id))

  def writesStatusesTask: IO[Fiber[IO, Throwable, Unit]] =
    supervisor.supervise(
      eventConsumer.receive.groupWithin(1000, 100.milliseconds)
        .evalTap(chunk =>
          NonEmptyChain.fromChain(chunk.toChain)
            .traverse_(nec => passwordSnapshotWriter.writeBatchEvents(nec))
        ).compile
        .drain
    )

  private def toStatus(row: PasswordStatusRow) =
    passwordEncryptDecryptor.decrypt(row.password).map(p =>
      PasswordStatus(row.id, row.name, p, row.comment, row.createdAt, row.deleted)
    )

  def read(id: PasswordId): IO[Option[PasswordStatus]] =
    passwordSnapshotReader.read(id)
      .flatMap(_.traverse(toStatus))
  def readBatch[R[_]: NonEmptyTraverse](ids: R[PasswordId]): IO[Chain[PasswordStatus]] =
    passwordSnapshotReader.readBatch(ids).flatMap(_.traverse(toStatus))
