package io.cirill.relay.dsl

import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLInputType

import static graphql.schema.GraphQLArgument.newArgument

public class GQLArgumentSpec {

    private String name
    private String description = ''
    private GraphQLInputType type

    void name(String n) { name = n }
    void description(String d) { description = d }
    void type(GraphQLInputType t) { type = t }
    void type(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        type = GQLInputTypeSpec.inputType(cl)
    }
    GraphQLArgument build() {
        newArgument()
                .name(name)
                .description(description)
                .type(type)
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