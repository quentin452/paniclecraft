package com.vanym.paniclecraft.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;

public abstract class CommandBase extends net.minecraft.command.CommandBase {
    
    protected String[] path;
    
    protected String getPath() {
        if (this.path != null) {
            return "/" + String.join(" ", this.path);
        } else {
            return this.getCommandName();
        }
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return this.getTranslationPrefix() + ".usage";
    }
    
    protected String getTranslationPrefix() {
        List<String> path = new ArrayList<>();
        path.add("commands");
        if (this.path != null) {
            path.addAll(Arrays.asList(this.path));
        } else {
            path.add(this.getCommandName());
        }
        return String.join(".", path);
    }
}
