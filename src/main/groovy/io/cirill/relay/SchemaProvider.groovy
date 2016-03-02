package io.cirill.relay

import graphql.Scalars
import graphql.schema.*
import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayEnum
import io.cirill.relay.annotation.RelayEnumField
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

import java.lang.reflect.ParameterizedType

import static graphql.schema.GraphQLEnumType.newEnum
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject

/**
 * Created by mcirillo on 12/15/15.
 */
public class SchemaProvider {

    public Map<Class, GraphQLObjectType> typeResolve
    public Map<Class, GraphQLEnumType> enumResolve
    public GraphQLSchema schema

    private DataFetcher nodeDataFetcher
    private TypeResolver typeResolver = { object -> typeResolve[object.getClass()] }
    private GraphQLInterfaceType nodeInterface

    public SchemaProvider(DataFetcher ndf, Class... domainClasses) {

        nodeDataFetcher = ndf
        nodeInterface = RelayHelpers.nodeInterface(typeResolver)

        // be sure the relay annotation is present
        if (domainClasses.any({ !it.isAnnotationPresent(RelayType)} )){
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
    }

    private GraphQLSchema buildSchema() {
        def queryBuilder = newObject()
                .name('RelayQuery')
                .field(RelayHelpers.nodeField(nodeInterface, nodeDataFetcher))

        typeResolve.each { domainObj, gqlObj ->
            def rootFields = new RootFieldProvider(domainObj, gqlObj, enumResolve)
            queryBuilder.field(rootFields.singleField)
            queryBuilder.field(rootFields.pluralField)
        }

        GraphQLSchema.newSchema().query(queryBuilder.build()).build()
    }

    private GraphQLObjectType classToGQLObject(Class domainClass) {
        def objectBuilder = newObject()
                .name(domainClass.simpleName)
                .description(domainClass.getAnnotation(RelayType).description())
                .field(newFieldDefinition()
                    .name('id')
                    .description(RelayHelpers.DESCRIPTION_ID_ARGUMENT)
                    .type(RelayHelpers.nonNull(Scalars.GraphQLID))
                    .dataFetcher( { env ->
                        def obj = env.getSource()
                        return RelayHelpers.toGlobalId(domainClass.simpleName, obj.id as String)
                    } as DataFetcher)
                    .fetchField()
                    .build())

        // add fields/arguments to the graphQL object for each domain field tagged for Relay
        domainClass.declaredFields.findAll({ it.isAnnotationPresent(RelayField) }).each { domainClassField ->

            boolean isArgument = domainClassField.getAnnotation(RelayArgument)
            boolean isArgumentNullable = domainClassField.getAnnotation(RelayArgument)?.nullable()
            boolean isArgumentUnique = domainClassField.getAnnotation(RelayArgument)?.unique()

            String fieldDescription = domainClassField.getAnnotation(RelayField).description()
            String argumentDescription = domainClassField.getAnnotation(RelayArgument)?.description()

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
                        If the field's type isn't covered above, check for the RelayType annotation on the type's
                        definition. If the type is a List, then we will check the list's generic type for the RelayType
                        annotation (some heavy reflection here) and create a relay 'connection' relationship if it present.
                     */

                    // field describes an enum type
                    if (domainClassField.type.isAnnotationPresent(RelayEnum)) {

                        // the field describes an enumeration
                        if (Enum.isAssignableFrom(domainClassField.type)) {
                            def gqlEnum = enumResolve[domainClassField.type]
                            fieldBuilder.type(gqlEnum)
                            fieldBuilder.dataFetcher({ env ->
                                def obj = env.getSource()
                                return obj."$domainClassField.name".toString()
                            })
                            if (isArgument) {
                                fieldBuilder.argument(RelayHelpers.makeArgument(
                                        domainClassField.name,
                                        gqlEnum,
                                        argumentDescription,
                                        isArgumentNullable,
                                        isArgumentUnique))
                            }
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

//                            if (isArgument) {
//                                //fieldBuilder.argument(makeArgument(argumentName('Name'), Scalars.GraphQLString, true))
//                                fieldBuilder.argument(makeArgument(argumentName('Id'), Scalars.GraphQLID, argumentDescription, false))
//                            }
                    }

                    // field describes a connection
                    else if (List.isAssignableFrom(domainClassField.type)) {

                        // parse the parameterized type of the list
                        def genericType = Class.forName(domainClassField.getGenericType().asType(ParameterizedType).getActualTypeArguments().first().typeName)

                        // throw an error if the generic type isn't marked for relay
                        if (!genericType.isAnnotationPresent(RelayType)) {
                            throw new Exception("Illegal relay type $genericType.simpleName for connection at ${domainClass.name + '.' + domainClassField.name}")
                        }

                        // TODO implement SimpleConnection
//                        List<GraphQLFieldDefinition> args = new ArrayList<>()
//                        def typeForEdge = new GraphQLTypeReference(genericType.simpleName)
//                        GraphQLObjectType edgeType = relay.edgeType(typeForEdge.name, typeForEdge, nodeInterface, args)
//                        GraphQLObjectType connectionType = relay.connectionType(typeForEdge.name, edgeType, args)
//                        fieldBuilder.type(connectionType)
                    }

                    else {
                        throw new Exception("Illegal type parameter for ${domainClass.name + '.' + domainClassField.name}")
                    }
            }

            // build simple argument for Scalar types
            if (scalarType) {
                fieldBuilder.type(scalarType)
                fieldBuilder.dataFetcher({ env ->
                    def obj = env.getSource()
                    return obj."$domainClassField.name"
                })
                if (isArgument) {
                    fieldBuilder.argument(RelayHelpers.makeArgument(
                            domainClassField.name,
                            scalarType,
                            argumentDescription,
                            isArgumentNullable,
                            isArgumentUnique))
                }
            }

            objectBuilder.field(fieldBuilder.fetchField().build())
        }
        objectBuilder.withInterface(nodeInterface).build()
    }

    private static GraphQLEnumType classToGQLEnum(Class type, String description) {
        def enumBuilder = newEnum().name(type.simpleName).description(description)

        type.declaredFields.findAll({ it.isAnnotationPresent(RelayEnumField) }).each { field ->
            enumBuilder.value(field.name)
        }

        enumBuilder.build()
    }
}
