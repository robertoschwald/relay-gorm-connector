package io.cirill.relay

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLEnumType
import io.cirill.relay.annotation.RelayEnum
import io.cirill.relay.annotation.RelayField
import io.cirill.relay.annotation.RelayQuery
import io.cirill.relay.annotation.RelayType
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
                    nonNull enumResolve[Species] as GraphQLEnumType
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
