package eu.trixcms.trixcore.sponge.method;

import eu.trixcms.trixcore.api.method.IMethod;
import eu.trixcms.trixcore.api.method.Methods;
import eu.trixcms.trixcore.api.method.annotation.MethodName;
import eu.trixcms.trixcore.common.response.JsonResponse;
import eu.trixcms.trixcore.common.response.SuccessResponse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.whitelist.WhitelistService;

import java.util.stream.Collectors;

@MethodName(method = Methods.GET_WHITELIST)
public class GetWhitelistMethod implements IMethod {

    @Override
    public JsonResponse exec(String[] args) {
        return new SuccessResponse(Sponge.getServiceManager().provide(WhitelistService.class).get()
                .getWhitelistedProfiles()
                .stream()
                .map(GameProfile::getName)
                .collect(Collectors.toList())
        );
    }

}
