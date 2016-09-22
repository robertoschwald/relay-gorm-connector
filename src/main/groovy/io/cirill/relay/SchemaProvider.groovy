package io.cirill.relay

import graphql.GraphQL
import graphql.Scalars
import graphql.relay.SimpleListConnection
import graphql.schema.*
import io.cirill.relay.annotation.*
import io.cirill.relay.dsl.GQLFieldSpec

import java.lang.reflect.ParameterizedType

import static graphql.schema.GraphQLEnumType.newEnum
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject

public class SchemaProvider {

    public static Map<Class, GraphQLEnumType> GLOBAL_ENUM_RESOLVE = [:]

    public Map<Class, GraphQLObjectType> typeResolve
    public Map<Class, GraphQLEnumType> enumResolve

    private GraphQLSchema schema

    private DataFetcher nodeDataFetcher
    private TypeResolver typeResolver = { object -> typeResolve[object.getClass()] }
    private GraphQLInterfaceType nodeInterface
    private GraphQL graphQL

    public SchemaProvider(DataFetcher ndf, Class... domainClasses) {

        nodeDataFetcher = ndf
        nodeInterface = RelayHelpers.nodeInterface(typeResolver)

        // be sure the relay annotation is present
        if (domainClasses.any({ !it.isAnnotationPresent(RelayType) })) {
            throw new Exception("Invalid relay type ${domainClasses.find({!it.isAnnotationPresent(RelayType)}).name}")
        }

        // convert annotated classes into gql object types
        enumResolve = domainClasses.collectEntries { clazz ->
            clazz.getDeclaredClasses()
                    .findAll{ it.isAnnotationPresent(RelayEnum) }
                    .collectEntries { [it, classToGQLEnum(it, it.getAnnotation(RelayEnum)?.description())] }
        }

        typeResolve = domainClasses.collectEntries { [it, classToGQLObject(it)] }

        schema = buildSchema()
        graphQL = new GraphQL(schema)
    }

	public GraphQL graphQL() { return graphQL }

    private GraphQLSchema buildSchema() {

        // this is needed for now as we can't yet use a GraphQLTypeReference for input types
        enumResolve.each { clazz, gql ->
            GLOBAL_ENUM_RESOLVE[clazz] = gql
        }

        // build root edgeFields for queries
        def queryBuilder = newObject()
                .name('queryType')
                .field(RelayHelpers.nodeField(nodeInterface, nodeDataFetcher))

        typeResolve.each { domainObj, gqlObj ->
            def queryNames = domainObj.declaredFields.findAll({ it.isAnnotationPresent(RelayQuery) })*.name
            queryBuilder.fields queryNames.collect { name -> domainObj."$name"() }
        }

        // build root edgeFields for mutations
	    def mutationBuilder = newObject().name('mutationType') // TODO

	    typeResolve.each { domainObj, gqlObj ->
            def mutationNames = domainObj.declaredFields.findAll({ it.isAnnotationPresent(RelayMutation) })*.name
            mutationBuilder.fields mutationNames.collect { name -> domainObj."$name"() }
	    }

        List<GraphQLType> allTypes = []
        allTypes.addAll typeResolve.values()
        allTypes.addAll enumResolve.values()

        GraphQLSchema.newSchema().query(queryBuilder.build()).mutation(mutationBuilder.build()).build(allTypes.toSet())
    }

