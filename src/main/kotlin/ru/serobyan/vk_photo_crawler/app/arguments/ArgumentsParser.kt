package ru.serobyan.vk_photo_crawler.app.arguments

import org.apache.commons.cli.*

object ArgumentsParser {
    fun parse(args: Array<String>): Arguments {
        try {
            val cmd = parser.parse(options, args)
            val commands: Set<AppCommand>? = parseCommands(cmd.getOptionValue(optionCommands))
            val vkGroupUrl: String = cmd.getOptionValue(optionVkGroupUrl)
            val login: String = cmd.getOptionValue(optionLogin)
            val password: String = cmd.getOptionValue(optionPassword)
            return Arguments(
                commands = commands?.takeIf { it.isNotEmpty() } ?: AppCommand.values().toSet(),
                vkGroupUrl = vkGroupUrl,
                login = login,
                password = password
            )
        } catch (e: ParseException) {
            formatter.printHelp("vk_photo_crawler", options)
            throw e
        }
    }

    private fun parseCommands(commands: String?): Set<AppCommand>? {
        return commands
            ?.split("")
            ?.filter { it.isNotBlank() }
            ?.map { AppCommand.fromConsoleCommand(it) }
            ?.toSet()
    }

    private val parser: CommandLineParser = DefaultParser()
    private val formatter = HelpFormatter()

    private const val optionCommands = "commands"
    private const val optionVkGroupUrl = "vkGroupUrl"
    private const val optionLogin = "login"
    private const val optionPassword = "password"

    private val commands = Option.builder("c")
        .longOpt(optionCommands)
        .desc(AppCommand.availableConsoleCommands)
        .numberOfArgs(1)
        .required(false)
        .build()

    private val vkGroupUrl = Option.builder("g")
        .longOpt(optionVkGroupUrl)
        .desc("For example 'https://vk.com/academicart'")
        .numberOfArgs(1)
        .required(true)
        .build()

    private val login = Option.builder("l")
        .longOpt(optionLogin)
        .desc("Login for vk")
        .numberOfArgs(1)
        .required(true)
        .build()

    private val password = Option.builder("p")
        .longOpt(optionPassword)
        .desc("Password for vk")
        .numberOfArgs(1)
        .required(true)
        .build()

    val options = Options().apply {
        addOption(commands)
        addOption(vkGroupUrl)
        addOption(login)
        addOption(password)
    }
}