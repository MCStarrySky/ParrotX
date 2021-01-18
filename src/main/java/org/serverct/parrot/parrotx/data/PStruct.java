package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.AutoLoader;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class PStruct extends AutoLoader implements UniqueData {

    protected final PID id;
    protected final ConfigurationSection section;
    @Getter
    protected final Map<String, Object> dataMap = new HashMap<>();
    private final String typeName;
    @Setter
    protected boolean readOnly = false;

    public PStruct(PID id, ConfigurationSection section, String typeName) {
        super(id.getPlugin());
        this.id = id;
        this.section = section;
        this.typeName = typeName;
    }

    @Override
    public void load() {
        defaultFrom(this.section);
        defaultTo(this);
        autoLoad();
    }

    @Override
    public void save() {
        autoSave();
    }

    @Override
    public String name() {
        return this.typeName + "/" + this.id.getId();
    }

    @Override
    public void init() {
    }

    @Override
    public void saveDefault() {
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Nullable
    public <T> T getAs(final String path, final Class<T> clazz) {
        final Object object = this.dataMap.get(path);
        if (Objects.isNull(object)) {
            return null;
        }
        return clazz.cast(object);
    }

    @Override
    public void reload() {
        plugin.getLang().log.action(I18n.RELOAD, name());
        load();
    }

    @Override
    public PID getID() {
        return this.id;
    }

    @Override
    public void delete() {
    }
}
