package spotty.server.router

import spock.lang.Specification
import spotty.common.router.route.RouteEntry

import java.util.function.Consumer

import static spotty.common.http.HttpMethod.GET

class TrieRoutesTest extends Specification {
    private def routes = new TrieRoutes()

    def "should find route successfully"() {
        given:
        registerRoute(route)

        when:
        def foundNode = routes.findRouteNode(searchPath)
        def actual = foundNode != null

        then:
        actual == expected

        where:
        route                          | searchPath                | expected
        "/user/:id"                    | "/user/12"                | true
        "/user/:id"                    | "/user/12/"               | true
        "/user/:id"                    | "/user/12/sad"            | true
        "/user/:id/"                   | "/user/12"                | false
        "/user/:id/"                   | "/user/12/"               | true
        "/user/:id/"                   | "/user/12/sad"            | false
        "/category"                    | "/category"               | true
        "/category"                    | "/category1"              | false
        "/category"                    | "/cate"                   | false
        "/category"                    | "cate"                    | false
        "/:any"                        | "/any/path"               | true
        "/*"                           | "/any/path"               | true
        "/:any/"                       | "/any/path"               | false
        "/user/:name/category/:cat_id" | "/user/alex/category/123" | true
        "/user/:name/category/:cat_id" | "/use/alex/category/123"  | false
        "/user/:name/category/:cat_id" | "/user/alex/cat/123"      | false
        "/user/:name/category/:cat_id" | "/user/alex"              | false
        "/user/:name/category/:cat_id" | "/alex/category/123"      | false
    }

    def "should found correct route when registered a few similar routes"() {
        given:
        def name = registerRoute("/user/:name")
        def nameId = registerRoute("/user/:name/:id")
        def nameIdHello = registerRoute("/user/:name/:id/hello")

        when:
        def nameFound = routes.findRouteNode("/user/alex")
        def nameNotFound = routes.findRouteNode("/user/alex/")
        def nameIdFound = routes.findRouteNode("/user/alex/1")
        def nameIdHelloFound = routes.findRouteNode("/user/alex/1/hello")

        then:
        name == nameFound
        nameNotFound == null
        nameId == nameIdFound
        nameIdHello == nameIdHelloFound
    }

    def "should remove correct route"() {
        when:
        registerRoute("/user")
        registerRoute("/user/:id")
        registerRoute("/user/:id/")
        registerRoute("/user/:id/*")
        registerRoute("/user/:id/:name")
        registerRoute("/user/:id/:name/alex")

        then:
        routes.removeExactly("/user") == true
        routes.removeExactly("/user/") == false
        routes.removeExactly("/us") == false
        routes.removeExactly("/") == false
        routes.removeExactly("/user/*") == true
        routes.removeExactly("/user/*") == false
        routes.removeExactly("/user/*/") == true
        routes.removeExactly("/user/*/*") == true
        routes.root.children.size() > 0
        routes.removeExactly("/user/*/*/alex") == true
        routes.root.children.size() == 0
    }

    def "should remove route, but not children"() {
        given:
        registerRoute("/a")
        registerRoute("/ab")
        registerRoute("/ac")
        registerRoute("/ad")
        registerRoute("/a/q")
        registerRoute("/a/e")
        registerRoute("/a/c")

        when:
        routes.removeExactly("/a")

        then:
        {
            def node = find("/a")
            node.isRoute == false
            node.children.size() == 4
        }

        when:
        routes.removeExactly("/a/")

        then:
        {
            def node = find("/a/")
            node.isRoute == false
            node.children.size() == 3
        }

        when:
        def found = find("/a/q")
        routes.removeExactly("/a/q")

        then:
        {
            found != null
            find("/a/q") == null
            find("/a/").children.size() == 2
        }

        then:
        routes.removeExactly("/a") == false
        routes.removeExactly("/ab") == true
        routes.root.children.size() > 0
        routes.removeExactly("/ac") == true
        routes.removeExactly("/ad") == true
        routes.removeExactly("/a/q") == false
        routes.root.children.size() > 0
        routes.removeExactly("/a/e") == true
        routes.root.children.size() > 0
        routes.removeExactly("/a/c") == true
        routes.root.children.size() == 0
    }

    def "should set node as route in the middle of tree"() {
        when:
        registerRoute("/name/alex")

        then:
        find("/name").isRoute == false
        find("/name/alex").isRoute == true

        when:
        registerRoute("/name")

        then:
        find("/name").isRoute == true
    }

    def "should for each by all routes"() {
        given:
        registerRoute("/")
        registerRoute("/name")
        registerRoute("/name/:id")
        registerRoute("/name/:id/category")
        registerRoute("/name/:id/product")

        var Consumer<RouteEntry> consumer = Mock()

        when:
        routes.forEachRouteIf({ true }, consumer)

        then:
        5 * consumer.accept(_)
    }

    def "should for each by all routes by predicate"() {
        given:
        registerRoute("/")
        registerRoute("/name")
        registerRoute("/name/:id")
        registerRoute("/name/:id/category")
        registerRoute("/name/:id/product")

        var Consumer<RouteEntry> consumer = Mock()

        when:
        routes.forEachRouteIf({ it.pathTemplate().startsWith("/name/:id") }, consumer)

        then:
        3 * consumer.accept({ it.pathTemplate().startsWith("/name/:id") })
    }

    def "should not for each by any when predicate returns false"() {
        given:
        registerRoute("/")
        registerRoute("/name")
        registerRoute("/name/:id")
        registerRoute("/name/:id/category")
        registerRoute("/name/:id/product")

        var Consumer<RouteEntry> consumer = Mock()

        when:
        routes.forEachRouteIf({ false }, consumer)

        then:
        0 * consumer.accept(_)
    }

    def "should find correct route when registered a few similar"() {
        given:
        registerRoute("/hello")
        registerRoute("/hello/:name")
        registerRoute("/hello/*/world")
        registerRoute("/hello/:id/:category/world")
        registerRoute("/hello/:id/:category/world2")
        registerRoute("/api/*")
        registerRoute("/*")

        when:
        def actual = routes.findRouteNode(path)?.pathNormalized

        then:
        actual == expected

        where:
        path                         | expected
        "/hello"                     | "/hello"
        "/hello/alex"                | "/hello/*"
        "/hello/alex/world"          | "/hello/*/world"
        "/hello/123/category/world"  | "/hello/*/*/world"
        "/hello/123/category/world2" | "/hello/*/*/world2"
        "/hello/123/category/world3" | "/*"
        "/api/any"                   | "/api/*"
        "/any/path"                  | "/*"
        "/"                          | null
    }

    def "should clear all routes"() {
        given:
        registerRoute("/hello")
        registerRoute("/hello/:name")
        registerRoute("/hello/*/world")

        when:
        def routesCount = routes.toNormalizedPaths().size()

        then:
        routesCount == 3

        when:
        routes.clear()
        routesCount = routes.toNormalizedPaths().size()

        then:
        routesCount == 0
    }

    private RouteNode registerRoute(String path) {
        def route = RouteEntryFactory.create(path, GET, "*/*", { "" })
        return routes.add(route.pathNormalized(), [(GET): ["*/*": route]])
    }

    private RouteNode find(String path) {
        def node = routes.root
        for (i in 0..<path.length()) {
            def ch = path.charAt(i)
            node = node.children.get(ch)
            if (node == null) {
                return null
            }
        }

        return node
    }
}
