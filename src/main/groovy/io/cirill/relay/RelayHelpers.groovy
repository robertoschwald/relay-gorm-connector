package io.cirill.relay

import graphql.relay.Relay
import graphql.relay.Relay.ResolvedGlobalId
import graphql.schema.DataFetcher
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLNonNull
import graphql.schema.TypeResolver
import groovy.transform.CompileStatic

import static graphql.schema.GraphQLArgument.newArgument

/**
 * Created by mcirillo on 3/1/16.
 */
@CompileStatic
class RelayHelpers {

    private static Relay relay = new Relay()

    public static final String DESCRIPTION_ID_ARGUMENT = 'The ID of an object'

    public static String toGlobalId(String type, String id) {
        relay.toGlobalId(type, id)
    }

    public static ResolvedGlobalId fromGlobalId(String id) {
        relay.fromGlobalId(id)
    }

    public static GraphQLInterfaceType nodeInterface(TypeResolver typeResolver) {
        relay.nodeInterface(typeResolver)
    }

    public static GraphQLFieldDefinition nodeField(GraphQLInterfaceType interfaceType, DataFetcher dataFetcher) {
        relay.nodeField(interfaceType, dataFetcher)
    }

    public static GraphQLArgument makeArgument(String name, GraphQLInputType type, String description, boolean isNullable, boolean unique) {
        def arg = newArgument().name(name).type(isNullable ? type : nonNull(type)).description(description).build()
        arg.metaClass['unique'] = unique
        return arg
    }

    public static GraphQLNonNull nonNull(GraphQLInputType obj) {
        new GraphQLNonNull(obj)
    }

}