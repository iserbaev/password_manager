package password.core

import cats.effect.IO
import password.*
import password.command.PasswordCommand
import password.event.PasswordEvent

trait PasswordManager {
  def passwordSnapshotWriter: PasswordSnapshotWriter
  def passwordSnapshotReader: PasswordSnapshotReader
  def commandConsumer: Consumer[PasswordCommand]
  def eventConsumer: Consumer[PasswordEvent]
  def commandProducer: Producer[PasswordCommand]
  def eventProducer: Producer[PasswordEvent]
  def passwordEncryptDecryptor: PasswordEncryptDecryptor
  
  def handleCommand(pc: PasswordCommand): IO[Unit] = commandProducer.send(pc) *> commandConsumer.acknowledgement(Consumer.MsgId.Command(pc.id))
  def handleEvent(pe: PasswordEvent): IO[Unit] = eventProducer.send(pe) *> eventConsumer.acknowledgement(Consumer.MsgId.Event(pe.id))
  
  
}
