package eu.trixcms.trixcore.sponge.method;

import eu.trixcms.trixcore.api.method.IMethod;
import eu.trixcms.trixcore.api.method.Methods;
import eu.trixcms.trixcore.api.method.annotation.ArgsPrecondition;
import eu.trixcms.trixcore.api.method.annotation.MethodName;
import eu.trixcms.trixcore.common.response.JsonResponse;
import eu.trixcms.trixcore.common.response.SuccessResponse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;

@MethodName(method = Methods.IS_BANNED)
public class IsBannedMethod implements IMethod {

    @Override
    @ArgsPrecondition(amount = 1)
    public JsonResponse exec(String[] args) {
        User user = Sponge.getServer().getPlayer(args[0]).get();
        BanService service = Sponge.getServiceManager().provide(BanService.class).get();

        return new SuccessResponse(service.isBanned(user.getProfile()));
    }
}
