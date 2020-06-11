package eu.trixcms.trixcore.sponge.method;

import eu.trixcms.trixcore.api.method.IMethod;
import eu.trixcms.trixcore.api.method.Methods;
import eu.trixcms.trixcore.api.method.annotation.MethodName;
import eu.trixcms.trixcore.common.response.JsonResponse;
import eu.trixcms.trixcore.common.response.SuccessResponse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ban.BanService;

import java.util.stream.Collectors;

@MethodName(method = Methods.GET_BANNED_PLAYER)
public class GetBannedPlayersMethod implements IMethod {

    @Override
    public JsonResponse exec(String[] args) {
        return new SuccessResponse(Sponge.getServiceManager()
                .provide(BanService.class)
                .get()
                .getBans()
                .stream()
                .map(ban -> ban.getType().getName())
                .collect(Collectors.toList())
        );
    }
}
