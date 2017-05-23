import io.cirill.relay.annotation.RelayType
import io.cirill.relay.artefact.GraphqlArtefactHandler

class RelayGormConnectorGrailsPlugin {
    def version = "2.2.5-SNAPSHOT"
    def grailsVersion = "2.0 > *"
    def pluginExcludes = [
      "**/Pet**",
      "**/Person**",
      "**gsp**",
      "**/test/**",
      "**/integration-test/**",
      "**/views/**"
    ]

    def title = "Relay Gorm Connector" // Headline display name of the plugin
    def author = "Matt Cirillo"
    def authorEmail = "misterzirillo@gmail.com"
    def description = '''\
        Translate Relay-style graphQL queries to GORM domain classes.
        '''
    def documentation = "http://grails.org/plugin/relay-gorm-connector"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    def issueManagement = [ system: "GITHUB", url: "https://github.com/mrcirillo/relay-gorm-connector/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/mrcirillo/relay-gorm-connector" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def artefacts = [new GraphqlArtefactHandler()]

    def watchedResources = [
      "file:./grails-app/domain/*.groovy",
      'file:./grails-app/domain/**/*.groovy',
      "file:./grails-app/graphql/**/*Graphql.groovy",
      "file:./plugins/*/graphql/**/*Graphql.groovy"
    ]

    def doWithSpring = {
        application.graphqlClasses.each { graphqlClass ->
            "${graphqlClass.propertyName}"(graphqlClass.clazz) { bean ->
                bean.autowire = "byName"
            }
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        if (event.source.class == Class) {
            Class c = (Class) event.source
            if (c.isAnnotationPresent(RelayType)) {
                applicationContext.getBeansOfType(RelayService).values()*.resetGraphQL()
            }
        }
        // Register GraphQL Artifacts
        if (application.isArtefactOfType(GraphqlArtefactHandler.TYPE, event.source)) {
            def oldClass = application.getGraphqlClass(event.source.name)
            application.addArtefact(GraphqlArtefactHandler.TYPE, event.source)

            // Reload subclasses
            application.graphql.each {
                if (it?.clazz != event.source && oldClass.clazz.isAssignableFrom(it?.clazz)) {
                    def newClass = application.classLoader.reloadClass(it.clazz.name)
                    application.addArtefact(GraphqlArtefactHandler.TYPE, newClass)
                }
            }
        }
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
