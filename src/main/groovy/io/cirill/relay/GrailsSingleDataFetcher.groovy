package io.cirill.relay

import grails.core.GrailsDomainClass
import graphql.relay.Relay
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.RelayArgument

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created by mcirillo on 2/25/16.
 */
public class GrailsSingleDataFetcher implements DataFetcher {

    private Relay relay = new Relay()

    private Class domain

    Map possibleArguments

    public GrailsSingleDataFetcher(Class domainClass) {
       domain = domainClass

        possibleArguments = domain.getDeclaredFields()
                .findAll { it.isAnnotationPresent(RelayArgument) }
                .collectEntries { [it.name, it] }
        possibleArguments.putAll domain.getDeclaredMethods()
                .findAll { it.isAnnotationPresent(RelayArgument) }
                .collectEntries { [it.name, it] }
    }

    def getForArgument(String name, Object value) {
        def eval = possibleArguments[name]

        if (eval) {
            if (eval instanceof Field) {
                return domain."findBy${name.capitalize()}"(value)
            } else if (eval instanceof Method) {
                return domain."$name"(value)
            }
        } else if (name == 'id') {
            return domain.findById(relay.fromGlobalId(value as String).id)
        }
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        def results = environment.arguments
                .findAll { it.value != null }
                .collect { getForArgument(it.key, it.value) }

        if (results && results.every { it == results.first() }) {
            return results.first()
        } else {
            return null
        }
    }
}
