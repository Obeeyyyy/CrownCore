package de.obey.crown.core.util;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import java.util.List;

public class TextDisplayBuilder {

    @Getter
    private TextDisplay textDisplay;

    private String identifier = "";

    @Getter
    private Location location;

    public TextDisplayBuilder(final Location location) {
        this.location = location;
        textDisplay = location.getWorld().spawn(location, TextDisplay.class);
    }

    public TextDisplayBuilder(final Location location, final String identifier) {
        this.identifier = identifier;
        this.location = location;

        textDisplay = location.getWorld().spawn(location, TextDisplay.class);

        if(!identifier.isBlank()) {
            textDisplay.setCustomName(identifier);
        }
    }

    public void teleport(final Location location) {
        this.location = location.clone();
        textDisplay.teleport(location.clone());
    }

    public TextDisplayBuilder setCustomName(final String customName) {
        textDisplay.setCustomName(customName);
        return this;
    }

    public void delete() {
        textDisplay.remove();
    }


    public TextDisplayBuilder setLocation(final Location location) {
        this.location = location.clone();
        return this;
    }

    public TextDisplayBuilder setText(final Component component) {
        textDisplay.text(component);
        return this;
    }

    public TextDisplayBuilder setText(final List<String> lines) {

        String text = "";

        for (String line : lines) {
            text = text + line + "\n";
        }

        textDisplay.text(TextUtil.translateComponent(text));
        return this;
    }

    public TextDisplayBuilder setBillboard(final Display.Billboard type){
        textDisplay.setBillboard(type);
        return this;
    }

    public TextDisplayBuilder setBackgroundColor(final Color color) {
        textDisplay.setBackgroundColor(color);
        return this;
    }

    public TextDisplayBuilder setTextShadow(final boolean value) {
        textDisplay.setShadowed(value);
        return this;
    }

    public TextDisplayBuilder setTextFromList(final List<String> list) {
        final String joined = String.join("\n", list);

        textDisplay.text(Component.text(joined));

        return this;
    }

}
