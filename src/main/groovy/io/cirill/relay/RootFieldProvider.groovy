package io.cirill.relay

import graphql.Scalars
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.TypeResolver
import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayType

import java.lang.reflect.Method

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition

/**
 * relay-gorm-connector
 * @author mcirillo
 */
class RootFieldProvider {

    private List<GraphQLArgument> singleArguments
    private List<GraphQLArgument> pluralArguments

    GraphQLFieldDefinition singleField
    GraphQLFieldDefinition pluralField

    private Map<Class, GraphQLEnumType> knownEnums

    public RootFieldProvider(Class domainClass, GraphQLObjectType objectType, Map<Class, GraphQLEnumType> knownEnums) {

        this.knownEnums = knownEnums

        singleArguments = objectType.fieldDefinitions.collectMany { it.arguments }
        singleArguments.addAll domainClass.declaredMethods
                .findAll({ it.isAnnotationPresent(RelayArgument) })
                .collect { buildArgument(it, domainClass) }

        pluralArguments = singleArguments.collect { arg ->
            new GraphQLArgument(arg.name, arg.description, new GraphQLList(arg.type), null)
        }

        def singleFieldBuilder = newFieldDefinition()
                .name(objectType.name.toLowerCase())
                .description(objectType.description)
                .type(objectType)
                .argument(newArgument()
                    .name('id')
                    .description(RelayHelpers.DESCRIPTION_ID_ARGUMENT)
                    .type(Scalars.GraphQLID)
                    .build())

        singleFieldBuilder.argument(singleArguments)
        singleFieldBuilder.dataFetcher(new DefaultSingleDataFetcher(domainClass, singleArguments.collect({it.name})))
        singleField = singleFieldBuilder.build()

        def pluralFieldBuilder = newFieldDefinition()
                .name(domainClass.getAnnotation(RelayType).pluralName().toLowerCase())
                .description(objectType.description)
                .type(new GraphQLList(objectType))
                .argument(newArgument()
                    .name('id')
                    .description(RelayHelpers.DESCRIPTION_ID_ARGUMENT)
                    .type(Scalars.GraphQLID)
                    .build())

        pluralFieldBuilder.argument(pluralArguments)
        pluralFieldBuilder.dataFetcher(new DefaultPluralDataFetcher(domainClass, singleArguments.collect({it.name})))
        pluralField = pluralFieldBuilder.build()
    }

    private GraphQLArgument buildArgument(Method method, Class clazz) {
        if (method.parameterCount != 1) {
            throw new Exception('Argument ' + method.name + ' must have only one parameter')
        }

        if (method.returnType != clazz) {
            throw new Exception('Argument ' + method.name + ' must return type ' + clazz.name)
        }

        def type
        switch (method.parameterTypes[0]) {
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
                if (method.parameterTypes[0].isAnnotationPresent(RelayType) && Enum.isAssignableFrom(method.parameterTypes[0])) {
                    type = knownEnums[method.parameterTypes[0]]
                } else {
                    throw new Exception('Illegal return type ' + method.returnType)
                }
        }

        boolean isArgumentNullable = method.getAnnotation(RelayArgument)?.nullable()
        String argumentDescription = method.getAnnotation(RelayArgument)?.description()

        RelayHelpers.makeArgument(method.name, type, argumentDescription, isArgumentNullable)
    }
}
