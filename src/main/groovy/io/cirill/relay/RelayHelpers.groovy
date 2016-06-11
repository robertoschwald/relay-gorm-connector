package io.cirill.relay

import graphql.relay.Relay
import graphql.relay.Relay.ResolvedGlobalId
import graphql.schema.*
import groovy.transform.CompileStatic

import static graphql.schema.GraphQLArgument.newArgument

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

    public static GraphQLArgument makeArgument(String name, GraphQLInputType type, String description, boolean isNullable) {
        newArgument().name(name).type(isNullable ? type : nonNull(type)).description(description).build()
    }

    public static GraphQLNonNull nonNull(GraphQLInputType obj) {
        new GraphQLNonNull(obj)
    }

    public static GraphQLFieldDefinition makeMutation(String name, String fieldname, List<GraphQLInputObjectField> inputFields, List<GraphQLFieldDefinition> outputFields, DataFetcher datafetcher) {
        relay.mutationWithClientMutationId(name, fieldname, inputFields, outputFields, datafetcher)
    }
}
