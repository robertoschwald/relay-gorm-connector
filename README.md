#relay-gorm-connector
The purpose of this plugin is to easily translate Grails ORM domain classes into a GrahphQL schema that is compatible
with Relay.js. Under the hood `graphql-java` (https://github.com/graphql-java/graphql-java) is being used dynamically
to generate the schema.

##Getting Started
###Requirements
* Grails >= 3.0
* Some knowledge of GraphQL/Relay https://facebook.github.io/relay/
* Some knowledge of GORM http://docs.grails.org/latest/guide/GORM.html

###Installation
This plugin is not available (yet) in the official Grails plugin repo. The best way to make this plugin available to
a local grails project is to clone this repo and build it.

```bash
$ git clone https://github.com/mrcirillo/relay-gorm-connector.git
$ cd relay-gorm-connector
$ ./gradlew publishToMavenLocal
```

Add the plugin as a `compile` dependency in your Grails project `build.gradle`.

```groovy
dependencies {
    compile 'io.cirill:relay-gorm-connector:0.4.0'
}
```

###Creating a Type
Marking your domain classes to be used with GraphQL is easy. A GraphQL type will be created for any classes in the
./grails-app/domains source root that has the `@RelayType` annotation. Fields of the domain class that should be
accessible by GraphQL are marked with the `@RelayField` annotation.

```groovy
// person.groovy
@RelayType(description='An optional description of a person')
class Person {

    @RelayField(description='An optional description of name')
    String name

    Date dateCreated // hidden from GraphQL
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

##Usage
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

###RelayService and the GraphQL endpoint
Outside of tests the `RelayService` is available by Grails convention `def relayService` on artefacts supporting
dependency injection. Naturally, you will want to create a controller to pass queries to the service. Relay expects
this endpoint to be `/graphql` with respect to the application (unless you have customized the relay network layer).

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

A Relay application can operate with just the node query, perhaps by embedding a valid GraphQL ID into the page, but
most applications will use a "root query" to seed their Relay application with data.

##Advanced Usage
###Root Queries
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

Inside the field closure we specify the `name` (`String`) of the query as it will appear in an actual query, the
`type` (`GraphQLOutputType`), and the `dataFetcher` that will actually fetch the data to fulfill the query. <b>The
type of the query needs to match what is returned by the dataFetcher.</b>

`query { justReturnsOne }` yields `{ "data": { "justReturnsOne": 1 } }`

####DataFetchers & Types
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
Above: `type` is a closure where `ref "Person"` references the person type that we created by annoting `Person` with
`@RelayQuery`. The `DataFetcher` is defined as a closure that uses a Grails default finder method to return
a `Person` groovy object.

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

This time we wrapped the Person reference in a list! The same works to create non-null GraphQL types:

`type { nonNull { ref "Person" } }` as well as `type { list { nonNull { ref "Person" } } }`

####Arguments
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