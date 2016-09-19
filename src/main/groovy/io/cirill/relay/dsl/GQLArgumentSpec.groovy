package io.cirill.relay.dsl

import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLInputType
import io.cirill.relay.RelayHelpers

import static graphql.schema.GraphQLArgument.newArgument

public class GQLArgumentSpec {

    String name
    String description = ''
    GraphQLInputType type
    boolean nullable

    void name(String n) { name = n }
    void description(String d) { description = d }
    void type(GraphQLInputType t) { type = t }
    void type(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        type = GQLInputTypeSpec.inputType(cl)
    }
    void nullable (boolean b) { nullable = b }
    GraphQLArgument build() {
        newArgument()
                .name(name)
                .description(description)
                .type(nullable ? type : RelayHelpers.nonNull(type))
                .build()
    }

    public static GraphQLArgument argument(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=GQLArgumentSpec) Closure cl) {
        GQLArgumentSpec obj = new GQLArgumentSpec()
        Closure code = cl.rehydrate(obj, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return obj.build()
    }
}