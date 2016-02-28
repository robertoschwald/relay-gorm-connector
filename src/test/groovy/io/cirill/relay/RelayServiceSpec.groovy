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
    Relay relay = new Relay()
    def toID = { type, id -> relay.toGlobalId(type as String, id as String) }

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
        result.data != null
        result.data.person.id == id
        result.data.person.id == result2.data.person.id
        result.data.person.name == result2.data.person.name
    }

    def "Test pet custom argument and enum"() {
        given:
        def cal = new Pet(name:'Cal', species: Species.Cat)
        cal.save(flush:true)

        def id = toID('Pet', cal.id)
        def query = "{ pet(id: \"$id\") { id name species } }"

        when:
        def result = service.query(query)
        def data = result.data.pet

        then:
        data.name == cal.name
        data.species as Species == cal.species
    }

    def "Get nested field data"() {
        given:
        def bill = new Person(name:'Bill')
        def steve = new Person(name:'Steve', bestFriend: bill)
        [bill, steve]*.save(flush:true)

        def id = toID('Person', steve.id)
        def query = """
{
    node(id: \"$id\") {
        ... on Person {
            bestFriend {
                name
            }
        }
    }
}
"""
        when:
        def result = service.query(query)

        then:
        result.data?.node?.bestFriend?.name == bill.name
    }
}