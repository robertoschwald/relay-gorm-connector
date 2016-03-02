package io.cirill.relay

import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayEnum
import io.cirill.relay.annotation.RelayEnumField
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */
@RelayType(pluralName = 'pets')
class Pet {

    static constraints = {}

    @RelayEnum()
    public enum Species {
        @RelayEnumField
        Cat,

        @RelayEnumField
        Dog,
    }

    @RelayField
    @RelayArgument(description = 'A pet\'s name', unique = false)
    String name

//    @RelayField
//    @RelayArgument
//    Person owner

    @RelayField
    @RelayArgument(description = 'A pet\'s species', unique = false)
    Species species
}
