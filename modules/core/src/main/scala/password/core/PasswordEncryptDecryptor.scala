package password.core

import cats.derived.*
import cats.effect.IO
import cats.syntax.all.*
import cats.{ Eq, Show }
import io.circe.Codec
import password.*

import java.util.Base64

trait PasswordEncryptDecryptor:
  def encrypt(raw: RawPassword): IO[EncryptedPassword] = encrypt(raw, PasswordEncryptDecryptor.EncryptionMethod.Base64)
  def decrypt(enc: EncryptedPassword): IO[RawPassword] = decrypt(enc, PasswordEncryptDecryptor.EncryptionMethod.Base64)

  def encrypt(raw: RawPassword, method: PasswordEncryptDecryptor.EncryptionMethod): IO[EncryptedPassword]
  def decrypt(enc: EncryptedPassword, method: PasswordEncryptDecryptor.EncryptionMethod): IO[RawPassword]

object PasswordEncryptDecryptor:
  enum EncryptionMethod derives Codec.AsObject, Eq, Show:
    case Base64
    case AES // TODO
    case RSA // TODO

  def local: PasswordEncryptDecryptor = new PasswordEncryptDecryptor:
    private val base64EncryptDecryptor = EncryptDecryptor.base64
    override def encrypt(raw: RawPassword, method: EncryptionMethod): IO[EncryptedPassword] = method match
      case EncryptionMethod.Base64 => base64EncryptDecryptor.encrypt(raw)
      case EncryptionMethod.AES    => IO.stub
      case EncryptionMethod.RSA    => IO.stub

    override def decrypt(enc: EncryptedPassword, method: EncryptionMethod): IO[RawPassword] = method match
      case EncryptionMethod.Base64 => base64EncryptDecryptor.decrypt(enc)
      case EncryptionMethod.AES    => IO.stub
      case EncryptionMethod.RSA    => IO.stub

trait EncryptDecryptor:
  def encrypt(s: RawPassword): IO[EncryptedPassword]
  def decrypt(e: EncryptedPassword): IO[RawPassword]

object EncryptDecryptor:
  def base64: EncryptDecryptor = new EncryptDecryptor:
    override def encrypt(s: RawPassword): IO[EncryptedPassword] =
      IO(EncryptedPassword(Base64.getEncoder.encodeToString(s.value.getBytes)))

    override def decrypt(e: EncryptedPassword): IO[RawPassword] =
      IO(RawPassword(new String(Base64.getDecoder.decode(e.value.getBytes))))
