package io.cirill.relay

import graphql.Scalars
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.cirill.relay.annotation.*
import io.cirill.relay.dsl.GQLFieldSpec
import io.cirill.relay.dsl.GQLMutationSpec

@RelayType(description = 'A person')
class Person {

    String notARelayField

    static constraints = {
        notARelayField nullable: true
    }

    @RelayQuery
    static byNamesQuery = {
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
                    list {
                        nonNull Scalars.GraphQLString
                    }
                }
            }
            dataFetcher new FindByNamesDataFetcher()
        }
    }

    @RelayMutation
    static addPersonMutation = {
        GQLMutationSpec.field {
            name 'addPerson'
            type {
                name 'AddPersonPayload'
                field {
                    name 'newPerson'
                    type {
                        ref 'Person'
                    }
                }
                field {
                    name 'clientMutationId'
                    type Scalars.GraphQLString
                }
            }
            inputType {
                name 'AddPersonInput'
                field {
                    name 'name'
                    description 'A new persons name'
                    type {
                        nonNull Scalars.GraphQLString
                    }
                }
                field {
                    name 'age'
                    description 'a new persons age'
                    type {
                        nonNull Scalars.GraphQLInt
                    }
                }
            }
            dataFetcher new AddPersonDataFetcher()
        }
    }

    @RelayField(description = 'A person\'s name')
    String name

    @RelayField
    int age

    @RelayField
    Person bestFriend

    @RelayField
    List<Pet> pets

    static class AddPersonDataFetcher implements DataFetcher {
        @Override
        Object get(DataFetchingEnvironment environment) {
            def person
            withTransaction { status ->
                person = new Person(name: environment.arguments.input.name as String, age: environment.arguments.input.age as int)
                person.save()
            }
            return [
                    newPerson : person,
                    clientMutationId : environment.arguments.input.clientMutationId
            ]
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
