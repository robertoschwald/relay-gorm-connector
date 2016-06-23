package io.cirill.relay

import graphql.Scalars
import graphql.relay.Relay
import graphql.relay.Relay.ResolvedGlobalId
import graphql.schema.*
import groovy.transform.CompileStatic

import java.lang.reflect.Type

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

    public static GraphQLInputType parseGraphQLInputType(Type javaType, Closure<GraphQLInputType> other) {
        GraphQLInputType type
	    switch (javaType) {
		    case int:
		    case Integer:
			    type = Scalars.GraphQLInt
			    break

		    case String:
			    type = Scalars.GraphQLString
			    break

		    case boolean:
		    case Boolean:
			    type = Scalars.GraphQLBoolean
			    break

		    case float:
		    case Float:
			    type = Scalars.GraphQLFloat
			    break

		    case long:
		    case Long:
			    type = Scalars.GraphQLLong
			    break

		    default:
			    type = other()
	    }
	    type
    }

    public final static String INSTROSPECTION_QUERY =
"""
query IntrospectionQuery {
    __schema {
      queryType { name }
      mutationType { name }
      subscriptionType { name }
      types {
        ...FullType
      }
      directives {
        name
        description
        args {
          ...InputValue
        }
      }
    }
  }
  fragment FullType on __Type {
    kind
    name
    description
    fields(includeDeprecated: true) {
      name
      description
      args {
        ...InputValue
      }
      type {
        ...TypeRef
      }
      isDeprecated
      deprecationReason
    }
    inputFields {
      ...InputValue
    }
    interfaces {
      ...TypeRef
    }
    enumValues(includeDeprecated: true) {
      name
      description
      isDeprecated
      deprecationReason
    }
    possibleTypes {
      ...TypeRef
    }
  }
  fragment InputValue on __InputValue {
    name
    description
    type { ...TypeRef }
    defaultValue
  }
  fragment TypeRef on __Type {
    kind
    name
    ofType {
      kind
      name
      ofType {
        kind
        name
        ofType {
          kind
          name
          ofType {
            kind
            name
            ofType {
              kind
              name
              ofType {
                kind
                name
                ofType {
                  kind
                  name
                }
              }
            }
          }
        }
      }
    }
  }
"""
}
