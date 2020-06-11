package eu.trixcms.trixcore.sponge.method;

import eu.trixcms.trixcore.api.container.PlayerContainer;
import eu.trixcms.trixcore.api.container.PlayersContainer;
import eu.trixcms.trixcore.api.method.IMethod;
import eu.trixcms.trixcore.api.method.Methods;
import eu.trixcms.trixcore.api.method.annotation.MethodName;
import eu.trixcms.trixcore.common.response.JsonResponse;
import eu.trixcms.trixcore.common.response.SuccessResponse;
import org.spongepowered.api.Sponge;

import java.util.stream.Collectors;

@MethodName(method = Methods.GET_PLAYER_LIST)
public class GetPlayerListMethod implements IMethod {

    @Override
    public JsonResponse exec(String[] args) {
        return new SuccessResponse(new PlayersContainer(
                Sponge.getServer().getOnlinePlayers().stream()
                        .map(p -> new PlayerContainer(p.getName(), p.getUniqueId()))
                        .collect(Collectors.toList())
        )
        );
    }
}
