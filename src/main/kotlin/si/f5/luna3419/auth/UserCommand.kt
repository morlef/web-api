package si.f5.luna3419.auth

import si.f5.luna3419.configLoader
import si.f5.luna3419.logger
import si.f5.luna3419.`object`.UserData
import org.mindrot.jbcrypt.BCrypt

class UserCommand {
    fun onCommand(args: List<String>) {
        if (args.size < 2) {
            logger.info("user add [UserID] [Password]")
            logger.info("user delete [UserID]")
            return
        }
        when (args[0].lowercase()) {
            "add" -> {
                if (args.size < 3) return

                val password = args[2]
                val salt = BCrypt.gensalt()

                val hashed = BCrypt.hashpw(password, salt)

                userConfig.add(UserData(args[1], hashed))
                logger.info("Successfully created user ${args[1]}! (hashed password = $hashed)")
            }
            "delete" -> {
                if (userConfig.removeIf { it.userId == args[1] }) {
                    logger.info("Successfully deleted user ${args[1]}!")
                }
            }
        }
        configLoader.saveUsers(userConfig)
    }
}