package ch.Restart.Main;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class RestartCommand implements CommandExecutor, Listener {

    private static final int TICKS_PER_SECOND = 20;
    private static final long SHUTDOWN_DELAY_TICKS = 60L;
    private static final int MIN_SECONDS = 5;
    private static final int MAX_SECONDS = 600;
    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofSeconds(3),
            Duration.ofMillis(500)
    );

    private final Main plugin;
    private BukkitRunnable restartTask;
    private BossBar restartBar;

    public RestartCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "srestart" -> handleRestart(sender, args);
            case "cancelrestart" -> handleCancel(sender);
            default -> false;
        };
    }

    private boolean handleRestart(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Messages.noPermission());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Messages.usage());
            sender.sendMessage(Messages.example());
            return true;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.invalidNumber());
            return true;
        }

        if (seconds < MIN_SECONDS) {
            sender.sendMessage(Messages.minimumTime());
            return true;
        }

        if (seconds > MAX_SECONDS) {
            sender.sendMessage(Messages.maximumTime());
            return true;
        }

        stopExistingCountdown();
        broadcastCancelledTitle(false);
        startRestartCountdown(seconds);
        sender.sendMessage(Messages.countdownStarted(seconds));
        return true;
    }

    private boolean handleCancel(CommandSender sender) {
        if (!sender.isOp()) {
            sender.sendMessage(Messages.noPermission());
            return true;
        }

        if (restartTask == null) {
            sender.sendMessage(Messages.noActiveCountdown());
            return true;
        }

        stopExistingCountdown();
        broadcastCancelledTitle(true);
        sender.sendMessage(Messages.countdownCancelled());
        return true;
    }

    private void startRestartCountdown(int totalSeconds) {
        restartBar = BossBar.bossBar(
                Messages.bossBarTitle(totalSeconds),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showBossBar(restartBar);
        }

        broadcastRestartTitle(totalSeconds);

        restartTask = new BukkitRunnable() {
            int remaining = totalSeconds;

            @Override
            public void run() {
                remaining--;

                float progress = (float) remaining / totalSeconds;
                restartBar.progress(Math.max(0.0f, progress));
                restartBar.name(Messages.bossBarTitle(remaining));

                Component actionBar = Messages.actionBar(remaining);
                Title title = null;
                Sound sound = null;
                float pitch = 1.0f;

                boolean announce = remaining == 60 || remaining == 30 || remaining == 15 || remaining == 10 || remaining <= 5;
                if (announce) {
                    title = Title.title(
                            Messages.restartAnnounceTitle(Messages.colorForSeconds(remaining)),
                            Messages.restartAnnounceSubtitle(remaining),
                            TITLE_TIMES
                    );
                    if (remaining <= 5) {
                        sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                        pitch = 2.0f;
                    } else if (remaining <= 10) {
                        sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                        pitch = 1.5f;
                    } else {
                        sound = Sound.BLOCK_NOTE_BLOCK_BASS;
                        pitch = 0.8f;
                    }
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendActionBar(actionBar);
                    if (title != null) {
                        online.showTitle(title);
                        online.playSound(online.getLocation(), sound, 1.0f, pitch);
                    }
                }

                if (remaining <= 0) {
                    finishRestart();
                    cancel();
                }
            }
        };

        restartTask.runTaskTimer(plugin, TICKS_PER_SECOND, TICKS_PER_SECOND);
    }

    private void broadcastRestartTitle(int seconds) {
        Title title = Title.title(
                Messages.restartAnnounceTitle(Messages.colorForSeconds(seconds)),
                Messages.restartAnnounceSubtitle(seconds),
                TITLE_TIMES
        );
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showTitle(title);
        }
    }

    private void finishRestart() {
        hideBossBar();

        Title title = Title.title(Messages.restartTitle(), Messages.restartSubtitle(), TITLE_TIMES);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showTitle(title);
            online.playSound(online.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(plugin, SHUTDOWN_DELAY_TICKS);
    }

    private void stopExistingCountdown() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }
        hideBossBar();
    }

    private void broadcastCancelledTitle(boolean playSound) {
        Title title = Title.title(Messages.cancelledTitle(), Messages.cancelledSubtitle(), TITLE_TIMES);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showTitle(title);
            if (playSound) {
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }

    private void hideBossBar() {
        if (restartBar == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hideBossBar(restartBar);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (restartBar == null) {
            return;
        }

        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (restartBar != null && player.isOnline()) {
                player.showBossBar(restartBar);
            }
        }, 1L);
    }

    public void cleanup() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }
        hideBossBar();
        restartBar = null;
    }
}
