package password.domain

import cats.{ Eq, Show }
import cats.derived.*
import cats.syntax.all.*
import password.*

final case class PasswordStatus(
    id: PasswordId,
    name: Name,
    password: RawPassword,
    comment: Comment,
    createdAt: Timestamp,
    deleted: Option[Timestamp]
) derives Eq, Show


