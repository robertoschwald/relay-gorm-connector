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
        def query2 = "{ person: node(id: \"$id\") { id ... on Person { name } } }" // when the type of the node is unkown

        when:
        def result2 = service.query(query2)

        then:
        result2.data?.person?.name == result2.data?.person?.name
    }

    def "Enum as argument"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        cal.save(flush:true)

        def queryByEnum = """{ bySpecies(species: $cal.species) { name }}"""

        when:
        def resultByEnum = service.query(queryByEnum)

        then:
        resultByEnum.errors == []
        resultByEnum.data?.bySpecies?.name == cal.name
    }

    def "Get nested field data"() {
        given:
        def bill = new Person(name:'Bill')
        def steve = new Person(name:'Steve', bestFriend: bill)
        [bill, steve]*.save(flush:true)

        def id = toID('Person', steve.id)
        def query = """{ node(id: \"$id\") { ... on Person { bestFriend { name }}}}"""

        when:
        def result = service.query(query)

        then:
        result.data?.node?.bestFriend?.name == bill.name
    }

    def "Plural identifying root"() {
        given:
        def bill = new Person(name:'Bill', age:10)
        def steve = new Person(name:'Steve', age:12)
        [bill, steve]*.save(flush:true)

        def query = "{ findByNameWithAges(name: [\"$bill.name\", \"$steve.name\"], age: [10,12]) { id }}"

        when:
        def result = service.query(query)

        then:
        result.errors == []
        result.data?.findByNameWithAges[0]?.id == toID('Person', bill.id)
        result.data?.findByNameWithAges[1]?.id == toID('Person', steve.id)
    }

    def "List query"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        def snoop = new Pet(name:'Snoopy', species: Species.Dog)
        def bill = new Person(name:'Bill', age:10, pets: [cal, snoop])
        [cal, snoop]*.owner = bill
        [cal, snoop, bill]*.save(flush: true)

        def query = "{ node(id: \"${toID('Person', bill.id)}\") { ... on Person { pets { name, species }}}}"

        when:
        def result = service.query(query)

        then:
        result.errors == []
    }
}