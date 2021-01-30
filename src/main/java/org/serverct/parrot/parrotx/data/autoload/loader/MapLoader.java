package org.serverct.parrot.parrotx.data.autoload.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.DataLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class MapLoader implements DataLoader<Map> {

    private static final Map<Class<?>, Function<String, ?>> PARSER_MAP = new HashMap<>();

    static {
        registerParser(String.class, string -> string);
        registerParser(Short.class, Short::parseShort);
        registerParser(Integer.class, Integer::parseInt);
        registerParser(Long.class, Long::parseLong);
        registerParser(Float.class, Float::parseFloat);
        registerParser(Double.class, Double::parseDouble);
    }

    public static void registerParser(@NotNull final Class<?> type,
                                      @NotNull final Function<String, ?> parser) {
        PARSER_MAP.put(type, parser);
    }

    @Override
    public @NotNull Class<Map> getType() {
        return Map.class;
    }

    @Override
    public @Nullable Map load(@NotNull String path, @NotNull ConfigurationSection section,
                              @NotNull List<Class<?>> classChain) {
        final Map<Object, Object> map = new HashMap<>();
        if (classChain.size() < 3) {
            Autoloader.log("加载 Map 数据时未提供泛型类型, 路径: {0}, 数据节: {1}, 类型链: {2}", path, section.getName(), classChain);
            return map;
        }

        final Function<String, ?> keyParser = PARSER_MAP.get(classChain.get(1));
        final DataLoader<?> valueLoader = Autoloader.getLoader(classChain.get(2));
        if (Objects.isNull(keyParser)) {
            Autoloader.log("加载 Map 数据时未注册 Key 对应分析函数: {0}", classChain.get(1));
            return map;
        }
        if (Objects.isNull(valueLoader)) {
            Autoloader.log("加载 Map 数据时未注册 Value 对应加载器: {0}", classChain.get(2));
            return map;
        }

        final ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (Objects.isNull(mapSection)) {
            Autoloader.log("加载 Map 数据时 Path 无效: {0}", path);
            return map;
        }


        final List<Class<?>> cacheTypes = classChain.subList(2, classChain.size() - 1);
        for (String key : mapSection.getKeys(false)) {
            if (mapSection.isConfigurationSection(key)) {
                Autoloader.log("加载 Map 数据时遇到数据节: {0}", key);
                continue;
            }

            final Object mapKey = keyParser.apply(key);
            final Object mapValue = valueLoader.load(key, mapSection, cacheTypes);
            if (Objects.isNull(mapKey)) {
                Autoloader.log("加载 Map 数据时 Key 值为 null: {0}", key);
                continue;
            }
            if (Objects.isNull(mapValue)) {
                Autoloader.log("加载 Map 数据时 Value 值为 null: {0}", key);
                continue;
            }

            map.put(mapKey, mapValue);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to, Object value,
                     @NotNull List<Class<?>> classChain) {
        if (Objects.isNull(value)) {
            to.set(path, null);
            return;
        }

        ConfigurationSection mapSection = to.getConfigurationSection(path);
        if (Objects.isNull(mapSection)) {
            mapSection = to.createSection(path);
        }

        final Map<Object, Object> map = (Map<Object, Object>) value;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            mapSection.set(String.valueOf(entry.getKey()), entry.getValue());
        }
    }
}
