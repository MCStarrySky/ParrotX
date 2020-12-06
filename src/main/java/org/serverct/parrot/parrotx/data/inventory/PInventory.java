package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;

import java.io.File;
import java.util.*;

public abstract class PInventory<T> extends AutoRefreshInventory {

    @Getter
    protected final T data;
    @Getter
    private final Map<String, InventoryElement> elementMap = new HashMap<>();
    @Getter
    private final Map<Integer, String> slotMap = new HashMap<>();
    protected Inventory inventory;

    public PInventory(PPlugin plugin, T data, Player user, File file) {
        super(new FileDefinedInventory(plugin, user, file));
        this.data = data;
    }

    public PInventory(PPlugin plugin, T data, Player user, String title, int row) {
        super(new BaseInventory(plugin, user, title, row));
        this.data = data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (Objects.isNull(this.inventory)) {
            this.inventory = construct(this);
        }
        return this.inventory;
    }

    @Override
    public Inventory construct(final InventoryHolder executor) {
        final Inventory result = base.construct(this);

        final List<InventoryElement> elements = new ArrayList<>(this.elementMap.values());
        elements.sort(Comparator.comparingInt(InventoryElement::getPriority));

        for (InventoryElement element : elements) {
            final BaseElement base = element.preload(this);

            if (!base.condition(super.base.viewer)) {
                continue;
            }

            for (int slot : element.getPositions()) {
                this.slotMap.put(slot, base.getName());
                result.setItem(slot, element.parseItem(this, slot));
            }
        }

        return result;
    }

    @Override
    public void open(InventoryOpenEvent event) {
        startRefresh(event.getInventory());
        final Sound sound = base.getSetting("Sound.Open", Sound.class);
        if (Objects.nonNull(sound)) {
            final Player user = base.getViewer();
            user.playSound(user.getLocation(), sound, 1, 1);
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        endRefresh();
        final Sound sound = base.getSetting("Sound.Close", Sound.class);
        if (Objects.nonNull(sound)) {
            final Player user = base.getViewer();
            user.playSound(user.getLocation(), sound, 1, 1);
        }
    }

    @Override
    public void execute(InventoryClickEvent event) {
        if (!check(this, event)) {
            return;
        }

        final InventoryElement element = getElement(event.getSlot());
        if (Objects.isNull(element) || !element.isClickable()) {
            event.setCancelled(true);
            return;
        }

        element.click(this, event);
    }

    protected void addElement(InventoryElement element) {
        this.elementMap.put(element.getBase().getName(), element);
    }

    public InventoryElement getElement(String name) {
        return this.elementMap.get(name);
    }

    public InventoryElement getElement(int slot) {
        return getElement(this.slotMap.get(slot));
    }
}
