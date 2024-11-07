package password

import cats.*
import io.circe.{Decoder, Encoder}

import java.util.UUID
import scala.annotation.unused

abstract class Newtype[A](using eqv: Eq[A], ord: Order[A], shw: Show[A], enc: Encoder[A], dec: Decoder[A]):
  opaque type Type = A

  inline def apply(a: A): Type = a

  inline protected final def derive[F[_]](using ev: F[A]): F[Type] = ev

  extension (t: Type) inline def value: A = t

  given Eq[Type]       = eqv
  given Order[Type]    = ord
  given Show[Type]     = shw
  given Encoder[Type]  = enc
  given Decoder[Type]  = dec
  given Ordering[Type] = ord.toOrdering

abstract class NumNewtype[A](
    using @unused eqv: Eq[A],
    ord: Order[A],
    shw: Show[A],
    enc: Encoder[A],
    dec: Decoder[A],
    num: Numeric[A]
) extends Newtype[A]:

  extension (x: Type)
    inline def -[T](using inv: T =:= Type)(y: T): Type =
      apply(num.minus(x.value, inv.apply(y).value))
    inline def +[T](using inv: T =:= Type)(y: T): Type =
      apply(num.plus(x.value, inv.apply(y).value))

abstract class IdNewtype extends Newtype[UUID]:
  given IsUUID[Type]                = derive[IsUUID]
  def unsafeFrom(str: String): Type = apply(UUID.fromString(str))
