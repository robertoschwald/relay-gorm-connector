package io.cirill.relay

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.RelayArgument

import java.lang.reflect.Method
import java.lang.reflect.Parameter

public class DefaultSingleDataFetcher implements DataFetcher {

    private Class domain
    private List<Parameter> params
    private String name

    public DefaultSingleDataFetcher(Class domainClass, Method method) {
        domain = domainClass
        params = method.parameters
        name = method.name
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        def args = params.collect { environment.arguments.get(it.getAnnotation(RelayArgument).name()).asType(it.type) }
        return domain.invokeMethod(name, args as Object[])
    }
}
