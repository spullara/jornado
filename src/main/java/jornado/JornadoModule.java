package jornado;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

import java.util.Collections;

/**
 * A guice module that contains the configuration for jornado
 */
public abstract class JornadoModule<R extends Request> extends AbstractModule {
    private final Config config;
    private final Iterable<RouteHandler<R>> routes;

    protected JornadoModule(final Config config, Iterable<RouteHandler<R>> routes) {
        this.config = config;
        this.routes = routes;
    }

    /**
     * @deprecated use version where you pass in routes instead
     */
    @Deprecated
    protected JornadoModule(final Config config) {
        this.config = config;
        routes = createRoutes();
    }

    /**
     * @deprecated pass in on constructor instead
     * @return routes
     */
    @Deprecated
    protected Iterable<RouteHandler<R>> createRoutes() {
      return Collections.emptyList();
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
        bindIterable("routes", routes);

        // inject-enable the handlers
        Matchers.subclassesOf(Handler.class); 

        // set up the timing intercepters
        final TimingInterceptor timingInterceptor = new TimingInterceptor();
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), timingInterceptor);
        bindInterceptor(Matchers.annotatedWith(Timed.class), Matchers.any(), timingInterceptor);
        bindInterceptor(Matchers.subclassesOf(Handler.class), Matchers.any(), timingInterceptor);
    }

    protected void bindIterable(String name, Iterable value) {
        bind(TypeLiteral.get(Iterable.class)).annotatedWith(Names.named(name)).toInstance(value);
    }
}