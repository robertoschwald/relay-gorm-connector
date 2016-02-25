package io.cirill.relay

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsDomainClass
import graphql.relay.Relay
import graphql.schema.DataFetcher
import graphql.schema.TypeResolver
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/16/16.
 */
public class RelayService extends AbstractRelayService {

    // injected
    GrailsApplication grailsApplication

    private Relay relay = new Relay()

    @Override
    protected Class[] getRelayDomain() {
        def domainClassesWithRelay = grailsApplication.getArtefacts('Domain')*.clazz.findAll({ it.isAnnotationPresent(RelayType) })
        domainClassesWithRelay << Pet.Species
    }

    @Override
    protected DataFetcher getNodeDataFetcher() {
        return { environment ->
            def decoded = relay.fromGlobalId(environment.arguments.id as String)

            grailsApplication.allClasses.groupBy { it.simpleName }."$decoded.type".first() findById decoded.id
        }
    }

    @Override
    protected DataFetcher getClassDataFetcher() {
        return { environment ->
            def type = grailsApplication.allClasses.find({ it.simpleName == environment.fieldType.name })
            def arg = environment.arguments.entrySet().find { entry -> entry.value != null }
            type."findBy${arg.key.capitalize()}" arg.value
        }
    }

    @Override
    protected TypeResolver getTypeResolver() {
        return { object ->
            schemaProvider.knownTypes.find { type -> type.name == object.getClass().simpleName }
        }
    }
}
