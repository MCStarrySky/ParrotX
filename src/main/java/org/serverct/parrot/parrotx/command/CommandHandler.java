package org.serverct.parrot.parrotx.command;

import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {

    public final String mainCmd;
    protected final PPlugin plugin;
    @Getter
    protected final Map<String, PCommand> commands = new HashMap<>();
    protected String defaultCmd = null;

    public CommandHandler(@NonNull PPlugin plugin, String mainCmd) {
        this.plugin = plugin;
        this.mainCmd = mainCmd;
    }

    protected void defaultCommand(String cmd) {
        this.defaultCmd = cmd;
    }

    protected void addCommand(String cmd, PCommand executor) {
        if (!commands.containsKey(cmd)) {
            commands.put(cmd, executor);
        } else {
            plugin.getLang().log.error(I18n.REGISTER, "子命令", "重复子命令注册: " + cmd);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            PCommand defCommand = commands.get((Objects.isNull(defaultCmd) ? "help" : defaultCmd));
            if (defCommand == null) {
                // plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
                formatHelp().forEach(sender::sendMessage);
            } else {
                boolean hasPerm = (defCommand.getPermission() == null || defCommand.getPermission().equals("")) || sender.hasPermission(defCommand.getPermission());
                if (hasPerm) {
                    return defCommand.execute(sender, args);
                }

                String msg = plugin.getLang().data.warn("您没有权限这么做.");
                if (sender instanceof Player) {
                    TextComponent text = JsonChatUtil.getFromLegacy(msg);
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&7所需权限 ▶ &c" + defCommand.getPermission()))));
                    ((Player) sender).spigot().sendMessage(text);
                } else sender.sendMessage(msg);
            }
            return true;
        }

        PCommand pCommand = commands.get(args[0].toLowerCase());
        if (pCommand == null) {
            sender.sendMessage(plugin.getLang().data.warn("未知命令, 请检查您的命令拼写是否正确."));
            plugin.getLang().log.error(I18n.EXECUTE, "子命令/" + args[0], sender.getName() + " 尝试执行未注册子命令");
            return true;
        }

        boolean hasPerm = (pCommand.getPermission() == null || pCommand.getPermission().equals("")) || sender.hasPermission(pCommand.getPermission());
        if (hasPerm) {
            String[] newArg = new String[args.length - 1];
            if (args.length >= 2) {
                System.arraycopy(args, 1, newArg, 0, args.length - 1);
            }
            return pCommand.execute(sender, newArg);
        }

        String msg = plugin.getLang().data.warn("您没有权限这么做.");
        if (sender instanceof Player) {
            TextComponent text = JsonChatUtil.getFromLegacy(msg);
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&7所需权限 ▶ &c" + pCommand.getPermission()))));
            ((Player) sender).spigot().sendMessage(text);
        } else sender.sendMessage(msg);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        String[] subCommands = commands.keySet().toArray(new String[0]);
        if (args.length == 0) {
            return new ArrayList<>(Arrays.asList(subCommands));
        } else {
            PCommand command = commands.get(args[0]);
            if (args.length == 1) {
                if (Objects.nonNull(command))
                    return Arrays.asList(command.getParams(0));
                else return query(subCommands, args[0]);
            } else {
                if (Objects.nonNull(command))
                    return query(command.getParams(args.length - 2), args[args.length - 1]);
                else return new ArrayList<>();
            }
        }
    }

    private List<String> query(String[] params, String input) {
        return Arrays.stream(params).filter(s -> s.startsWith(input)).collect(Collectors.toList());
    }

    public List<String> formatHelp() {
        final List<String> result = new ArrayList<>();
        final PluginDescriptionFile description = plugin.getDescription();

        result.add(I18n.color("&9&l{0} &fv{1}", description.getName(), description.getVersion()));

        final String authorList = description.getAuthors().toString();
        if (authorList.length() > 2) {
            final String authors = authorList.substring(1);
            result.add(I18n.color("&7作者: &f{0}", authors));
        }
        result.add("");

        final String prefix = "/" + mainCmd;
        boolean first = true;
        for (Map.Entry<String, PCommand> entry : commands.entrySet()) {
            final String command = entry.getKey();
            final PCommand executor = entry.getValue();

            if (first) {
                result.add(I18n.color("&f{0} {1}", prefix, command));
            } else {
                result.add(I18n.color("{0}&7- &f{0}", I18n.blank(prefix.length() - 1), command));
            }
            result.add(I18n.color("{0} &7{1}", I18n.blank(prefix.length()), executor.getDescription()));
            first = false;
        }

        if (commands.containsKey("help")) {
            result.add("");
            result.add(I18n.color("&6▶ &7使用 &f/{0} help &7指令查看更多信息.", mainCmd));
        }
        return result;
    }

    public void register(final BaseCommand command) {
        addCommand(command.getName().toLowerCase(), command);
    }
}
