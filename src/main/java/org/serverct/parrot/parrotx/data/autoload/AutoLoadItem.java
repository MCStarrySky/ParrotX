package org.serverct.parrot.parrotx.data.autoload;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class AutoLoadItem {
    private final String path;
    private final DataType type;
    private final String field;

    public enum DataType {
        STRING("字符串"),
        INT("整数"),
        DOUBLE("小数(Double)"),
        LONG("长整数"),
        BOOLEAN("布尔值"),
        LIST("列表"),
        LIST_MAP("Map 列表"),
        LIST_STRING("字符串列表"),
        MAP_STRING_STRING("哈希表(String)"),
        MAP_STRING_INTEGER("哈希表(Int)"),
        SOUND("音效(Sound)枚举"),
        ITEMSTACK("物品堆(ItemStack)"),
        LOCATION("坐标"),
        COLOR("颜色"),
        SERIALIZABLE("可序列化对象"),
        UNKNOWN("未知类型");

        @Getter
        public final String name;

        DataType(String name) {
            this.name = name;
        }
    }
}
