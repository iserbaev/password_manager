package password

import password.domain.PasswordStatus

import java.time.Instant

export OrphanInstances.given

type Timestamp = Timestamp.Type
object Timestamp extends Newtype[Instant]

type UserId = UserId.Type
object UserId extends IdNewtype

type PasswordId = PasswordId.Type
object PasswordId extends IdNewtype

type CommandId = CommandId.Type
object CommandId extends IdNewtype

type CorrelationId = CorrelationId.Type
object CorrelationId extends IdNewtype

type QueryId = QueryId.Type
object QueryId extends IdNewtype

type EventId = EventId.Type
object EventId extends IdNewtype

type Name = Name.Type
object Name extends Newtype[String]

type Comment = Comment.Type
object Comment extends Newtype[String]

type RawPassword = RawPassword.Type
object RawPassword extends Newtype[String]

type EncryptedPassword = EncryptedPassword.Type
object EncryptedPassword extends Newtype[String]

type Reason = Reason.Type
object Reason extends Newtype[String]

type PasswordState = Map[PasswordId, PasswordStatus]