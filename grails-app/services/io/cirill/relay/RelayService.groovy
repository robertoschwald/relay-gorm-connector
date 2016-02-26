package io.cirill.relay

import grails.core.GrailsApplication
import grails.core.GrailsDomainClass
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.relay.Relay
import graphql.schema.DataFetcher
import graphql.schema.TypeResolver
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/16/16.
 */
public class RelayService {

    // injected
    GrailsApplication grailsApplication

    protected Relay relay = new Relay()
    protected GraphQL graphQL
    protected SchemaProvider schemaProvider

    protected Map<String, Class> domainArtefactCache

    protected Class[] getRelayDomain() {
        domainArtefactCache = grailsApplication.getArtefacts('Domain')*.clazz
                .findAll({ it.isAnnotationPresent(RelayType) })
                .collectEntries { [it.simpleName, it] }
        return domainArtefactCache.values()
    }

    protected Closure nodeDataFetcher = { environment ->
        def decoded = relay.fromGlobalId(environment.arguments.id as String)
        domainArtefactCache."$decoded.type".findById decoded.id
    }

    protected Closure<DataFetcher> selectDataFetcher = { clazz -> new GrailsSingleDataFetcher(clazz as Class) }

    public ExecutionResult query(String query) {
        if (graphQL == null) {
            schemaProvider = new SchemaProvider(nodeDataFetcher as DataFetcher, selectDataFetcher , getRelayDomain())
            graphQL = new GraphQL(schemaProvider.schema)
        }
        graphQL.execute(query)
    }
}
