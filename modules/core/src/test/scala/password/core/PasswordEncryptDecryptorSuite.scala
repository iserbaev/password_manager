package password.core

import password.RawPassword
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object PasswordEncryptDecryptorSuite extends SimpleIOSuite with Checkers:
  test("PasswordEncryptDecryptor.local should correclty ecnrypt and decrypt") {
    val password = RawPassword("test")
    val local    = PasswordEncryptDecryptor.local
    local.encrypt(password)
      .flatMap(local.decrypt)
      .map(expect.same(password, _))
  }
