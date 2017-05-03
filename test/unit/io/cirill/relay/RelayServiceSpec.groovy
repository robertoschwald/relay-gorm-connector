package io.cirill.relay

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import io.cirill.relay.Pet.Species
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RelayService)
@Mock([Person, Pet])
class RelayServiceSpec extends Specification {

    @Shared
    def toID = { type, id -> RelayHelpers.toGlobalId(type as String, id as String) }

    def "Add and retrieve a person directly"() {
        given:
        def steve = new Person(name:'Steve', age:10)
        steve.save(flush: true)

        expect:
        Person.countByName('Steve') == 1
        Person.countById(1) == 1
    }

    def "Add and retrieve a person via Node Interface"() {
        given:
        def bill = new Person(name: 'Bill', age: 12)
        bill.save(flush: true)

        def id = toID('Person', bill.id)
        def query2 = "{ person: node(id: \"$id\") { id ... on Person { name } } }" // when the type of the node is unknown

        when:
        def result2 = service.query(query2, null, [:])

        then:
        result2.data?.person?.name == result2.data?.person?.name
    }

    def "Enum as argument"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        cal.save(flush:true)

        def queryByEnum = """{ bySpecies(species: $cal.species) { name }}"""

        when:
        def resultByEnum = service.query(queryByEnum, null, [:])

        then:
        resultByEnum.data?.bySpecies[0]?.name == cal.name
    }

    def "Get nested field data"() {
        given:
        def bill = new Person(name:'Bill')
        def steve = new Person(name:'Steve', bestFriend: bill)
        [bill, steve]*.save(flush:true)

        def id = toID('Person', steve.id)
        def query = """query { node(id: \"$id\") { ... on Person { bestFriend { name }}}}"""

        when:
        def result = service.query(query, null, [:])

        then:
        result.data?.node?.bestFriend?.name == bill.name
    }

    def "Plural identifying root"() {
        given:
        def bill = new Person(name:'Bill', age:10)
        def steve = new Person(name:'Steve', age:12)
        [bill, steve]*.save(flush:true)

        def query = "{ findByNames(name: [\"$bill.name\", \"$steve.name\"]) { id }}"

        when:
        def result = service.query(query, null, [:])

        then:
        result.data?.findByNames[0]?.id == toID('Person', bill.id)
        result.data?.findByNames[1]?.id == toID('Person', steve.id)
    }

    def "List field"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        def snoop = new Pet(name:'Snoopy', species: Species.Dog)
        def bill = new Person(name:'Bill', age:10, pets: [cal, snoop])
        [cal, snoop]*.owner = bill
        [cal, snoop, bill]*.save(flush: true)

        def query = "{ node(id: \"${toID('Person', bill.id)}\") { ... on Person { pets { name, species }}}}"

        when:
        def result = service.query(query, null, [:])

        then:
        result.data.node?.pets[0]?.name == cal.name
        result.data.node?.pets[1]?.name == snoop.name
    }

    def "Query for list"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        def snoop = new Pet(name:'Snoopy', species: Species.Cat)
        [cal, snoop]*.save(flush: true)

        def query = "query { bySpecies(species: Cat) { name }}"

        when:
        def result = service.query query, null, [:]

        then:
        result.data.bySpecies[0]?.name == cal.name
        result.data.bySpecies[1]?.name == snoop.name
    }

    def "Add via mutation query"() {
        given:
        def mutationId = '1234'
        def query = "mutation { addPerson(input: {name: \"Steve\", age: 10, clientMutationId: \"$mutationId\"}) { newPerson { id }, clientMutationId } }"

        when:
        def result = service.query query, null, [:]

	    then:
	    result.data.addPerson.newPerson.id == toID('Person', 1)
	    result.data.addPerson.clientMutationId == mutationId
    }

    def "Connection test"() {
        given:
        def bill = new Person(name:'Bill', age:10)
        def steve = new Person(name:'Steve', age:12)
        def sally = new Person(name:'Sally', age: 6)
        bill.children = [steve, sally]
        [bill, steve, sally]*.save(flush:true)
        def cursor = null

        def query = "query { node(id: \"${toID('Person', bill.id)}\") { ... on Person { children { totalChildren, edges { childsBirthday }}}}}"
        def query2 = "query { node(id: \"${toID('Person', bill.id)}\") { ... on Person { children(first: 1) { edges { cursor, node { name }}}}}}"
        def query3 = "query { node(id: \"${toID('Person', bill.id)}\") { ... on Person { children(first: 1, after: \"${->cursor}\") { edges { node { name }}}}}}"

        when:
        def result = service.query query, null, [:]
        def result2 = service.query query2, null, [:]
        cursor = result2.data.node.children.edges[0].cursor
        def result3 = service.query query3, null, [:]

        then:
        result.data.node.children.size() == 2
        result.data.node.children.totalChildren == 2
        result.data.node.children.edges[0].childsBirthday == Calendar.getInstance().get(Calendar.YEAR) - steve.age
        result.data.node.children.edges[1].childsBirthday == Calendar.getInstance().get(Calendar.YEAR) - sally.age
        result2.data.node.children.edges[0].node.name == steve.name
        result3.data.node.children.edges[0].node.name == sally.name
    }

    def "Proxy field test"() {
        given:
        def bill = new Person(name:'Bill', age:10, notARelayField: 'hidden from relay')
        bill.save(flush: true)
        def query = "query { node(id: \"${toID('Person', bill.id)}\") { ... on Person { proxyField }}}"

        when:
        def result = service.query query, null, [:]

        then:
        result.data.node.proxyField == bill.notARelayField
    }
}