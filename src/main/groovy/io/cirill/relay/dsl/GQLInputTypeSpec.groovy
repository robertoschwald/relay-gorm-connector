package io.cirill.relay.dsl

import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLTypeReference

public class GQLInputTypeSpec {

    GraphQLTypeReference ref
    GraphQLList list

    void ref(String n) { ref = new GraphQLTypeReference(n) }
    void list(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        list = new GraphQLList(inputType(cl))
    }
    void list(GraphQLOutputType t) { list = new GraphQLList(t) }

    GraphQLInputType build() {
//            if (ref) {
//                ref
//            } else if (list) {
//                list
//            }
        list
    }

    public static GraphQLInputType inputType(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        GQLInputTypeSpec spec = new GQLInputTypeSpec()
        Closure code = cl.rehydrate(spec, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return spec.build()
    }
}