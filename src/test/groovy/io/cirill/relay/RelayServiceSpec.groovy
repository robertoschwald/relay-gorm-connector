package io.cirill.relay

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*
import graphql.relay.Relay

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RelayService)
@Mock([Person, Pet])
class RelayServiceSpec extends Specification {

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

        def id = new Relay().toGlobalId('Person', bill.id as String)
        def query = "{ node(id: \"$id\") { id } }"
        def query2 = "{ Person(name: \"$bill.name\") { id } }"

        when:
        def result = service.query(query)
        def result2 = service.query(query2)

        then:
        result.data != null
        result.data.node.id == id
        result.data.node.id == result2.data.Person.id
    }
}