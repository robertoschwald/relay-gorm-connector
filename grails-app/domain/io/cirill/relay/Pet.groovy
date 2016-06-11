package io.cirill.relay

import io.cirill.relay.annotation.*

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
