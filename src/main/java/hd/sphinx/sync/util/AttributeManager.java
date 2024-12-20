package hd.sphinx.sync.util;

import hd.sphinx.sync.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public class AttributeManager {
    public static String saveAttributes(Attributable entity) {
        StringBuilder result = new StringBuilder();
        for (Attribute attr : Attribute.values()) {
            AttributeInstance instance = entity.getAttribute(attr);
            if (instance != null) {
                result.append(attr.getKey().getNamespace());
                result.append(",");
                result.append(attr.getKey().getKey());
                result.append(",");
                result.append(instance.getBaseValue());
                for (AttributeModifier modifier : instance.getModifiers()) {
                    result.append(",");
                    result.append(modifier.getKey().getNamespace());
                    result.append(",");
                    result.append(modifier.getKey().getKey());
                    result.append(",");
                    result.append(modifier.getAmount());
                    result.append(",");
                    result.append(modifier.getOperation().name());
                }
                result.append(";");
            }
        }
        return result.substring(0, result.length() - 1);
    }

    public static void loadAttributes(Attributable entity, String attributes) {
        for (String attrData : attributes.split(";")) {
            String[] values = attrData.split(",");
            Attribute attr = Registry.ATTRIBUTE.get(new NamespacedKey(values[0], values[1]));
            if (attr == null) continue;
            AttributeInstance instance = entity.getAttribute(attr);
            if (instance == null) continue;
            instance.setBaseValue(Double.parseDouble(values[2]));
            for (AttributeModifier modifier : instance.getModifiers()) {
                instance.removeModifier(modifier);
            }
            for (int i = 3; i < values.length; i += 4) {
                instance.addModifier(new AttributeModifier(
                        new NamespacedKey(values[i], values[i + 1]),
                        Double.parseDouble(values[i + 2]),
                        AttributeModifier.Operation.valueOf(values[i + 3])
                ));
            }
        }
    }
}
