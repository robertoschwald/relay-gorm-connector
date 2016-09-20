package io.cirill.relay.dsl

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLList
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLTypeReference

import static graphql.schema.GraphQLObjectType.newObject

public class GQLOutputTypeSpec {

    private String name
    private String description = ''
    private List<GraphQLFieldDefinition> fields = []
    private GraphQLTypeReference ref
    private GraphQLList list

    void name(String n) { name = n }
    void description(String d) { description = d }
    void field(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLFieldSpec) Closure cl) {
        fields.add GQLFieldSpec.field(cl)
    }
    void ref(String n) { ref = new GraphQLTypeReference(n) }
    void list(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        list = new GraphQLList(type(cl))
    }
    void list(GraphQLOutputType t) { list = new GraphQLList(t) }

    GraphQLOutputType build() {
        if (ref) {
            ref
        } else if (list) {
            list
        }
        else {
            newObject().name(name).description(description).fields(fields).build()
        }
    }

    public static GraphQLOutputType type(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        GQLOutputTypeSpec spec = new GQLOutputTypeSpec()
        Closure code = cl.rehydrate(spec, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return spec.build()
    }
}