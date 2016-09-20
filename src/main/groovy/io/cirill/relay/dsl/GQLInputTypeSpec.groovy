package io.cirill.relay.dsl

import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLTypeReference
import io.cirill.relay.RelayHelpers

public class GQLInputTypeSpec {

    private GraphQLTypeReference ref
    private GraphQLList list
    private GraphQLInputType nonNull

    void ref(String n) { ref = new GraphQLTypeReference(n) }
    void list(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        list = new GraphQLList(inputType(cl))
    }
    void nonNull(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        nonNull = RelayHelpers.nonNull(inputType(cl))
    }
    void nonNull(GraphQLInputType t) {
        nonNull = RelayHelpers.nonNull(t)
    }
    void list(GraphQLOutputType t) { list = new GraphQLList(t) }

    GraphQLInputType build() {
        if (nonNull) {
            nonNull
        } else if (list) {
            list
        } else {
            throw new Error("Must specify one type")
        }
    }

    public static GraphQLInputType inputType(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        GQLInputTypeSpec spec = new GQLInputTypeSpec()
        Closure code = cl.rehydrate(spec, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return spec.build()
    }
}