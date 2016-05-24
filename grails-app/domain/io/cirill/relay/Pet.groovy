package io.cirill.relay

import io.cirill.relay.annotation.RelayArgument
import io.cirill.relay.annotation.RelayQuery
import io.cirill.relay.annotation.RelayEnum
import io.cirill.relay.annotation.RelayEnumField
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/15/16.
 */
@RelayType
class Pet {

    static constraints = {
        owner nullable: true
    }

    @RelayEnum()
    public enum Species {
        @RelayEnumField
        Cat,

        @RelayEnumField
        Dog,
    }

    @RelayField
    String name

    @RelayField
    Person owner

    @RelayField
    Species species

    @RelayQuery
    static List<Pet> bySpecies(
            @RelayArgument(name = 'species') Species species
    ) {
        findAllBySpecies(species)
    }
}
