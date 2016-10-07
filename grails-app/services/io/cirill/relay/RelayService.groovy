package io.cirill.relay

import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.GraphQLInterfaceType
import io.cirill.relay.annotation.RelayType

public class RelayService {

    // injected
    def grailsApplication

    protected GraphQL graphQL
    protected SchemaProvider schemaProvider

    protected Map<String, Class> domainArtefactCache

    protected Class[] getRelayDomain() {
        domainArtefactCache.values()
    }

    protected Closure nodeDataFetcher = { environment ->
        def decoded = RelayHelpers.fromGlobalId(environment.arguments.id as String)
        domainArtefactCache."$decoded.type".findById decoded.id
    }

    void resetGraphQL() {
        graphQL = null
    }

    public GraphQLInterfaceType nodeInterface() {
        schemaProvider.nodeInterface
    }

    public Map query(String query, Object context, Map variables) {
        if (graphQL == null) {
            domainArtefactCache = grailsApplication.getArtefacts('Domain')*.clazz
                    .findAll({ it.isAnnotationPresent(RelayType) })
                    .collectEntries { [it.simpleName, it] }

            schemaProvider = new SchemaProvider(nodeDataFetcher as DataFetcher , getRelayDomain())
            graphQL = new GraphQL(schemaProvider.schema)
        }
        def result = graphQL.execute(query, context, variables)
        def ret = [:]
        if (result.data) {
            ret.data = result.data
        }
        if (result.errors) {
            ret.errors = result.errors
        }
        return ret
    }

    public Map introspect() {
        query RelayHelpers.INTROSPECTION_QUERY, null, [:]
    }
}
