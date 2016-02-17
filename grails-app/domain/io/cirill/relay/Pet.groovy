package io.cirill.relay

import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */
@RelayType
class Pet {

    @RelayType
    enum Species {
        @RelayField
        Cat,

        @RelayField
        Dog,

        @RelayField
        Snake
    }

    @RelayField
    @RelayArgument
    String name

    @RelayField
    @RelayArgument
    Person owner

    @RelayField
    Species species
}
