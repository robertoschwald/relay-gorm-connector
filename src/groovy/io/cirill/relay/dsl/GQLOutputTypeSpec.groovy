package io.cirill.relay.dsl

import graphql.schema.*
import io.cirill.relay.RelayHelpers

public class GQLOutputTypeSpec {

    private String name
    private String description = ''
    private List<GraphQLFieldDefinition> fields = []
    private GraphQLTypeReference ref
    private GraphQLList list
    private GraphQLNonNull nonNull
    private List<GraphQLInterfaceType> interfaces = []

    void name(String n) { name = n }
    void description(String d) { description = d }
    void field(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLFieldSpec) Closure cl) {
        fields.add GQLFieldSpec.field(cl)
    }
    void ref(String n) { ref = new GraphQLTypeReference(n) }
    void list(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        list = new GraphQLList(type(cl))
    }
    void list(GraphQLOutputType t) { list = new GraphQLList(t) }
    void nonNull(GraphQLType t) { nonNull = RelayHelpers.nonNull(t) }
    void nonNull(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        nonNull(type(cl))
    }
    void withInterface(GraphQLInterfaceType interfaceType) {
        interfaces << interfaceType
    }

    GraphQLOutputType build() {
        if (ref) {
            ref
        } else if (list) {
            list
        } else if (nonNull) {
            nonNull
        } else {
            def obj = graphql.schema.GraphQLObjectType.newObject().name(name).description(description).fields(fields)
            interfaces.each { obj.withInterface(it) }
            obj.build()
        }
    }

    public static GraphQLOutputType type(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GQLOutputTypeSpec) Closure cl) {
        GQLOutputTypeSpec spec = new GQLOutputTypeSpec()
        def owner = Closure.isAssignableFrom(cl.owner.class) ? cl.owner.owner : cl.owner
        Closure code = cl.rehydrate(spec, owner, cl.thisObject)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return spec.build()
    }
}