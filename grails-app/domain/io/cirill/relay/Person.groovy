package io.cirill.relay

import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */

@RelayType(description = 'A person')
class Person {

    @RelayField(description = 'A person\'s name')
    String name

    @RelayField
    int age

    String notRelayField

    @RelayField
    @RelayArgument(description = 'Best friend\'s id')
    Person bestFriend

    @RelayField
    List<Pet> pets

}
