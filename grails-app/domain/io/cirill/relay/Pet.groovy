package io.cirill.relay

import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */
@RelayType
class Pet {

    static constraints = {}

    @RelayType
    public enum Species {
        @RelayField
        Cat,

        @RelayField
        Dog,
    }

    @RelayField
    @RelayArgument(nullable = true)
    String name

//    @RelayField
//    @RelayArgument
//    Person owner

    @RelayField
    Species species

    @RelayArgument
    Pet singleByNameLike(String name) {
        findByNameIlike(name)
    }
}
