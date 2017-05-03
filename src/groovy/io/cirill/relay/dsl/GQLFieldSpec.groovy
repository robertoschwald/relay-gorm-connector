package io.cirill.relay.dsl

import graphql.schema.DataFetcher
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLOutputType

public class GQLFieldSpec {

    protected String name
    protected String description = ''
    protected List<GraphQLArgument> args = []
    protected DataFetcher df
    protected GraphQLOutputType type

    void name(String n) { name = n }
    void description(String d) { description = d }

    void argument(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=GQLArgumentSpec)Closure cl) { args.add GQLArgumentSpec.argument(cl) }
    void arguments(Collection<GraphQLArgument> arg) { args.addAll(arg) }
    void dataFetcher(DataFetcher d) { df = d }
    void type(GraphQLOutputType t) { type = t }
    void type(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        type = GQLOutputTypeSpec.type(cl)
    }

    GraphQLFieldDefinition build() {
        graphql.schema.GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .description(description)
                .type(type)
                .dataFetcher(df)
                .argument(args)
                .fetchField()
                .build()
    }

    public static GraphQLFieldDefinition field(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=GQLFieldSpec) Closure cl) {
        GQLFieldSpec obj = new GQLFieldSpec()
        def owner = Closure.isAssignableFrom(cl.owner.class) ? cl.owner.owner : cl.owner
        Closure code = cl.rehydrate(obj, owner, cl.thisObject)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return obj.build()
    }

}