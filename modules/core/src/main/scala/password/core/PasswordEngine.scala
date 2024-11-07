package password.core

import cats.syntax.all.*
import password.command.PasswordCommand
import password.command.PasswordCommand.*
import password.domain.PasswordStatus
import password.event.PasswordEvent
import password.event.PasswordEvent.*
import password.*

object PasswordEngine:
  val fsm = FSM.id[PasswordState, PasswordCommand, (EventId, Timestamp) => PasswordEvent] {
    case (st, cmd @ Create(_, cid, passwordId, name, comment, password, createdAt)) =>
      val nst = st.updated(passwordId, PasswordStatus(passwordId, name, password, comment, createdAt, None))
      nst -> ((id, ts) => CommandExecuted(id, cid, cmd, ts))

    case (st, cmd @ Update(_, cid, passwordId, name, comment, password, createdAt)) =>
      val canUpdate: Boolean = st.get(passwordId).exists(_.deleted.isEmpty)
      def nst                = st.updated(passwordId, PasswordStatus(passwordId, name, password, comment, createdAt, None))

      if canUpdate then
        nst -> ((id, ts) => CommandExecuted(id, cid, cmd, ts))
      else
        st -> ((id, ts) => CommandRejected(id, cid, cmd, Reason("Password not exist"), ts))

    case (st, cmd @ Delete(_, cid, passwordId, createdAt)) =>
      val canDelete: Boolean = st.get(passwordId).exists(_.deleted.isEmpty)

      def nst = st.updatedWith(passwordId)(_.map(_.copy(deleted = createdAt.some)))

      if canDelete then
        nst -> ((id, ts) => CommandExecuted(id, cid, cmd, ts))
      else
        st -> ((id, ts) => CommandRejected(id, cid, cmd, Reason("Password not exist"), ts))

  }

  val eventsFsm = FSM.id[PasswordState, PasswordEvent, Unit] {
    case (st, CommandExecuted(_, _, cmd, _)) =>
      fsm.runS(st, cmd) -> ()
    case (st, CommandRejected(_, _, _, _, _)) =>
      st -> ()
  }
