package eu.trixcms.trixcore.sponge;

import com.google.inject.Inject;
import eu.trixcms.trixcore.api.command.ICommandExecutor;
import eu.trixcms.trixcore.api.config.IConfig;
import eu.trixcms.trixcore.api.config.exception.InvalidConfigException;
import eu.trixcms.trixcore.api.container.CommandContainer;
import eu.trixcms.trixcore.api.i18n.Lang;
import eu.trixcms.trixcore.api.method.exception.DuplicateMethodNameException;
import eu.trixcms.trixcore.api.method.exception.InvalidMethodDefinitionException;
import eu.trixcms.trixcore.api.server.exception.InvalidPortException;
import eu.trixcms.trixcore.api.util.ServerTypeEnum;
import eu.trixcms.trixcore.common.CommandManager;
import eu.trixcms.trixcore.common.SchedulerManager;
import eu.trixcms.trixcore.common.TrixServer;
import eu.trixcms.trixcore.common.i18n.JsonMessageSource;
import eu.trixcms.trixcore.common.i18n.Translator;
import eu.trixcms.trixcore.sponge.method.*;
import lombok.Getter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Plugin(id = "trixcore", name = "TrixCore", version = "1.0.3", url = "https://trixcms.eu", authors = {"antoineok", "iambluedev"})
public class TrixCore implements IConfig, ICommandExecutor<CommandContainer> {

    private static final Logger logger = LoggerFactory.getLogger(TrixCore.class);

    @Getter private static TrixCore instance;
    @Getter private TrixServer trixServer;
    @Getter private Translator translator;
    @Getter private SchedulerManager schedulerManager;
    @Getter private CommandManager commandManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File workingDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    ConfigurationLoader<CommentedConfigurationNode> loader;

    private Game game;

    @Inject
    public TrixCore(Game game) {
        this.game = game;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent e) {
        Config.getInstance().setup(new File(workingDir, "config.conf"), loader);
        Config.getInstance().loadConfig();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        translator = new Translator(JsonMessageSource.class, Lang.values());

        if (Sponge.getPluginManager().isLoaded("azlink")) {
            throw new RuntimeException(translator.of("PLUGIN_AZLINK"));
        }

        //fix for https://github.com/Mojang/LegacyLauncher/blob/master/src/main/java/net/minecraft/launchwrapper/LaunchClassLoader.java#L58
        ClassLoader loader = TrixCore.class.getClassLoader();
        try {
            loader.loadClass("javax/servlet/ServletContext");
        } catch (final ClassNotFoundException ignored) {
        }

        schedulerManager = new SchedulerManager(translator);
        commandManager = new CommandManager(this,
                translator,
                schedulerManager,
                new File(workingDir, "commands.json"));
        trixServer = new TrixServer();

        try {
            trixServer
                    .translator(translator)
                    .scheduler(schedulerManager)
                    .commandManager(commandManager)
                    .serverType(ServerTypeEnum.SPONGE)
                    .registerMethods(
                            new GetBannedPlayersMethod(),
                            new GetPlayerListMethod(),
                            new GetServerInfoMethod(),
                            new GetWhitelistMethod(),
                            new IsBannedMethod(),
                            new IsConnectedMethod(),
                            new RemoveScheduledCommandsMethod(),
                            new RunCommandMethod(),
                            new RunScheduledCommandMethod(),
                            new SetMOTDMethod()
                    );
        } catch (DuplicateMethodNameException | InvalidMethodDefinitionException e) {
            logger.error(translator.of("ERROR"), e);
        }

        try {
            trixServer.config(this);

            logger.info(translator.of("STARTING_SERVER"));
            trixServer.start();
        } catch (InvalidPortException e) {
            logger.error(translator.of("PORT_HELP"));
            logger.error(translator.of("ERROR"), e);
        } catch (IOException e) {
            logger.error(translator.of("ERROR"), e);
        } catch (InvalidConfigException e) {
            logger.error(translator.of("UNKNOWN_SAVER"), e);
        }

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("TrixCore main command"))
                .permission("trixcore.command.trixcore")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arg"))),
                        GenericArguments.optional(GenericArguments.integer(Text.of("port")))
                )
                .executor(new TrixCommand(this, translator))
                .build(), "trixcore");
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        logger.info(translator.of("STOPPING_SERVER"));
        trixServer.stop();
        Config.getInstance().saveConfig();
    }

    @Inject
    public void setConfiguration(@ConfigDir(sharedRoot = true) Path configPath) {
        this.loader = HoconConfigurationLoader.builder().setPath(configPath.resolve("trixcore/config.conf")).build();
    }

    @Override
    public boolean executeCommand(CommandContainer commandContainer) {
        logger.debug(translator.of("HTTP_RUNNING_COMMAND", commandContainer.getCmd()));
        CommandResult result = game.getCommandManager().process(Sponge.getServer().getConsole(), commandContainer.getCmd());
        return result.getSuccessCount().isPresent();
    }

    @Override
    public String getSecretKey() {
        String key = Config.getInstance().getConfig().getNode("secret_key").getString();
        return (key == null || key.isEmpty()) ? "" : key;
    }

    @Override
    public Integer getServerPort() {
        Integer port = Config.getInstance().getConfig().getNode("port").getInt();
        return (port == null) ? 0 : port;
    }

    @Override
    public void saveSecretKey(String key) throws IOException {
        logger.info(translator.of("SAVER_SAVING_SECRET_KEY"));
        Config.getInstance().getConfig().getNode("secret_key").setValue(key);
        Config.getInstance().saveConfig();
    }

    @Override
    public void saveServerPort(Integer port) throws IOException {
        logger.info(translator.of("SAVER_SAVING_SERVER_PORT"));
        Config.getInstance().getConfig().getNode("port").setValue(port);
        Config.getInstance().saveConfig();
    }

    public void saveMotd(String motd) throws IOException {
        Config.getInstance().getConfig().getNode("custom_motd").setValue(motd);
        Config.getInstance().saveConfig();
    }

    @Listener
    public void onPing(ClientPingServerEvent e) {
        if (!Objects.requireNonNull(Config.getInstance().getConfig().getNode("custom_motd").getString()).isEmpty()) {
            e.getResponse().setDescription(Text.of(Objects.requireNonNull(Config.getInstance().getConfig().getNode("custom_motd").getString())));
        }
    }
}
