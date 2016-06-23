package io.cirill.relay

import graphql.schema.*
import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayEnum
import io.cirill.relay.annotation.RelayQuery
import io.cirill.relay.annotation.RelayType

import java.lang.reflect.Method
import java.lang.reflect.Parameter

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition

/**
 * relay-gorm-connector
 * @author mcirillo
 */
class RootFieldProvider {

    List<GraphQLFieldDefinition> fields

    private Map<Class, GraphQLEnumType> knownEnums

    public RootFieldProvider(Class domainClass, GraphQLObjectType objectType, Map<Class, GraphQLEnumType> knownEnums) {

        this.knownEnums = knownEnums

        fields = domainClass.getDeclaredMethods()
            .findAll({ it.isAnnotationPresent(RelayQuery) })
            .collectMany({ method ->
                if (method.name == method.getAnnotation(RelayQuery).pluralName()) {
                    throw new Exception('Plural root name is the same as the single root name ' + method.name)
                }

                boolean isList = method.returnType.isAssignableFrom(List)

                def ret = [
                        newFieldDefinition()
                            .name(method.name)
                            .description(method.getAnnotation(RelayQuery).description())
                            .type(isList ? new GraphQLList(objectType) : objectType)
                            .argument(buildArguments(method, domainClass))
                            .dataFetcher(new DefaultSingleDataFetcher(domainClass, method))
                            .build()
                ]

                if (method.getAnnotation(RelayQuery).pluralName()) {
                    ret << newFieldDefinition()
                            .name(method.getAnnotation(RelayQuery).pluralName())
                            .description(method.getAnnotation(RelayQuery).description())
                            .type(isList ? new GraphQLList(new GraphQLList(objectType)) : new GraphQLList(objectType))
                            .argument(buildArguments(method, domainClass, true))
                            .dataFetcher(new DefaultPluralDataFetcher(domainClass, method))
                            .build()
                }
                return ret
            })
    }

    private List<GraphQLArgument> buildArguments(Method method, Class clazz, boolean plural = false) {
        if (method.returnType != clazz) {
            if (method.returnType.isAssignableFrom(List)) {
                def genericType = Class.forName(method.annotatedReturnType.type.actualTypeArguments.first().typeName as String)
                if (!genericType.isAnnotationPresent(RelayType)) {
                    throw new Exception("Illegal relay type $genericType.simpleName for list at ${clazz.name + '.' + method.name}")
                }
            } else {
                throw new Exception('Argument ' + method.name + ' must return type ' + clazz.name)
            }
        }

        if (method.parameters.any { param -> !param.isAnnotationPresent(RelayArgument)}) {
            throw new Exception('Parameters for relay query fields must use the @RelayArgument annotation: ' + method.name)
        }

        method.getParameters().collect { buildArgument(it, plural) }
    }

    private GraphQLArgument buildArgument(Parameter param, boolean plural) {

        def type = RelayHelpers.parseGraphQLInputType param.type, {
            if (param.type.isAnnotationPresent(RelayEnum) && Enum.isAssignableFrom(param.type)) {
                knownEnums[param.type]
            } else {
                throw new Exception('Illegal parameter type ' + param.type)
            }
        }

        boolean isArgumentNullable = param.getAnnotation(RelayArgument).nullable()
        String argumentDescription = param.getAnnotation(RelayArgument).description()
        String name = param.getAnnotation(RelayArgument).name()
        RelayHelpers.makeArgument(name, plural ? new GraphQLList(type) : type, argumentDescription, isArgumentNullable)
    }
}
