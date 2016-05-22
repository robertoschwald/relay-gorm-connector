package io.cirill.relay

import grails.gorm.CriteriaBuilder
import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayQuery
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */

@RelayType(description = 'A person')
class Person {

    static namedQueries = {
        peopleNamedBill {
            eq('name', 'Bill')
        }
        personsGtAge { anage ->
            ge('age', anage)
        }
    }

    static constraints = {

    }

    @RelayField(description = 'A person\'s name')
    String name

    @RelayField
    int age

    //String notRelayField

    @RelayField
    Person bestFriend

//    @RelayField
//    List<Pet> pets

    @RelayQuery(pluralName = 'singleByNamesLike')
    static Person singleByNameLike(
            @RelayArgument(name = 'name') String name,
            @RelayArgument(name = 'age') int age
    ) {
        findAllByNameIlike(name).find { it.age == age }
    }

    static byCriteria() {
        def eq = "eq"
        CriteriaBuilder c = Person.createCriteria()
        c.list {
            "$eq"('name', 'Bill')
        }
    }
}
