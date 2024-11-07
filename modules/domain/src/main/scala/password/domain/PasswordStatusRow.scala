package password.domain

import cats.derived.*
import cats.syntax.all.*
import cats.{ Eq, Show }
import password.*

final case class PasswordStatusRow(
    id: PasswordId,
    name: Name,
    password: EncryptedPassword,
    comment: Comment,
    createdAt: Timestamp,
    deleted: Option[Timestamp]
) derives Eq, Show