    private GraphQLObjectType classToGQLObject(Class domainClass) {
        def objectBuilder = newObject()
                .name(domainClass.simpleName)
                .description(domainClass.getAnnotation(RelayType).description())
                .field(GQLFieldSpec.field {
                    name 'id'
                    description RelayHelpers.DESCRIPTION_ID_ARGUMENT
                    type RelayHelpers.nonNull(Scalars.GraphQLID)
                    dataFetcher { env ->
                        def obj = env.getSource()
                        return RelayHelpers.toGlobalId(domainClass.simpleName, obj.id as String)
                    }
                })

        // add all normal/list relay fields
        domainClass.declaredFields.findAll({ it.isAnnotationPresent(RelayField) }).each { domainClassField ->

            String fieldDescription = domainClassField.getAnnotation(RelayField).description()

            def fieldBuilder = newFieldDefinition().name(domainClassField.name).description(fieldDescription)
            GraphQLScalarType scalarType

            switch (domainClassField.type) {

                case int:
                case Integer:
                    scalarType = Scalars.GraphQLInt
                    break

                case String:
                    scalarType = Scalars.GraphQLString
                    break

                case boolean:
                case Boolean:
                    scalarType = Scalars.GraphQLBoolean
                    break

                case float:
                case Float:
                    scalarType = Scalars.GraphQLFloat
                    break

                case long:
                case Long:
                    scalarType = Scalars.GraphQLLong
                    break

                default:
                    /*
                        If the inputObject's type isn't covered above, check for the RelayType annotation on the type's
                        description. If the type is a List, then we will check the list's generic type for the RelayType
                        annotation (some heavy reflection here) and create a relay 'connectionType' relationship if it present.
                     */

                    // inputObject describes an enum type
                    if (domainClassField.type.isAnnotationPresent(RelayEnum)) {

                        // the inputObject describes an enumeration
                        if (Enum.isAssignableFrom(domainClassField.type)) {
                            def gqlEnum = enumResolve[domainClassField.type]
                            fieldBuilder.type(gqlEnum)
                            fieldBuilder.dataFetcher({ env ->
                                def obj = env.getSource()
                                return obj."$domainClassField.name".toString()
                            })
                        }
                    }

                    else if (domainClassField.type.isAnnotationPresent(RelayType)) {
                        def reference = new GraphQLTypeReference(domainClassField.type.simpleName)
                        //def argumentName = { argType -> domainClassField.name + 'With' + (argType as String) }

                        // Allow base type to be found via a name or ID from a nested type
                        fieldBuilder.type(reference)
                        fieldBuilder.dataFetcher({ env ->
                            env.source."$domainClassField.name"
                        })
                    }

                    // inputObject describes a connectionType
                    else if (List.isAssignableFrom(domainClassField.type)) {

                        // parse the parameterized type of the list
                        def genericType = Class.forName(domainClassField.getGenericType().asType(ParameterizedType).getActualTypeArguments().first().typeName)

                        // throw an error if the generic type isn't marked for relay
                        if (!genericType.isAnnotationPresent(RelayType)) {
                            throw new Exception("Illegal relay type $genericType.simpleName for connectionType at ${domainClass.name + '.' + domainClassField.name}")
                        }

                        // TODO implement SimpleConnection
//                        List<GraphQLFieldDefinition> args = new ArrayList<>()
                        def typeForEdge = new GraphQLTypeReference(genericType.simpleName)
//                        GraphQLObjectType edgeType = relay.edgeType(typeForEdge.name, typeForEdge, nodeInterface, args)
//                        GraphQLObjectType connectionType = relay.connectionType(typeForEdge.name, edgeType, args)
//                        fieldBuilder.type(connectionType)
                        fieldBuilder.type(new GraphQLList(typeForEdge))
                        fieldBuilder.dataFetcher({ env ->
                            def obj = env.getSource()
                            return obj."$domainClassField.name"
                        })
                    }

                    else {
                        throw new Exception("Illegal type parameter for ${domainClass.name + '.' + domainClassField.name}")
                    }
            }

            // data fetching for scalar types
            if (scalarType) {
                fieldBuilder.type(scalarType)
                fieldBuilder.dataFetcher({ env ->
                    def obj = env.getSource()
                    return obj."$domainClassField.name"
                })
            }

            objectBuilder.field(fieldBuilder.fetchField().build())
        }

        // add connections
        domainClass.declaredFields.findAll({ it.isAnnotationPresent(RelayConnection) }).each {
            String dataField = it.getAnnotation(RelayConnection).connectionFor()
            GraphQLObjectType connectionType = (GraphQLObjectType) domainClass."$it.name"()
            objectBuilder.field(GQLFieldSpec.field {
                name dataField
                type connectionType
                arguments RelayHelpers.relayArgs()
                dataFetcher { env ->
                    new SimpleListConnection(env.source."$dataField" as List).get(env)
                }
            })
        }

        objectBuilder.withInterface(nodeInterface).build()
    }

    private static GraphQLEnumType classToGQLEnum(Class type, String description) {
        def enumBuilder = newEnum().name(type.simpleName).description(description)

        type.declaredFields.each { field ->
            enumBuilder.value(field.name)
        }

        enumBuilder.build()
    }
}
