package io.cirill.relay

import graphql.Scalars
import graphql.relay.SimpleListConnection
import graphql.schema.*
import io.cirill.relay.annotation.*
import io.cirill.relay.dsl.GQLFieldSpec

import java.lang.reflect.ParameterizedType

public class SchemaProvider {

    @Deprecated
    public static Map<Class, GraphQLEnumType> GLOBAL_ENUM_RESOLVE = [:]

    Map<Class, GraphQLObjectType> typeResolve
    Map<Class, GraphQLEnumType> enumResolve
    GraphQLInterfaceType nodeInterface

    private DataFetcher nodeDataFetcher
    private TypeResolver typeResolver = { object -> typeResolve[object.getClass()] }

    def GraphQLSchema schema

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
    }

    private GraphQLSchema buildSchema() {

        // this is needed for now as we can't yet use a GraphQLTypeReference for input types
        enumResolve.each { clazz, gql ->
            GLOBAL_ENUM_RESOLVE[clazz] = gql
        }

        // build root edgeFields for queries
        def queryBuilder = graphql.schema.GraphQLObjectType.newObject().name('queryType').field(RelayHelpers.nodeField(nodeInterface, nodeDataFetcher))
        typeResolve.each { domainObj, gqlObj ->

            def queryNames = domainObj.declaredFields.findAll({ it.isAnnotationPresent(RelayQuery) })*.name
            queryBuilder.fields queryNames.collect { name ->
                Closure<GraphQLFieldDefinition> source = domainObj."$name"
                def code = source.rehydrate(source.delegate, thisObject, source.thisObject)
                code()
            }

            def queriesNames = domainObj.declaredFields.findAll({ it.isAnnotationPresent(GraphQLQueries) })*.name
            queryBuilder.fields queriesNames.collect { name ->
                Closure<List<GraphQLFieldDefinition>> sources = domainObj."$name"
                def code = sources.rehydrate(sources.delegate, thisObject, sources.thisObject)
                code()
            }.flatten() as List<GraphQLFieldDefinition>
        }

        // build root edgeFields for mutations
	    def mutationBuilder = graphql.schema.GraphQLObjectType.newObject().name('mutationType')
	    typeResolve.each { domainObj, gqlObj ->

            def mutationNames = domainObj.declaredFields.findAll({ it.isAnnotationPresent(RelayMutation) })*.name
            mutationBuilder.fields mutationNames.collect { name ->
                Closure<GraphQLFieldDefinition> source = domainObj."$name"
                def code = source.rehydrate(source.delegate, thisObject, source.thisObject)
                code()
            }

            def mutationsNames = domainObj.declaredFields.findAll({ it.isAnnotationPresent(GraphQLMutations) })*.name
            mutationBuilder.fields mutationsNames.collect { name ->
                Closure<List<GraphQLFieldDefinition>> source = domainObj."$name"
                def code = source.rehydrate(source.delegate, thisObject, source.thisObject)
                code()
            }.flatten() as List<GraphQLFieldDefinition>
	    }

        queryBuilder = queryBuilder.build()
        mutationBuilder = mutationBuilder.build()

        List<GraphQLType> allTypes = []
        allTypes << nodeInterface
        allTypes.addAll typeResolve.values()
        allTypes.addAll enumResolve.values()
        // make sure all built ins are available
        allTypes.addAll([
                Scalars.GraphQLInt,
                Scalars.GraphQLString,
                Scalars.GraphQLBoolean,
                Scalars.GraphQLFloat,
                Scalars.GraphQLLong,
                Scalars.GraphQLShort,
                Scalars.GraphQLBigInteger,
                Scalars.GraphQLBigDecimal
        ])

        def r = GraphQLSchema.newSchema()
        if (queryBuilder.fieldDefinitions.size() > 0) {
            r.query(queryBuilder)
        }
        if (mutationBuilder.fieldDefinitions.size() > 0) {
            r.mutation(mutationBuilder)
        }
        r.build(allTypes.toSet())
    }

    private GraphQLObjectType classToGQLObject(Class domainClass) {

        def proxyFields = domainClass.declaredFields.findAll({ it.isAnnotationPresent(RelayProxyField) }).collect({
            Closure<GraphQLFieldDefinition> source = domainClass."$it.name"
            def code = source.rehydrate(source.delegate, thisObject, source.thisObject)
            code()
        })

        def multiProxyFields = domainClass.declaredFields.findAll({ it.isAnnotationPresent(GraphQLFields) }).collect({
            Closure<List<GraphQLFieldDefinition>> sources = domainClass."$it.name"
            def code = sources.rehydrate(sources.delegate, thisObject, sources.thisObject)
            code()
        }).flatten()

        def objectBuilder = graphql.schema.GraphQLObjectType.newObject()
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
                .fields(proxyFields)
                .fields(multiProxyFields)


        // add all normal/list relay fields
        domainClass.declaredFields.findAll({ it.isAnnotationPresent(RelayField) }).each { domainClassField ->

            String fieldDescription = domainClassField.getAnnotation(RelayField).description()

            def fieldBuilder = graphql.schema.GraphQLFieldDefinition.newFieldDefinition().name(domainClassField.name).description(fieldDescription)
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

                case short:
                case Short:
                    scalarType = Scalars.GraphQLShort
                    break

                case BigInteger:
                    scalarType = Scalars.GraphQLBigInteger
                    break

                case BigDecimal:
                    scalarType = Scalars.GraphQLBigDecimal
                    break

                default:
                    /*
                        If the inputObject's type isn't covered above, check for the RelayType annotation on the type's
                        description. If the type is a List, then we will check the list's generic type for the RelayType
                        annotation (some heavy reflection here) and create a relay 'connectionType' relationship if it present.
                     */

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

                        def typeForEdge = new GraphQLTypeReference(genericType.simpleName)
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
        def enumBuilder = graphql.schema.GraphQLEnumType.newEnum().name(type.simpleName).description(description)

        for (def it : EnumSet.allOf(type)) {
            enumBuilder.value(it.toString())
        }

        enumBuilder.build()
    }
}
