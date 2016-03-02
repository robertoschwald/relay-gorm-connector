package io.cirill.relay

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.RelayArgument

/**
 * Created by mcirillo on 2/25/16.
 */
public class DefaultPluralDataFetcher implements DataFetcher {

    private Class domain
    private Map argumentIsMethod

    public DefaultPluralDataFetcher(Class domainClass, List<String> argNames) {
       domain = domainClass

        argumentIsMethod = domain.getDeclaredFields()
                .findAll { argNames.contains(it.name) }
                .collectEntries { [it.name, false] }
        argumentIsMethod.putAll domain.getDeclaredMethods()
                .findAll { argNames.contains(it.name) }
                .collectEntries { [it.name, true] }
    }

    def getForArgument(String name, Object value) {
        if (argumentIsMethod.keySet().contains(name)) {
            if (argumentIsMethod[name]) {
                return domain."$name"(value)
            } else {
                return domain."findBy${name.capitalize()}"(value)
            }
        } else if (name == 'id') {
            return domain.findById(RelayHelpers.fromGlobalId(value as String).id)
        }
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        def results = environment.arguments
                .find { it.value != null }
                .collect { args ->
                    args.value.asType(List).collect { arg ->
                        getForArgument(args.key, arg)
                    }}
                .flatten()

        return results
    }
}
