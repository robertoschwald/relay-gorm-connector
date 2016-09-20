package io.cirill.relay

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.*
import io.cirill.relay.dsl.GQLFieldSpec

@RelayType
class Pet {

    static constraints = {
        owner nullable: true
    }

    @RelayQuery
    static bySpeciesRoot = {
        GQLFieldSpec.field {
            name 'bySpecies'
            argument {
                name 'species'
                type {
                    nonNull SchemaProvider.GLOBAL_ENUM_RESOLVE[Species]
                }
            }
            type {
                list {
                    ref 'Pet'
                }
            }
            dataFetcher new BySpeciesDataFetcher()
        }
    }

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
