/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.commons;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.commons.iterator.NodeIterable;
import org.apache.jackrabbit.commons.iterator.PropertyIterable;
import org.apache.jackrabbit.commons.iterator.RowIterable;

/**
 * Collection of static utility methods for use with the JCR API.
 *
 * @since Apache Jackrabbit 2.0
 */
public class JcrUtils {

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private JcrUtils() {
    }

    /**
     * Calls {@link Node#getSharedSet()} on the given node and returns
     * the resulting {@link NodeIterator} as an {@link Iterable<Node>} instance
     * for use in a Java 5 for-each loop.
     *
     * @see NodeIterable
     * @param node shared node
     * @return nodes in the shared set
     * @throws RepositoryException if the {@link Node#getSharedSet()} call fails
     */
    public static Iterable<Node> getSharedSet(Node node)
            throws RepositoryException {
        return new NodeIterable(node.getSharedSet());
    }

    /**
     * Calls {@link Node#getNodes()} on the given node and returns the
     * resulting {@link NodeIterator} as an {@link Iterable<Node>} instance
     * for use in a Java 5 for-each loop.
     *
     * @see NodeIterable
     * @param node parent node
     * @return child nodes
     * @throws RepositoryException if the {@link Node#getNodes()} call fails
     */
    public static Iterable<Node> getChildNodes(Node node)
            throws RepositoryException {
        return new NodeIterable(node.getNodes());
    }

    /**
     * Calls {@link Node#getNodes(String)} on the given node with the given
     * name pattern and returns the resulting {@link NodeIterator} as an
     * {@link Iterable<Node>} instance for use in a Java 5 for-each loop.
     *
     * @see NodeIterable
     * @param node parent node
     * @param pattern node name pattern
     * @return matching child nodes
     * @throws RepositoryException
     *         if the {@link Node#getNodes(String)} call fails
     */
    public static Iterable<Node> getChildNodes(Node node, String pattern)
            throws RepositoryException {
        return new NodeIterable(node.getNodes(pattern));
    }

    /**
     * Calls {@link Node#getNodes(String[])} on the given node with the given
     * name globs and returns the resulting {@link NodeIterator} as an
     * {@link Iterable<Node>} instance for use in a Java 5 for-each loop.
     *
     * @see NodeIterable
     * @param node parent node
     * @param pattern node name pattern
     * @return matching child nodes
     * @throws RepositoryException
     *         if the {@link Node#getNodes(String[])} call fails
     */
    public static Iterable<Node> getChildNodes(Node node, String[] globs)
            throws RepositoryException {
        return new NodeIterable(node.getNodes(globs));
    }

    /**
     * Calls {@link Node#getProperties()} on the given node and returns the
     * resulting {@link NodeIterator} as an {@link Iterable<Node>} instance
     * for use in a Java 5 for-each loop.
     *
     * @see PropertyIterable
     * @param node node
     * @return properties of the node
     * @throws RepositoryException
     *         if the {@link Node#getProperties()} call fails
     */
    public static Iterable<Property> getProperties(Node node)
            throws RepositoryException {
        return new PropertyIterable(node.getProperties());
    }

    /**
     * Calls {@link Node#getProperties(String)} on the given node with the
     * given name pattern and returns the resulting {@link PropertyIterator}
     * as an {@link Iterable<Property>} instance for use in a Java 5
     * for-each loop.
     *
     * @see PropertyIterable
     * @param node node
     * @param pattern property name pattern
     * @return matching properties of the node
     * @throws RepositoryException
     *         if the {@link Node#getProperties(String)} call fails
     */
    public static Iterable<Property> getProperties(Node node, String pattern)
            throws RepositoryException {
        return new PropertyIterable(node.getProperties(pattern));
    }

    /**
     * Calls {@link Node#getProperty(String[])} on the given node with the
     * given name globs and returns the resulting {@link PropertyIterator}
     * as an {@link Iterable<Property>} instance for use in a Java 5
     * for-each loop.
     *
     * @see PropertyIterable
     * @param node node
     * @param pattern property name globs
     * @return matching properties of the node
     * @throws RepositoryException
     *         if the {@link Node#getProperties(String[])} call fails
     */
    public static Iterable<Property> getProperties(Node node, String[] globs)
            throws RepositoryException {
        return new PropertyIterable(node.getProperties(globs));
    }

    /**
     * Calls {@link Node#getReferences()} on the given node and returns the
     * resulting {@link PropertyIterator} as an {@link Iterable<Property>}
     * instance for use in a Java 5 for-each loop.
     *
     * @see PropertyIterable
     * @param node reference target
     * @return references that point to the given node
     * @throws RepositoryException
     *         if the {@link Node#getReferences()} call fails
     */
    public static Iterable<Property> getReferences(Node node)
            throws RepositoryException {
        return new PropertyIterable(node.getReferences());
    }

    /**
     * Calls {@link Node#getReferences(String)} on the given node and returns
     * the resulting {@link PropertyIterator} as an {@link Iterable<Property>}
     * instance for use in a Java 5 for-each loop.
     *
     * @see PropertyIterable
     * @param node reference target
     * @param name reference property name
     * @return references with the given name that point to the given node
     * @throws RepositoryException
     *         if the {@link Node#getReferences(String)} call fails
     */
    public static Iterable<Property> getReferences(Node node, String name)
            throws RepositoryException {
        return new PropertyIterable(node.getReferences(name));
    }

    /**
     * Calls {@link Node#getWeakReferences()} on the given node and returns the
     * resulting {@link PropertyIterator} as an {@link Iterable<Property>}
     * instance for use in a Java 5 for-each loop.
     *
     * @see PropertyIterable
     * @param node reference target
     * @return weak references that point to the given node
     * @throws RepositoryException
     *         if the {@link Node#getWeakReferences()} call fails
     */
    public static Iterable<Property> getWeakReferences(Node node)
            throws RepositoryException {
        return new PropertyIterable(node.getWeakReferences());
    }

    /**
     * Calls {@link Node#getReferences(String)} on the given node and returns
     * the resulting {@link PropertyIterator} as an {@link Iterable<Property>}
     * instance for use in a Java 5 for-each loop.
     *
     * @see PropertyIterable
     * @param node reference target
     * @param name reference property name
     * @return weak references with the given name that point to the given node
     * @throws RepositoryException
     *         if the {@link Node#getWeakReferences(String)} call fails
     */
    public static Iterable<Property> getWeakReferences(Node node, String name)
            throws RepositoryException {
        return new PropertyIterable(node.getWeakReferences(name));
    }

    /**
     * Calls {@link QueryResult#getNodes()} on the given query result and
     * returns the resulting {@link NodeIterator} as an {@link Iterable<Node>}
     * instance for use in a Java 5 for-each loop.
     *
     * @see NodeIterable
     * @param result query result
     * @return nodes in the query result
     * @throws RepositoryException
     *         if the {@link QueryResult#getNodes()} call fails
     */
    public static Iterable<Node> getNodes(QueryResult result)
            throws RepositoryException {
        return new NodeIterable(result.getNodes());
    }

    /**
     * Calls {@link QueryResult#getRows()} on the given query result and
     * returns the resulting {@link RowIterator} as an {@link Iterable<Row>}
     * instance for use in a Java 5 for-each loop.
     *
     * @see RowIterable
     * @param result query result
     * @return rows in the query result
     * @throws RepositoryException
     *         if the {@link QueryResult#getRows()} call fails
     */
    public static Iterable<Row> getRows(QueryResult result)
            throws RepositoryException {
        return new RowIterable(result.getRows());
    }

}
