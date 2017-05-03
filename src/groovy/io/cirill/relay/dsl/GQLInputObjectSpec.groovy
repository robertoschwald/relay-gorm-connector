package io.cirill.relay.dsl

import graphql.Scalars
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType

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
        graphql.schema.GraphQLInputObjectType.newInputObject()
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
        def owner = Closure.isAssignableFrom(cl.owner.class) ? cl.owner.owner : cl.owner
        Closure code = cl.rehydrate(ios, owner, cl.thisObject)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return ios.build()
    }

}
