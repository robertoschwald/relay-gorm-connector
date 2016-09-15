package io.cirill.relay

import io.cirill.relay.annotation.*

@RelayType(description = 'A person')
class Person {

    String notARelayField

    static constraints = {
        notARelayField nullable: true
    }

    @RelayField(description = 'A person\'s name')
    String name

    @RelayField
    int age

    @RelayField
    Person bestFriend

    @RelayField
    List<Pet> pets

    @RelayQuery(pluralName = 'findByNameWithAges')
    static Person findByNameWithAge(
            @RelayArgument(name = 'name') String name,
            @RelayArgument(name = 'age') int age
    ) {
        findAllByNameIlike(name).find { it.age == age }
    }

    @RelayMutation(output = ['id'])
    static def addPerson(
            @RelayMutationInput(name = 'name') String name,
            @RelayMutationInput(name = 'age') int age
    ) {
        def person
        withTransaction { status ->
            person = new Person(name: name, age: age)
            person.save()
        }
    }
}
