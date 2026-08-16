package ch.Restart.Main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class Messages {

    private Messages() {
    }

    public static Component noPermission() {
        return Component.text("No permission!", NamedTextColor.RED);
    }

    public static Component usage() {
        return Component.text("Usage: /srestart <seconds>", NamedTextColor.RED);
    }

    public static Component example() {
        return Component.text("Example: /srestart 60", NamedTextColor.GRAY);
    }

    public static Component invalidNumber() {
        return Component.text("Please enter a valid number!", NamedTextColor.RED);
    }

    public static Component minimumTime() {
        return Component.text("Minimum: 5 seconds!", NamedTextColor.RED);
    }

    public static Component maximumTime() {
        return Component.text("Maximum: 600 seconds (10 minutes)!", NamedTextColor.RED);
    }

    public static Component countdownStarted(int seconds) {
        return Component.text("Restart countdown started: ", NamedTextColor.GREEN)
                .append(Component.text(seconds + " seconds", NamedTextColor.YELLOW));
    }

    public static Component countdownCancelled() {
        return Component.text("Restart countdown cancelled!", NamedTextColor.GREEN);
    }

    public static Component noActiveCountdown() {
        return Component.text("No active countdown!", NamedTextColor.RED);
    }

    public static Component cancelledTitle() {
        return Component.text("✓ RESTART CANCELLED", NamedTextColor.GREEN, TextDecoration.BOLD);
    }

    public static Component cancelledSubtitle() {
        return Component.text("The server restart has been stopped", NamedTextColor.WHITE);
    }

    public static Component restartTitle() {
        return Component.text("SERVER RESTART", NamedTextColor.DARK_RED, TextDecoration.BOLD);
    }

    public static Component restartSubtitle() {
        return Component.text("The server is restarting...", NamedTextColor.RED);
    }

    public static Component bossBarTitle(int seconds) {
        return Component.text("⚠ SERVER RESTART IN " + seconds + "s ⚠", NamedTextColor.RED, TextDecoration.BOLD);
    }

    public static Component actionBar(int seconds) {
        return Component.text("⚠ RESTART IN " + seconds + "s ⚠", NamedTextColor.RED, TextDecoration.BOLD);
    }

    public static Component restartAnnounceTitle(TextColor color) {
        return Component.text("⚠ SERVER RESTART ⚠", color, TextDecoration.BOLD);
    }

    public static Component restartAnnounceSubtitle(int seconds) {
        String timeString;
        if (seconds >= 60) {
            int minutes = seconds / 60;
            timeString = minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            timeString = seconds + " second" + (seconds > 1 ? "s" : "");
        }

        TextColor color = colorForSeconds(seconds);
        return Component.text("The server will restart in ", NamedTextColor.WHITE)
                .append(Component.text(timeString, color, TextDecoration.BOLD))
                .append(Component.text("!", NamedTextColor.WHITE));
    }

    public static TextColor colorForSeconds(int seconds) {
        if (seconds <= 10) {
            return NamedTextColor.DARK_RED;
        }
        if (seconds <= 30) {
            return NamedTextColor.RED;
        }
        return NamedTextColor.GOLD;
    }
}
