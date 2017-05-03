# relay-gorm-connector (Grails 2.x)

## Capabilities
The purpose of this plugin is to easily translate Grails ORM domain classes into a GrahphQL schema that is compatible
with Relay.js. Under the hood `graphql-java` (https://github.com/graphql-java/graphql-java) is being used dynamically
to generate the schema.

With this plugin a Grails app can serve a modern React/Relay frontend! Following Relay philosophy, the resulting GraphQL
schema is described alongside the data that it relates to using relatively little code and an expressive DSL.

<b>This plugin does not transpile frontend code.</b>

## Limitations & Roadmap
There are some unsupported GraphQL features still. Here they are in order of descending importance.

1. Configurable `ExecutionStrategy` for data fetching
2. Custom Union and Interface GraphQL types
3. Relay2 support. Relay2 is still in the pipes at Facebook but we will have a new library soon. Due to this plugin's
dependency on `graphql-java` some upstream work may be necessary as well.

## Getting Started
### Requirements
* Grails >= 3.0
* Some knowledge of GraphQL/Relay https://facebook.github.io/relay/
* Some knowledge of GORM http://docs.grails.org/latest/guide/GORM.html

### Installation
This plugin is not available (yet) in the official Grails plugin repo. The best way to make this plugin available to
a local Grails project is to clone this repo and build it.

```bash
$ git clone https://github.com/mrcirillo/relay-gorm-connector.git
$ cd relay-gorm-connector
$ git checkout grails2
$ ./grailsw compile
$ ./grailsw maven-install
```

Note: If you want to use the plugin with Java 1.7 (e.g. if you run Grails < 2.5.5), you must use Java 7 to compile / package the plugin. 
On OSX this can be achived by performing:
```
export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
export PATH=${JAVA_HOME}/bin:$PATH
java -version
```

Add the plugin as a `compile` dependency in your Grails project.

```groovy
dependencies {
    compile 'io.cirill:relay-gorm-connector:1.2.5' // TODO: Separate versioning for Grails2 variant
}
```

### Creating a Type
Marking your domain classes to be used with GraphQL is easy. A GraphQL type will be created for any classes in the
./grails-app/domains source root that has the `@RelayType` annotation. Fields on the domain class that are accessible
to GraphQL are marked with the `@RelayField` annotation.

```groovy
// person.groovy
@RelayType(description='An optional description of a person')
class Person {

    @RelayField(description='An optional description of name')
    String name

    Date dateCreated // no annotation? hidden from GraphQL
}
```

The type's name is the name of the domain class ("Person" in the example). There are restrictions fields that can be
annotated with `@RelayField`. The types are as follows:

1. Java primitives, specifically
  1. `int`
  2. `long`
  3. `boolean`
  4. `float`
  5. `short`
2. `String`
3. `BigInteger`
4. `BigDecimal`
5. `List<@RelayType>`
6. Any other `@RelayType`

## Usage
Because this plugin is designed to support Relay, the Relay node interface is automatically implemented by every type
you make. After annotating your domain class you can test it using this interface and the `RelayService` service bean.

```groovy
@TestMixin(GrailsUnitTestMixin)
@TestFor(RelayService)
@Mock([Person])
class PersonSpec extends Specification {
    void "test Ralph"() {
	    when:
	    def ralph = new Person(name: "Ralph")
	    ralph.save(flush: true)

	    def idString = RelayHelpers.toGlobalId("Person", ralph.id.toString())
	    def query = """
	        query {
	            node(id: "$idString") {
	                ... on Person {
	                    name
	                }
	            }
	        }
	    """
	    def result = service.query(query, null, [:])

        then:
        result.data.node.name == ralph.name
        /* result.data is an object representing the resolved query:
         * "node": {
         *     "name": "Ralph"
         * }
         */
    }
}
```
The query string above uses the `node` query to retrieve `ralph`. This will work for any instance of any Relay type you
specify thanks to the node interface.

### RelayService and the GraphQL endpoint
Outside of tests the `RelayService` is available by Grails convention `def relayService` on artefacts supporting
dependency injection. Naturally, you will want to create a controller to pass queries to the service. Unless you have,
customized the Relay network layer, Relay expects this endpoint to be `/graphql` with respect to the application.

```groovy
class GraphqlController {

    def relayService

    def index() {
        String query = request.JSON.query.toString()
	    Map vars = request.JSON.variables
	    def result = relayService.query(query, null, vars)

	    render(result as JSON)
    }
}
```

The endpoint can now be queried using the node interface, however a Relay application will need at least one root query
to get started.

## Advanced Usage
### Root Queries
Root queries are defined inline on domain classes using a static closure with the `@RelayQuery` annotation. The closure
should return a `GraphQLFieldDefinition`. You can use a builder from `graphql-java` or this project's DSL for
creating the object. These examples will use the DSL.

```groovy
@RelayType
class Person {

    @RelayQuery
    static one = {
        io.cirill.relay.dsl.GQLFieldSpec.field {
            name "justReturnsOne"
            type graphql.Scalars.GraphQLInt
            dataFetcher { env -> 1 } // just returns 1
        }
    }

    // ...
}
```

What is happening here? `one` is a closure that returns a `GraphQLFieldDefinition`, as stated before. Classes in the
`dsl` package are your entry point to the GraphQL DSL. They each have one static method allowing for the creation of
the respective object. In this case we used `GQLFieldSpec.field()`, which takes the a closure as a parameter.

Inside the field closure we specify the `name` of the query as it will appear in an actual query, the
`type`, and the `dataFetcher` that will actually fetch the data to fulfill the query. <b>The
type of the query needs to match what is returned by the dataFetcher.</b>

`query { justReturnsOne }` yields `{ "data": { "justReturnsOne": 1 } }`

#### DataFetchers & Types
In the previous example our DataFetcher just returns a static `1`. This is not useful. Root queries typically return
user defined types or lists of user defined types. Let's implement a more interesting query:

```groovy
GQLFieldSpec.field {
    name "theFirstPerson"
    type {
        ref "Person"
    }
    dataFetcher { env -> Person.first() }
}
```
Above: `type` is a closure where `ref "Person"` references the type that we created by annoting `Person` with
`@RelayQuery`. The `DataFetcher` is defined as a closure that uses a Grails default finder method to return
a `Person` groovy object. `type` can also specify a GraphQL list:

```groovy
GQLFieldSpec.field {
    name "allPeople"
    type {
        list {
            ref "Person"
        }
    }
    dataFetcher { env -> Person.findAll() }
}
```

The same works to create non-null GraphQL types:

`type { nonNull { ref "Person" } }` as well as `type { list { nonNull { ref "Person" } } }`

#### Arguments
Root queries commonly include arguments to dynamically find results. The relay `node` interface uses a single argument,
`id`, to fetch by ID. Here is a similar field definition that we could use to find a person by name:

```groovy
GQLFieldSpec.field {
    name "personByName"
    type { ref "Person" }
    argument {
        name "name"
        type Scalars.GraphQLString
    }
    dataFetcher { env -> Person.findByName(env.arguments.name) }
}
```

Now we issue the query `query { personByName(name: "Ralph") { id, name } }` to give Relay info about our buddy Ralph!

### Enum Types
GraphQL supports `enum` types. Mark an `enum` definition on a domain class with `@RelayEnum` and it will be added to the
GraphQL schema. The containing class needs to have `@RelayType`.

```groovy
@RelayType
class Person {

    @RelayEnum
    enum Status {
        Single,
        Married,
        ItsComplicated
    }

    @RelayField
    Status status
}
```

Enums can be used as arguments or as fields. *Note:* due to a limitation in `graphql-java` enum types can not be
referenced with the DSL `ref` clause. As a workaround, a map is available to look up any enum type that has been
parsed by the application:

```groovy
// ...
argument {
    name 'status'
    type {
        nonNull enumResolve[Status] as GraphQLEnumType
    }
}
```


### RelayProxyFields
What if you want to add a field to your Relay type that doesn't have a GraphQL equivalent? A good example is a `Date` field.
GORM classes can save a `Date` type to the database, but marking it with `@RelayField` will result in an error. You can create
a 'proxy' to deal with this using `@RelayProxyField`.

```groovy
@RelayType
class Person {

	Date dateCreated

	@RelayProxyField
	static dateProxy = {
		GQLFieldSpec.field {
			name 'dateCreatedMs'
			description 'Date comment was created in ms from epoch'
			type Scalars.GraphQLLong
			dataFetcher { env ->
				(env.source as Comment).dateCreated.getTime() // get the milliseconds
			}
		}
	}
	
	// ...
}
```

Relay can now query `query { personByName(name: "Ralph") { id, name, dateCreatedMs } }` to get information about when Ralph
was added to the database. The proxy field is useful for transforming data into something more serializable or calculating
a value that only the frontend application would care about.

### Connections
Connections are defined as static closure as well, this time with the `@RelayConnection` annotation. At a minimum, `edgeType`
must be specified (it works just like `type`s we've done before) and `connectionFor` is required to locate
the actual data.

```groovy
@RelayType
class Person {

	static hasMany = [ friends: Person ]

	@RelayConnection(connectionFor="friends")
	static friendsConnection = {
		GQLConnectionTypeSpec.connectionType {
			name "Friends"
			description "A persons friends"
			edgeType {
				ref "Person"
			}
		}
	}
}
```

### Mutations
Mutations are also a static closure. Use `@RelayMutation` and `GQLMutationSpec.field`. 

```groovy
@RelayMutation
static relayMutations = {
	GQLMutationSpec.field {
		name 'addFriend'
		
		inputType {
			name "AddFriendInput"
			field {
				name 'frienderId'
				description 'The person (ID) making the request'
				type {
					nonNull Scalars.GraphQLID
				}
			}
			field {
				name 'friendeeId'
				description 'The person (ID) being requested'
				type {
					nonNull Scalars.GraphQLString
				}
			}
		}
		
		type {
			name 'AddFriendPayload'
			field {
				name 'friender'
				type { ref 'Person' }
			}
			field {
				name 'newFriendEdge'
				type {
					name 'NewFriendEdge'
					field {
						name 'cursor'
						type Scalars.GraphQLString
					}
					field {
						name 'node'
						type {
							ref 'Person'
						}
					}
				}
			}
			field {
				name 'clientMutationId'
				type Scalars.GraphQLString
			}
		}
		dataFetcher new AddFriendMutation()
	}
}
```
An explanation of the above:
* `inputType` describes a Relay-compatible input object type. This information will be sent from the frontend application to
the backend.
* `type` describes what is sent back to the frontend when the mutation is fulfilled. Here a type is defined inline that is
called "AddFriendPayload". Because we mutated a connection, Relay expects to recieve back the owner of the connection 
(`friender`) _and_ information for a newly created edge _and_ the `clientMutationId`.
* Finally, `dataFetcher` is an implementation of the `DataFetcher` interface. Sometimes it is best to implement the interface
instead of using a Closure as a dataFetcher because the `this` context of a Closure is manipulated for the purposes of this
DSL.


```groovy
static class AddFriendMutation implements DataFetcher {
	@Override
	Object get(DataFetchingEnvironment env) {
	
		// we are guaranteed to have these values per "nonNull"
		String frienderId = RelayHelpers.fromGlobalId(env.arguments.input.frienderId)
		String friendeeId = RelayHelpers.fromGlobalId(env.arguments.input.friendeeId)
		
		// use grails finders to get the actual objects
		Person friender = Person.findById(frienderId)
		Person friendee = Person.findById(friendeeId)

		// this is an unhealthy friendship so only friender is being updated with a new friend
		friender.friends.add friendee
		friender.save()

		// get a cursor to return with the payload
		def connection = new SimpleListConnection(friender.friends as List)
		def cursor = connection.cursorForObjectInConnection(friendee)

		return [
			newFriendEdge : [
				cursor : cursor.value,
				node : friendee
			],
			friender : friender,
			clientMutationId : env.arguments.input.clientMutationId
		]
	}
}
```
Note that the DataFetcher returns an object whose structure mimics what was defined by `type`.

<b>Pro tip:</b> Adding as much code as we've written above to a simple domain class can seriously clutter your code.
Move these static fields to a Groovy `trait` instead.