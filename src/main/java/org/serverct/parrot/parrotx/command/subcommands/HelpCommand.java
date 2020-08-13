package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.Map;

public class HelpCommand implements PCommand {
    private final String permission;
    private final PPlugin plugin;
    private final CommandHandler commandHandler;
    private final Map<String, PCommand> subCommands;

    public HelpCommand(PPlugin plugin, String perm, CommandHandler commandHandler) {
        this.plugin = plugin;
        this.permission = perm;
        this.commandHandler = commandHandler;
        this.subCommands = commandHandler.getCommands();
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getDescription() {
        return "查看插件或指定子指令的帮助信息";
    }

    @Override
    public String[] getHelp() {
        return new String[]{
                "&9&l" + plugin.getName() + " &7指令帮助 ᚏᚎᚍᚔᚓᚒᚑᚐ",
                "  &9▶ &d/" + plugin.getCmdHandler().mainCmd + " help " + optionalParam("子指令"),
                "    &7&o" + getDescription(),
                "    &7所需权限: &c" + (getPermission() == null ? "无" : getPermission())
        };
    }

    @Override
    public String[] getParams(int arg) {
        if (arg == 0) return subCommands.keySet().toArray(new String[0]);
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            // plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
            this.commandHandler.formatHelp().forEach(sender::sendMessage);
        } else {
            if (subCommands.containsKey(args[1]))
                for (String help : subCommands.get(args[0]).getHelp()) sender.sendMessage(I18n.color(help));
            else
                sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.WARN, "未知子命令, 输入 &d/" + plugin.getCmdHandler().mainCmd + " help &7获取插件帮助."));
        }
        return true;
    }
}
