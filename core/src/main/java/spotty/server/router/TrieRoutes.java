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

import com.google.common.annotations.VisibleForTesting;
import spotty.common.http.HttpMethod;
import spotty.common.router.route.RouteEntry;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Collections.emptyMap;

/**
 * <p>Represents a Trie Router implementation.
 * Each registered path is split into a series of nodes, with each node representing a character in the path.
 * The Trie supports wildcard shortcuts '*', which represent any path segment between slashes (/ * /).</p>
 */
class TrieRoutes {
    private static final Character PLACEHOLDER = '*';

    final RouteNode root = new RouteNode('\0');

    /**
     * Adds a route to the Trie Router.
     *
     * @param pathNormalized The normalized path to add to the router.
     * @param routeHandlers  The handlers associated with the route.
     * @return The node representing the added route.
     */
    RouteNode add(String pathNormalized, Map<HttpMethod, Map<String, RouteEntry>> routeHandlers) {
        RouteNode current = root;
        for (int i = 0; i < pathNormalized.length(); i++) {
            final char ch = pathNormalized.charAt(i);
            RouteNode node = current.children.get(ch);
            if (node == null) {
                node = new RouteNode(ch);
                current.addChild(node);
            }

            current = node;
        }

        current.isRoute = true;
        current.pathNormalized = pathNormalized;
        current.handlers = routeHandlers;

        return current;
    }

    /**
     * Finds the route node corresponding to the given raw path.
     *
     * @param rawPath The raw path for which to find the route node.
     * @return The route node corresponding to the raw path, or null if not found.
     */
    RouteNode findRouteNode(String rawPath) {
        final RouteNode node = findNode(rawPath, 0, root);
        if (node != null && node.isRoute) {
            return node;
        }

        return null;
    }

    /**
     * Finds the node in the Trie Router corresponding to the given path.
     * This method recursively traverses the Trie structure to locate the node.
     * It handles wildcard shortcuts '*' in the path by navigating to the appropriate node representing the wildcard.
     *
     * @param path    The path for which to find the corresponding node.
     * @param index   The index representing the current position in the path.
     * @param current The current node being examined during traversal.
     * @return The node in the Trie Router corresponding to the given path,
     * or null if no such node is found.
     *
     * <p>The findNode method recursively searches the Trie Router structure to locate the node
     * representing the given path. It starts from the root node and traverses the tree based on
     * the characters in the path. If the current character is not found in the children of the current node,
     * the method checks if there is a wildcard node ('*') representing any path segment between slashes.
     * If such a wildcard node exists, the method navigates to it and continues the search.</p>
     *
     * <p>For example, consider the following Trie structure:
     * <pre>
     *                root
     *                 |
     *                "/"
     *              /  |  \
     *             u   *   a
     *            / \      |
     *           s   *     t
     *          / \        |
     *         e   r       r
     *        /
     *       r
     *      /
     *    "/"
     *    /
     *   *
     * </pre>
     * </p>
     * <p>If we call findNode("/user/test", 0, root), the method will traverse the Trie as follows:</p>
     * <ol>
     * <li>Start from the root node.</li>
     * <li>Check if there is a child node 'u', proceed to it.</li>
     * <li>Check if there is a child node 's', proceed to it.</li>
     * <li>Check if there is a child node 'e', proceed to it.</li>
     * <li>Check if there is a child node 'r', proceed to it.</li>
     * <li>Reach the end of the path ("/user/test").</li>
     * <li>Return the node corresponding to the path.</li>
     * </ol>
     * <p>If the path contains wildcard segments (e.g., "/user/*"), the findNode method will
     * handle them appropriately by navigating to the corresponding wildcard node and continuing the search.</p>
     *
     * <p>The time complexity of this method is O(n), where n is the length of the path.
     * The space complexity is also O(n) due to the recursive nature of the algorithm.</p>
     */
    private RouteNode findNode(String path, int index, RouteNode current) {
        if (current == null) {
            return null;
        }

        if (index < 0 || index >= path.length()) {
            return current;
        }

        final char ch = path.charAt(index);
        RouteNode node = findNode(path, index + 1, current.children.get(ch));
        if (node == null) {
            final RouteNode nodePlaceholder = current.children.get(PLACEHOLDER);
            if (nodePlaceholder != null) {
                if (nodePlaceholder.key == '*' && nodePlaceholder.isRoute && nodePlaceholder.children.isEmpty()) {
                    return nodePlaceholder;
                }

                final RouteNode child = findNode(path, path.indexOf('/', index), nodePlaceholder);
                if (child != null) {
                    return child;
                }
            }
        }

        return node;
    }

    /**
     * Removes the route exactly matching the specified normalized path.
     *
     * @param normalizedPath The normalized path of the route to remove.
     * @return True if the route was successfully removed, false otherwise.
     */
    boolean removeExactly(String normalizedPath) {
        RouteNode node = findRouteNode(normalizedPath);
        if (node == null) {
            return false;
        }

        while (node != null && node != root) {
            node.pathNormalized = null;
            node.handlers = emptyMap();
            node.isRoute = false;

            if (node.children.isEmpty()) {
                node.parent.children.remove(node.key);
            }

            node = node.parent;
        }

        return true;
    }

    /**
     * Clears all routes from the Trie Router.
     */
    void clear() {
        root.children.clear();
    }

    /**
     * Executes the provided consumer for each route entry in the Trie Router
     * that satisfies the specified predicate.
     *
     * @param predicate The predicate to filter route entries.
     * @param consumer  The consumer to execute for each matching route entry.
     */
    void forEachRouteIf(Predicate<RouteEntry> predicate, Consumer<RouteEntry> consumer) {
        final Deque<RouteNode> queue = new LinkedList<>(root.children.values());
        while (!queue.isEmpty()) {
            final RouteNode current = queue.remove();
            queue.addAll(current.children.values());

            if (current.isRoute) {
                current.handlers
                    .values()
                    .stream()
                    .flatMap(routes -> routes.values().stream())
                    .filter(predicate)
                    .forEach(consumer);
            }
        }
    }

    @VisibleForTesting
    List<String> toNormalizedPaths() {
        final List<String> result = new ArrayList<>();
        forEachRouteIf(__ -> true, route -> result.add(route.pathNormalized()));

        return result;
    }
}
