package eu.trixcms.trixcore.sponge;

import eu.trixcms.trixcore.api.config.exception.InvalidConfigException;
import eu.trixcms.trixcore.api.server.exception.InvalidPortException;
import eu.trixcms.trixcore.common.i18n.Translator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.io.IOException;

@RequiredArgsConstructor
public class TrixCommand implements CommandExecutor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TrixCore trixCore;
    private final Translator translator;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String arg = args.<String>getOne("arg").get();
        if (arg.equals("setup")) {
            int port = args.<Integer>getOne("port").get();

            if (trixCore.getServerPort() != 0) {
                logger.error(translator.of("CMD_NEED_RESET_BEFORE"));
                return CommandResult.success();
            }

            trixCore.getTrixServer().stop();

            try {
                trixCore.getTrixServer().setPort(port);
            } catch (InvalidPortException e) {
                logger.error(translator.of("PORT_HELP"));
                logger.error(translator.of("ERROR"), e);
                src.sendMessage(Text.of(translator.of("PORT_HELP")));
                src.sendMessage(Text.of(translator.of("ERROR") + e.getMessage()));
            } catch (IOException e) {
                logger.error(translator.of("ERROR"), e);
            }

            try {
                trixCore.getTrixServer().config(trixCore).start();
            } catch (InvalidPortException e) {
                logger.error(translator.of("PORT_HELP"));
                logger.error(translator.of("ERROR"), e);
                src.sendMessage(Text.of(translator.of("PORT_HELP")));
                src.sendMessage(Text.of(translator.of("ERROR") + e.getMessage()));
            } catch (IOException e) {
                logger.error(translator.of("ERROR"), e);
                src.sendMessage(Text.of(translator.of("ERROR") + e.getMessage()));
            } catch (InvalidConfigException e) {
                src.sendMessage(Text.of(translator.of("UNKNOWN_SAVER") + e.getMessage()));
                logger.error(translator.of("UNKNOWN_SAVER"), e);
            }

            logger.info(translator.of("CMD_PORT_SUCCESSFULLY_SETUP", port + ""));
            src.sendMessage(Text.of(translator.of("CMD_PORT_SUCCESSFULLY_SETUP", port + "")));
            return CommandResult.success();
        } else if (arg.equals("reset")) {
            if (trixCore.getServerPort() == 0)
                logger.info(translator.of("CMD_PORT_ALREADY_RESET"));

            trixCore.getTrixServer().stop();

            try {
                trixCore.getTrixServer().setSecretKey("");
                trixCore.getTrixServer().setPort(-1);
            } catch (IOException e) {
                logger.error(translator.of("ERROR"), e);
            } catch (InvalidPortException ignored) {
            }

            logger.info(translator.of("CMD_PORT_SUCCESSFULLY_RESET"));
            src.sendMessage(Text.of(translator.of("CMD_PORT_SUCCESSFULLY_RESET")));
            return CommandResult.success();
        }
        return CommandResult.empty();
    }

}
