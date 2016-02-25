package io.cirill.relay

import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.TypeResolver
/**
 * Created by mcirillo on 2/16/16.
 */
public abstract class AbstractRelayService {

    private GraphQL graphQL

    protected SchemaProvider schemaProvider

    def final public ExecutionResult query(String query) {
        if (graphQL == null) {
            schemaProvider = new SchemaProvider(getNodeDataFetcher(), getClassDataFetcher(), getRelayDomain())
            graphQL = new GraphQL(schemaProvider.schema)
            schemaProvider.setTypeResolver(getTypeResolver())
        }
        graphQL.execute(query)
    }

    protected abstract Class[] getRelayDomain()
    protected abstract DataFetcher getNodeDataFetcher()
    protected abstract DataFetcher getClassDataFetcher()
    protected abstract TypeResolver getTypeResolver()

}
