package io.cirill.relay

import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */

@RelayType(description = 'A person', pluralName = 'persons')
class Person {

    static constraints = {

    }

    @RelayField(description = 'A person\'s name')
    @RelayArgument(description = 'A person\'s name')
    String name

    @RelayField
    @RelayArgument(description = 'A person\'s age')
    int age

    //String notRelayField

    @RelayField
    Person bestFriend

//    @RelayField
//    List<Pet> pets

}
