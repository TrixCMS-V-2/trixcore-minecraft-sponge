package eu.trixcms.trixcore.sponge.method;

import eu.trixcms.trixcore.api.method.IMethod;
import eu.trixcms.trixcore.api.method.Methods;
import eu.trixcms.trixcore.api.method.annotation.ArgsPrecondition;
import eu.trixcms.trixcore.api.method.annotation.MethodName;
import eu.trixcms.trixcore.common.response.JsonResponse;
import eu.trixcms.trixcore.common.response.SuccessResponse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

@MethodName(method = Methods.IS_CONNECTED)
public class IsConnectedMethod implements IMethod {

    @Override
    @ArgsPrecondition(amount = 1)
    public JsonResponse exec(String[] args) {
        return new SuccessResponse(Sponge.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .anyMatch(name -> name.equals(args[0]))
        );
    }
}
