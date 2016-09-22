package io.cirill.relay.dsl

import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputType

import static graphql.schema.GraphQLInputObjectField.newInputObjectField

/**
 * relay-gorm-connector
 * @author mcirillo
 */
public class GQLInputObjectFieldSpec {

    private String name
    private String description = ''
    private GraphQLInputType type

    void name(String n) { name = n }
    void description(String d) { description = d }
    void type(GraphQLInputType t ) { type = t }
    void type(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputTypeSpec) Closure cl) {
        type = GQLInputTypeSpec.inputType cl
    }

    GraphQLInputObjectField build() {
        newInputObjectField().name(name).description(description).type(type).build()
    }

    public static GraphQLInputObjectField field(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputObjectFieldSpec) Closure cl) {
        GQLInputObjectFieldSpec gms = new GQLInputObjectFieldSpec()
        Closure code = cl.rehydrate(gms, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return gms.build()
    }
}
