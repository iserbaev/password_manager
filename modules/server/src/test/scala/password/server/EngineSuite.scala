package password.server

import cats.effect.std.Queue
import cats.effect.{ IO, Resource }
import cats.syntax.all.*
import fs2.Stream
import password.*
import password.command.PasswordCommand
import password.core.{ Consumer, Producer }
import password.domain.PasswordStatus
import password.event.PasswordEvent
import weaver.scalacheck.Checkers
import weaver.{ Expectations, SimpleIOSuite }

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.*

object EngineSuite extends SimpleIOSuite with Checkers:
  def resources =
    for
      commandQueue    <- Resource.eval(Queue.unbounded[IO, Option[PasswordCommand]])
      eventQueue      <- Resource.eval(Queue.unbounded[IO, Option[PasswordEvent]])
      commandProducer <- Producer.local(commandQueue)
      eventProducer   <- Producer.local(eventQueue)
      commandConsumer <- Consumer.local(commandQueue)
      eventConsumer   <- Consumer.local(eventQueue)
      fsm = Engine.fsm(eventProducer, commandConsumer)
    yield (commandQueue, eventQueue, commandProducer, commandConsumer, eventProducer, eventConsumer, fsm)

  def testFSM(commands: List[PasswordCommand], expected: List[PasswordEvent]): IO[Expectations] =
    Stream
      .resource(resources)
      .evalMap {
        (commandQueue, _, commandProducer, commandConsumer, _, eventConsumer, fsm) =>

          val engineProgram: Stream[IO, (Map[PasswordId, PasswordStatus], Unit)] =
            commandConsumer.receiveM.evalMapAccumulate(Map.empty[PasswordId, PasswordStatus]) { case (state, msg) =>
              fsm.run(state, msg)
            }

          val populateProgram: IO[Unit] = commands.traverse_(commandProducer.send) *> commandQueue.offer(None)

          populateProgram *>
            eventConsumer.receive
              .concurrently(engineProgram)
              .interruptAfter(1.second)
              .compile.toList.map(result =>
                expect.same(expected.map(_.command), result.map(_.command))
                  .and(expect.same(expected.map(_.ordinal), result.map(_.ordinal)))
              )
      }
      .compile
      .lastOrError

  private def commandId  = CommandId(UUID.randomUUID())
  private def cid        = CorrelationId(UUID.randomUUID())
  private def eventId    = EventId(UUID.randomUUID())
  private def passwordId = PasswordId(UUID.randomUUID())

  test("Server engine fsm for one cmd") {
    val createCmd =
      PasswordCommand.Create(
        commandId,
        cid,
        passwordId,
        Name("pass"),
        Comment("comn"),
        RawPassword("pass"),
        Timestamp(Instant.now)
      )
    testFSM(
      List(createCmd),
      List(PasswordEvent.CommandExecuted(eventId, createCmd.cid, createCmd, Timestamp(Instant.now)))
    )
  }

  test("Server engine fsm for cud and reject late update") {
    val createCmd =
      PasswordCommand.Create(
        commandId,
        cid,
        passwordId,
        Name("pass1"),
        Comment("comn1"),
        RawPassword("pass1"),
        Timestamp(Instant.now)
      )
    val updateCmd =
      PasswordCommand.Update(
        commandId,
        createCmd.cid,
        createCmd.passwordId,
        Name("pass2"),
        Comment("comn2"),
        RawPassword("pass2"),
        Timestamp(Instant.now)
      )
    val deleteCmd =
      PasswordCommand.Delete(commandId, createCmd.cid, createCmd.passwordId, Timestamp(Instant.now))
    val updateCmd2 =
      PasswordCommand.Update(
        commandId,
        createCmd.cid,
        createCmd.passwordId,
        Name("pass2"),
        Comment("comn2"),
        RawPassword("pass2"),
        Timestamp(Instant.now)
      )

    testFSM(
      List(createCmd, updateCmd, deleteCmd, updateCmd2),
      List(
        PasswordEvent.CommandExecuted(eventId, createCmd.cid, createCmd, Timestamp(Instant.now)),
        PasswordEvent.CommandExecuted(eventId, updateCmd.cid, updateCmd, Timestamp(Instant.now)),
        PasswordEvent.CommandExecuted(eventId, deleteCmd.cid, deleteCmd, Timestamp(Instant.now)),
        PasswordEvent.CommandRejected(eventId, updateCmd2.cid, updateCmd2, Reason(""), Timestamp(Instant.now))
      )
    )
  }
