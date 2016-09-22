package io.cirill.relay.dsl

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition

/**
 * relay-gorm-connector
 * @author mcirillo
 */
public class GQLMutationSpec extends GQLFieldSpec {

    private GraphQLInputObjectType inputObject

    void inputType(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLInputObjectSpec) Closure cl) {
        inputObject = GQLInputObjectSpec.inputObject cl
    }

    GraphQLFieldDefinition build() {
        newFieldDefinition()
                .name(name)
                .description(description)
                .type(type)
                .argument(GQLArgumentSpec.argument {
                    name 'input'
                    type {
                        nonNull inputObject
                    }
                })
                .dataFetcher(df)
                .build()
    }

    public static GraphQLFieldDefinition field(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLMutationSpec) Closure cl) {
        GQLMutationSpec gms = new GQLMutationSpec()
        Closure code = cl.rehydrate(gms, cl.owner, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return gms.build()
    }

}
