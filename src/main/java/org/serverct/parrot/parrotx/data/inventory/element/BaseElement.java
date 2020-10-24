package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.BaseInventory;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public @Data
@Builder
class BaseElement implements InventoryElement {
    private final int priority;
    private final String name;
    private final Supplier<ItemStack> item;
    private final String xPos;
    private final String yPos;
    private final Predicate<Player> condition;

    public static BaseElement of(final PPlugin plugin, final ConfigurationSection section, final int priority, final Predicate<Player> condition) {
        final String name = section.getName();
        final Supplier<ItemStack> item = () -> ItemUtil.build(plugin, section);
        final ConfigurationSection posSection = section.getConfigurationSection("Position");
        if (BasicUtil.isNull(plugin, posSection, I18n.LOAD, "Gui 元素 " + name, "未找到 Position 数据节")) {
            return null;
        }
        final String x = posSection.getString("X", "0");
        final String y = posSection.getString("Y", "0");
        return BaseElement.builder()
                .priority(priority)
                .name(name)
                .item(item)
                .xPos(x)
                .yPos(y)
                .condition(condition)
                .build();
    }

    public boolean condition(final Player user) {
        return Objects.isNull(condition) || condition.test(user);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return this;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public List<Integer> getPositions() {
        if (Objects.isNull(xPos) || Objects.isNull(yPos)) {
            return new ArrayList<>();
        }
        return Position.get(xPos, yPos);
    }

    @Override
    public ItemStack parseItem(BaseInventory<?> inv, int slot) {
        return this.item.get();
    }

    @Override
    public BaseElement preload(BaseInventory<?> inv) {
        return this;
    }

    @Override
    public void click(final BaseInventory<?> holder, final InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
