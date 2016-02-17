package io.cirill.relay

import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */

@RelayType
class Person {

    @RelayField
    String name

    @RelayField
    int age

    String notRelayField

    @RelayField
    Person bestFriend

    @RelayField
    List<Pet> pets

}
