package io.cirill.relay

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.*
import io.cirill.relay.dsl.GQLFieldSpec

/**
 * Created by mcirillo on 2/15/16.
 */
@RelayType
class Pet {

    static constraints = {
        owner nullable: true
    }

    static relayRoots = {[
            GQLFieldSpec.field {
                name 'bySpecies'
                argument {
                    name 'species'
                    type SchemaProvider.GLOBAL_ENUM_RESOLVE[Species]
                    nullable false
                }
                type {
                    list {
                        ref 'Pet'
                    }
                }
                dataFetcher new BySpeciesDataFetcher()
            }
    ]}

    @RelayEnum
    public enum Species {
        Cat,
        Dog
    }

    @RelayField
    String name

    @RelayField
    Person owner

    @RelayField
    Species species

    static class BySpeciesDataFetcher implements DataFetcher {
        @Override
        Object get(DataFetchingEnvironment env) {
            return findAllBySpecies(env.arguments.species as Species)
        }
    }
}
