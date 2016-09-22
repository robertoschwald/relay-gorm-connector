package io.cirill.relay.dsl

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import io.cirill.relay.RelayHelpers

/**
 * relay-gorm-connector
 * @author mcirillo
 */
public class GQLConnectionTypeSpec {

    private String name
    private GraphQLOutputType edgeType
    private List<GraphQLFieldDefinition> edgeFields = []
    private List<GraphQLFieldDefinition> connectionFields = []

    void name(String n) { name = n }
    void edgeType(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        edgeType = GQLOutputTypeSpec.type(cl)
    }
    void edgeField(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLFieldSpec) Closure cl) {
        edgeFields.add GQLFieldSpec.field(cl)
    }
    void connectionField(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLFieldSpec) Closure cl) {
        connectionFields.add GQLFieldSpec.field(cl)
    }

    GraphQLObjectType build() {
        def edge = RelayHelpers.edgeType(name, edgeType, edgeFields)
        RelayHelpers.connectionType(name, edge, connectionFields)
    }

    public static GraphQLObjectType connectionType(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLConnectionTypeSpec) Closure cl) {
        def conn = new GQLConnectionTypeSpec()
        def code = cl.rehydrate(conn, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return conn.build()
    }
}
