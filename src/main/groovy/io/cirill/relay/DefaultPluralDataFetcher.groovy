package io.cirill.relay

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.RelayArgument

import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * Created by mcirillo on 2/25/16.
 */
public class DefaultPluralDataFetcher implements DataFetcher {

    private Class domain
    private List<Parameter> params
    private String name

    public DefaultPluralDataFetcher(Class domainClass, Method method) {
        domain = domainClass
        params = method.parameters
        name = method.name
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        def totalArgs = environment.arguments.collectMany({ it.value.asType(Collection) }).size()
        if (environment.arguments.any({ totalArgs % it.value.asType(Collection).size() != 0 })) {
            throw new Exception('Plural argument lengths do not match: ' + environment.arguments)
        } else {
            (0..<(totalArgs / environment.arguments.size())).collect { i ->
                def argSet = params.collect { param ->
                    String paramName = param.getAnnotation(RelayArgument).name()
                    environment.arguments[paramName as String].asType(Collection).getAt(i as Integer).asType(param.type)
                }
                domain.invokeMethod(name, argSet as Object[])
            }
        }
    }
}
