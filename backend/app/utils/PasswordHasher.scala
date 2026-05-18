package utils

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {

  def hash(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt(12))
  }

  def verify(password: String, hash: String): Boolean = {
    BCrypt.checkpw(password, hash)
  }
}
