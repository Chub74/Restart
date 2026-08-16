package ch.Restart.Main;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private RestartCommand restartCommand;

    @Override
    public void onEnable() {
        restartCommand = new RestartCommand(this);
        getCommand("srestart").setExecutor(restartCommand);
        getCommand("cancelrestart").setExecutor(restartCommand);
        getServer().getPluginManager().registerEvents(restartCommand, this);
    }

    @Override
    public void onDisable() {
        if (restartCommand != null) {
            restartCommand.cleanup();
        }
    }
}
