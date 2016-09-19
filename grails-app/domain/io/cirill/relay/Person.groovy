package io.cirill.relay

import graphql.Scalars
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.*
import io.cirill.relay.dsl.GQLFieldSpec

@RelayType(description = 'A person')
class Person {

    String notARelayField

    static constraints = {
        notARelayField nullable: true
    }

    static relayRoots = {[
            GQLFieldSpec.field {
                name 'findByNames'
                type {
                    list {
                        ref 'Person'
                    }
                }
                argument {
                    name 'name'
                    type {
                        list Scalars.GraphQLString
                    }
                    nullable false
                }
                dataFetcher new FindByNamesDataFetcher()
            }
    ]}

    @RelayField(description = 'A person\'s name')
    String name

    @RelayField
    int age

    @RelayField
    Person bestFriend

    @RelayField
    List<Pet> pets

    @RelayMutation(output = ['id'])
    static def addPerson(
            @RelayMutationInput(name = 'name') String name,
            @RelayMutationInput(name = 'age') int age
    ) {
        def person
        withTransaction { status ->
            person = new Person(name: name, age: age)
            person.save()
        }
    }

    static class FindByNamesDataFetcher implements DataFetcher {

        @Override
        Object get(DataFetchingEnvironment env) {
            env.arguments.name.collect {
                findByName(it as String)
            }
        }
    }
}
