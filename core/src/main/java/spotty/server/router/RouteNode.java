/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty.server.router;

import spotty.common.http.HttpMethod;
import spotty.common.router.route.RouteEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;

/**
 * Represents a node in the Trie-based router.
 * Each node stores information about a specific character in a path.
 */
class RouteNode {
    // The character key of the node
    final char key;

    // The normalized path associated with the node
    String pathNormalized;

    // Map of route handlers associated with HTTP methods and accept types
    Map<HttpMethod, Map<String, RouteEntry>> handlers = emptyMap();

    // Flag indicating whether the node represents a route
    boolean isRoute;

    // Parent node reference
    RouteNode parent;

    // Map of child nodes indexed by character keys
    Map<Character, RouteNode> children = emptyMap();

    RouteNode(char key) {
        this.key = key;
    }

    /**
     * Adds a child node to the current node.
     *
     * @param node The child node to be added
     */
    void addChild(RouteNode node) {
        if (children.equals(emptyMap())) {
            children = new HashMap<>();
        }

        node.parent = this;
        children.put(node.key, node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteNode node = (RouteNode) o;
        return key == node.key &&
            isRoute == node.isRoute &&
            Objects.equals(pathNormalized, node.pathNormalized) &&
            Objects.equals(handlers, node.handlers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, isRoute, pathNormalized, handlers);
    }

    @Override
    public String toString() {
        return "[key=" + key + ",path=" + pathNormalized + ",isRoute=" + isRoute + "]";
    }
}
