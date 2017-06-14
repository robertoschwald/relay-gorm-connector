package io.cirill.relay.dsl

import graphql.schema.GraphQLFieldDefinition
/**
 * relay-gorm-connector
 * @author mcirillo
 */
public class GQLMutationSpec extends GQLFieldSpec {

    GraphQLFieldDefinition build() {
        graphql.schema.GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .description(description)
                .type(type)
                .argument(args)
                .dataFetcher(df)
                .build()
    }

    public static GraphQLFieldDefinition field(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLMutationSpec) Closure cl) {
        GQLMutationSpec gms = new GQLMutationSpec()
        def owner = Closure.isAssignableFrom(cl.owner.class) ? cl.owner.owner : cl.owner
        Closure code = cl.rehydrate(gms, owner, cl.thisObject)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return gms.build()
    }

}
