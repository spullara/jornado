package jornado;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A guice module that contains the configuration for jornado
 */
public abstract class JornadoModule<R extends Request> extends AbstractModule {
  protected final Config config;
  private final Iterable<RouteHandler<R>> routes;

  protected JornadoModule(final Config config, Iterable<RouteHandler<R>> routes) {
    this.config = config;
    this.routes = routes;
  }

  // TODO: I was thinking this would be useful for something... not sure what.
  protected Class getRequestClass() {
    return Request.class;
  }

  protected JornadoModule(final Config config) {
    this.config = config;
    routes = createRoutes();
  }

  protected Iterable<RouteHandler<R>> createRoutes() {
    return Collections.emptyList();
  }

  public Iterable<RouteHandler<R>> getRoutes() {
    return routes;
  }

  @Override
  protected void configure() {    
    bind(Config.class).toInstance(config);
    bindIterable("routes", routes);

    Set<Class> bound = new HashSet<Class>();
    for (RouteHandler<R> route : routes) {
      Class<? extends Handler<R>> handlerClass = route.getHandlerClass();
      if (bound.add(handlerClass)) bind(handlerClass);
    }

    bindLiteral("cookieKey", config.getCookieKey());
    bindLiteral("loginUrl", config.getLoginUrl());

    bind(TypeLiteral.get(List.class)).annotatedWith(Names.named("filters")).toInstance(filters());

    // inject-enable the handlers
    Matchers.subclassesOf(Handler.class);
  }

  protected void bindLiteral(String annotationName, String value) {
    bind(TypeLiteral.get(String.class)).annotatedWith(Names.named(annotationName)).toInstance(value);
  }

  protected void bindIterable(String name, Iterable<?> value) {
    bind(TypeLiteral.get(Iterable.class)).annotatedWith(Names.named(name)).toInstance(value);
  }
  
  protected List<? extends Class> filters() {
    return Lists.newArrayList(AuthFilter.class);
  }
}