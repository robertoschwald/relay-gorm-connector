package io.cirill.relay

import graphql.language.Field
import graphql.language.InlineFragment
import graphql.relay.Relay
import graphql.relay.Relay.ResolvedGlobalId
import graphql.schema.*
import groovy.transform.CompileStatic

@CompileStatic
public class RelayHelpers {

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

    public static GraphQLNonNull nonNull(GraphQLType obj) {
        new GraphQLNonNull(obj)
    }

    public static GraphQLObjectType connectionType(String name, GraphQLObjectType edgeType, List<GraphQLFieldDefinition> connectionFields) {
        relay.connectionType(name, edgeType, connectionFields)
    }

    public static GraphQLObjectType edgeType(String name, GraphQLOutputType nodeType, List<GraphQLFieldDefinition> edgeFields) {
        relay.edgeType(name, nodeType, null, edgeFields)
    }

    public static List<GraphQLArgument> relayArgs() {
        relay.connectionFieldArguments
    }

	public static List<String> eagerAssociationStrings(DataFetchingEnvironment env) {
		env.fields.collectMany { queryField ->

			// extract fragments from the query
			List<InlineFragment> fragments = queryField.selectionSet.selections*.asType InlineFragment
			fragments.collectMany { fragment ->

				// extract fields from the fragment
				fragment.selectionSet.selections.collectMany { fragmentField ->
					Field field = fragmentField as Field

					// recursively analyze fields
					eagerAssocationStrings('', field) as Collection<String>
				} as Collection<String>
			} as Collection<String>
		} as List<String>
	}

	private static List<String> eagerAssocationStrings(String path, Field field) {
		String fieldPath = path + field.name
		List<String> ret = [ fieldPath ]
		if (field.selectionSet?.selections) {
			ret.addAll(field.selectionSet.selections.collectMany({ selection -> eagerAssocationStrings(fieldPath + '.', selection as Field) }))
		}
		ret
	}

    public final static String INTROSPECTION_QUERY =
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
