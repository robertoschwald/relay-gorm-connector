package io.cirill.relay

import graphql.Scalars
import graphql.schema.*
import io.cirill.relay.annotation.RelayEnum
import io.cirill.relay.annotation.RelayMutation
import io.cirill.relay.annotation.RelayMutationInput

import java.lang.reflect.Parameter

import static graphql.schema.GraphQLInputObjectField.newInputObjectField

/**
 * relay-gorm-connector
 * @author mcirillo
 */
public class DefaultMutationProvider {

	List<GraphQLFieldDefinition> mutations

	private Map<Class, GraphQLEnumType> knownEnums

    public DefaultMutationProvider(Class domainClass, GraphQLObjectType objectType, Map<Class, GraphQLEnumType> knownEnums) {
        this.knownEnums = knownEnums

        mutations = domainClass.getDeclaredMethods()
                .findAll({ it.isAnnotationPresent(RelayMutation) })
                .collect({ method ->
                    if (!method.parameters.every({p -> p.getAnnotation(RelayMutationInput)})) {
                        throw new Exception("Not all parameters of $method.name have @RelayMutationInput")
                    }

                    def outputFields = method.getAnnotation(RelayMutation).output()
	                outputFields = outputFields.collect { name -> objectType.fieldDefinitions.find({ it.name == name }) }
	                def inputFields = method.parameters.collect({ makeInputField(it) })

	                DataFetcher dataFetcher = { env ->
		                def args = method.parameters.collect { env.arguments.input."${it.getAnnotation(RelayMutationInput).name()}".asType(it.type) }
		                def domain = domainClass.invokeMethod(method.name, args as Object[])
		                def ret = method.getAnnotation(RelayMutation).output().collectEntries({ field -> [ field, domain."$field" ]})
		                ret.put('clientMutationId', env.arguments.input.clientMutationId)
		                return ret
	                }

	                RelayHelpers.makeMutation(method.name, method.name, inputFields, outputFields, dataFetcher)
                })
    }

	private GraphQLInputObjectField makeInputField(Parameter param) {

		def type
		switch (param.type) {
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
				if (param.type.isAnnotationPresent(RelayEnum) && Enum.isAssignableFrom(param.type)) {
					type = knownEnums[param.type]
				} else {
					throw new Exception('Illegal parameter type ' + param.type)
				}
		}

		newInputObjectField()
				.name(param.getAnnotation(RelayMutationInput).name())
				.type(new GraphQLNonNull(type))
				.build()
	}
}
