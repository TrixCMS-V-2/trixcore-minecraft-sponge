package eu.trixcms.trixcore.sponge.method;

import eu.trixcms.trixcore.api.container.ServerCapacityContainer;
import eu.trixcms.trixcore.api.container.ServerInfoContainer;
import eu.trixcms.trixcore.api.method.IMethod;
import eu.trixcms.trixcore.api.method.Methods;
import eu.trixcms.trixcore.api.method.annotation.MethodName;
import eu.trixcms.trixcore.common.response.JsonResponse;
import eu.trixcms.trixcore.common.response.SuccessResponse;
import org.spongepowered.api.Sponge;

import java.net.InetAddress;

@MethodName(method = Methods.GET_SERVER_INFO)
public class GetServerInfoMethod implements IMethod {

    @Override
    public JsonResponse exec(String[] args) {
        InetAddress ip = Sponge.getServer().getBoundAddress().get().getAddress();

        String completeIP = ip.getHostAddress() + ":" + Sponge.getServer().getBoundAddress().get().getPort();

        return new SuccessResponse(
                new ServerInfoContainer(
                        Sponge.getServer().getDefaultWorldName().toString(),
                        completeIP,
                        Sponge.getServer().getMotd().toPlain(),
                        Sponge.getPlatform().getMinecraftVersion().getName(),
                        new ServerCapacityContainer(getPlayerNumber(),
                                Sponge.getGame().getServer().getMaxPlayers())));
    }

    private int getPlayerNumber() {
        return Sponge.getServer().getOnlinePlayers().size();
    }
}
