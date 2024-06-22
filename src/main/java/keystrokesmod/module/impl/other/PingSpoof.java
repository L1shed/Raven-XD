package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PingSpoof extends Module {
    private final SliderSetting latency;
    private final ButtonSetting cancelPacket;
    private final ButtonSetting sendAllOnDisabled;

    public PingSpoof() {
        super("PingSpoof", category.other);
        this.registerSetting(latency = new SliderSetting("Latency", 100, 0, 1000, 10, "ms"));
        this.registerSetting(cancelPacket = new ButtonSetting("Cancel packet", false));
        this.registerSetting(sendAllOnDisabled = new ButtonSetting("Send all on disabled", false));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.isCanceled()) return;

        final Packet<?> packet = event.getPacket();
        if (packet instanceof C0FPacketConfirmTransaction || packet instanceof C00PacketKeepAlive) {
            event.setCanceled(true);
            if (!cancelPacket.isToggled()) {
                Raven.getExecutor().schedule(() -> {
                    if (!cancelPacket.isToggled() && (sendAllOnDisabled.isToggled() || ModuleManager.pingSpoof.isEnabled())) {
                        PacketUtils.sendPacketNoEvent(packet);
                    }
                }, (long) latency.getInput(), TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public String getInfo() {
        return latency.getInput() + "ms";
    }
}
