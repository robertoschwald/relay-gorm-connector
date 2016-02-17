package io.cirill.relay

import graphql.Scalars
import graphql.relay.Relay
import graphql.schema.*
import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

import java.lang.reflect.ParameterizedType

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLEnumType.newEnum
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject

/**
 * Created by mcirillo on 12/15/15.
 */
public class SchemaProvider {

    private static Relay relay = new Relay()
    private static TypeResolverProxy typeResolverProxy = new TypeResolverProxy()
    private static GraphQLInterfaceType NodeInterface = relay.nodeInterface(typeResolverProxy)

    private List<GraphQLObjectType> knownTypes
    private List<GraphQLEnumType> knownEnums

    private DataFetcher nodeDataFetcher

    public GraphQLSchema schema

    public SchemaProvider(DataFetcher nodeDataFetcher, Class... relayTypes) {

        this.nodeDataFetcher = nodeDataFetcher

        // be sure the relay annotation is present
        if (relayTypes.any({ !it.isAnnotationPresent(RelayType)} )){
            throw new Exception("Invalid relay type ${relayTypes.find({!it.isAnnotationPresent(RelayType)}).name}")
        }

        // convert annotated classes into gql object types
        knownEnums = relayTypes.findAll({ Enum.isAssignableFrom(it) }).collect { classToGQLEnum(it) }
        knownTypes = relayTypes.findAll({ !Enum.isAssignableFrom(it) }).collect { classToGQLObject(it) }

        // convert gql object types into schema
        def queryBuilder = newObject()
                .name('RelayQuery')
                .field(relay.nodeField(NodeInterface, nodeDataFetcher))

        knownTypes.each { type ->
            def fieldBuilder = newFieldDefinition()
                    .name(type.name)
                    .type(type)
                    .argument(newArgument()
                        .name('id')
                        .type(nonNull(Scalars.GraphQLID))
                        .build())

            type.fieldDefinitions.collectMany({ it.arguments })*.asType(GraphQLArgument).each {
                fieldBuilder.argument( it )
            }

            queryBuilder.field(fieldBuilder.build())
        }

        schema = GraphQLSchema.newSchema().query(queryBuilder.build()).build()
    }

    private GraphQLObjectType classToGQLObject(Class domainClass) {
        def objectBuilder = newObject()
                .name(domainClass.simpleName)
                .description(domainClass.getAnnotation(RelayType)?.description())
                .field(newFieldDefinition()
                    .name('id')
                    .type(nonNull(Scalars.GraphQLID))
                    .fetchField()
                    .build())

        // add fields/arguments to the graphQL object for each domain field tagged for Relay
        domainClass.declaredFields.findAll({ it.isAnnotationPresent(RelayField) }).each { domainClassField ->

            boolean isArgument = domainClassField.getAnnotation(RelayArgument)
            boolean isArgumentNullable = isArgument ? domainClassField.getAnnotation(RelayArgument).nullable() : false

            String description = domainClass.getAnnotation(RelayType)?.description()

            def fieldBuilder = newFieldDefinition().name(domainClassField.name).description(description)
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

                    // field describes a type
                    if (domainClassField.type.isAnnotationPresent(RelayType)) {

                        // the field describes an enumeration
                        if (Enum.isAssignableFrom(domainClassField.type)) {
                            fieldBuilder.type(knownEnums.find { it.name == domainClassField.type.simpleName })
                        }

                        // the field describes some other type
                        else {
                            def reference = new GraphQLTypeReference(domainClassField.type.simpleName)
                            def argumentName = { argType -> domainClassField.name + 'With' + (argType as String) }

                            // Allow base type to be found via a name or ID from a nested type
                            fieldBuilder.type(reference)
                            //fieldBuilder.argument(makeArgument(argumentName('Name'), Scalars.GraphQLString, true))
                            //fieldBuilder.argument(makeArgument(argumentName('Id'), Scalars.GraphQLID, true))
                        }
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
                        List<GraphQLFieldDefinition> args = new ArrayList<>()
                        def typeForEdge = new GraphQLTypeReference(genericType.simpleName)
                        GraphQLObjectType edgeType = relay.edgeType(typeForEdge.name, typeForEdge, NodeInterface, args)
                        GraphQLObjectType connectionType = relay.connectionType(typeForEdge.name, edgeType, args)
                        fieldBuilder.type(connectionType)
                    }

                    else {
                        throw new Exception("Illegal type parameter for ${domainClass.name + '.' + domainClassField.name}")
                    }
            }

            // build simple argument for Scalar types
            if (scalarType) {
                fieldBuilder.type(scalarType)
                if (isArgument) {
                    fieldBuilder.argument(makeArgument(domainClassField.name, scalarType, isArgumentNullable))
                }
            }

            objectBuilder.field(fieldBuilder.fetchField().build())
        }

        objectBuilder.build()
    }

    private static GraphQLEnumType classToGQLEnum(Class type) {
        def enumBuilder = newEnum().name(type.simpleName)

        type.declaredFields.findAll({ it.isAnnotationPresent(RelayField) }).each { field ->
            enumBuilder.value(field.name)
        }

        enumBuilder.build()
    }

    private static GraphQLNonNull nonNull(GraphQLInputType obj) {
        new GraphQLNonNull(obj)
    }

    private static GraphQLArgument makeArgument(String name, GraphQLInputType type, boolean isNullable) {
        newArgument().name(name).type(isNullable ? type : nonNull(type)).build()
    }
}
