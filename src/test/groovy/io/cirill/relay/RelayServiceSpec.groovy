package io.cirill.relay

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*
import graphql.relay.Relay
import io.cirill.relay.Pet.Species

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

    def "Add and retrieve a person via Node Interface and Type argument"() {
        given:
        def bill = new Person(name: 'Bill', age: 12)
        bill.save(flush: true)

        def id = toID('Person', bill.id)
        def query = "{ person(id: \"$id\") { id name } }"
        def query2 = "{ person: node(id: \"$id\") { id ... on Person { name } } }"

        when:
        def result = service.query(query)
        def result2 = service.query(query2)

        then:
        result.data?.person?.id == id
        result.data?.person?.id == result2.data?.person?.id
        result.data?.person?.name == result2.data?.person?.name
    }

    def "Test pet custom argument, enum fetching, and enum as argument"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        cal.save(flush:true)

        def id = toID('Pet', cal.id)
        def query = "{ pet(id: \"$id\") { id name species } }"
        def queryByEnum = """{ pet(species: $cal.species) { name }}"""
        def queryByLikeName = """{ pet(singleByNameLike: \"%al\") { name }}"""

        when:
        def result = service.query(query)
        def resultByEnum = service.query(queryByEnum)

        then:
        result.data?.pet?.name == cal.name
        result.data?.pet?.species as Species == cal.species

        resultByEnum.errors == []
        resultByEnum.data?.pet?.name == cal.name
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

    def "Plural identifying root nodes using static method query"() {
        given:
        def bill = new Person(name:'Bill')
        def steve = new Person(name:'Steve')
        [bill, steve]*.save(flush:true)

        def query = "{ persons(singleByNameLike: [\"$bill.name\",\"$steve.name\"],id:[\"${toID('Person', steve.id)}\",\"${toID('Person', steve.id)}\"]) { name }}"

        when:
        def result = service.query(query)

        then:
        result.errors == []
        result.data?.persons[0]?.name == bill.name
        result.data?.persons[1]?.name == steve.name
    }

    def "Test static 'named query'"() {
        given:
        def bill = new Person(name:'Bill', age: 10)
        def steve = new Person(name:'Steve', age: 10)
        [steve, bill]*.save(flush:true)

        def query = "{ peopleNamedBill { id name } }"
        def query2 = "{ personsGtAge { id name } }"

        when:
        def result = service.query(query)
        def result2 = service.query(query2)

        then:
        result.errors == []
        result.data?.peopleNamedBill?.id == toID('Person', bill.id)
        result.data?.peopleNamedBill?.name == bill.name
    }
}