package io.cirill.relay.dsl

import graphql.Scalars
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType

import static graphql.schema.GraphQLInputObjectType.newInputObject

/**
 * relay-gorm-connector
 * @author mcirillo
 */
public class GQLInputObjectSpec {

    private String name
    private String description = ''
    private List<GraphQLInputObjectField> fields = []

    void name(String n) { name = n }
    void description(String d) { description = d }
    void field(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputObjectFieldSpec) Closure cl) {
        fields.add GQLInputObjectFieldSpec.field(cl)
    }

    GraphQLInputObjectType build() {
        newInputObject()
                .name(name)
                .description(description)
                .field(GQLInputObjectFieldSpec.field {
                    name 'clientMutationId'
                    type {
                        nonNull Scalars.GraphQLString
                    }
                })
                .fields(fields)
                .build()
    }

    public static GraphQLInputObjectType inputObject(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLMutationSpec) Closure cl) {
        GQLInputObjectSpec ios = new GQLInputObjectSpec()
        Closure code = cl.rehydrate(ios, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return ios.build()
    }

}
