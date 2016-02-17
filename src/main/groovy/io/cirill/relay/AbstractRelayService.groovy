package io.cirill.relay

import graphql.GraphQL
import graphql.schema.DataFetcher

/**
 * Created by mcirillo on 2/16/16.
 */
public abstract class AbstractRelayService {

    private GraphQL graphQL

    def final public query(String query) {
        if (graphQL == null) {
            graphQL = new GraphQL(new SchemaProvider(getNodeDataFetcher(), getRelayDomain()).schema)
        }
        graphQL.execute(query)
    }

    protected abstract Class[] getRelayDomain()
    protected abstract DataFetcher getNodeDataFetcher()

}
