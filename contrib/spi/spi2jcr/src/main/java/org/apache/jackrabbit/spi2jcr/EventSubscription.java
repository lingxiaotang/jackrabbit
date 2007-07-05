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
package org.apache.jackrabbit.spi2jcr;

import org.apache.jackrabbit.spi.EventBundle;
import org.apache.jackrabbit.spi.EventFilter;
import org.apache.jackrabbit.spi.Event;
import org.apache.jackrabbit.spi.ItemId;
import org.apache.jackrabbit.spi.NodeId;
import org.apache.jackrabbit.spi.IdFactory;
import org.apache.jackrabbit.spi.commons.EventImpl;
import org.apache.jackrabbit.spi.commons.EventBundleImpl;
import org.apache.jackrabbit.uuid.UUID;
import org.apache.jackrabbit.name.NamespaceResolver;
import org.apache.jackrabbit.name.Path;
import org.apache.jackrabbit.name.PathFormat;
import org.apache.jackrabbit.name.QName;
import org.apache.jackrabbit.name.NameFormat;
import org.apache.jackrabbit.name.IllegalNameException;
import org.apache.jackrabbit.name.UnknownPrefixException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.jcr.observation.EventListener;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * <code>EventSubscription</code> listens for JCR events and creates SPI event
 * bundles for them.
 */
class EventSubscription implements EventListener {

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(EventSubscription.class);

    /**
     * Mask for all events.
     */
    static final int ALL_EVENTS = javax.jcr.observation.Event.NODE_ADDED
            | javax.jcr.observation.Event.NODE_REMOVED
            | javax.jcr.observation.Event.PROPERTY_ADDED
            | javax.jcr.observation.Event.PROPERTY_CHANGED
            | javax.jcr.observation.Event.PROPERTY_REMOVED;

    private final List eventBundles = new ArrayList();

    private final IdFactory idFactory;

    private final SessionInfoImpl sessionInfo;

    private final NamespaceResolver nsResolver;

    EventSubscription(IdFactory idFactory, SessionInfoImpl sessionInfo) {
        this.idFactory = idFactory;
        this.sessionInfo = sessionInfo;
        this.nsResolver = sessionInfo.getNamespaceResolver();
    }

    /**
     * Adds the events to the list of pending event bundles.
     *
     * @param events the events that occurred.
     */
    public void onEvent(javax.jcr.observation.EventIterator events) {
        createEventBundle(events, false);
    }

    /**
     * @return a temporary event listener that will create local event bundles
     *         for delivered events.
     */
    EventListener getLocalEventListener() {
        return new EventListener() {
            public void onEvent(javax.jcr.observation.EventIterator events) {
                createEventBundle(events, true);
            }
        };
    }

    /**
     * @return all the pending event bundles.
     */
    EventBundle[] getEventBundles(EventFilter[] filters, long timeout) {
        EventBundle[] bundles;
        synchronized (eventBundles) {
            while (eventBundles.isEmpty()) {
                try {
                    eventBundles.wait(timeout);
                } catch (InterruptedException e) {
                    // continue
                }
            }
            bundles = (EventBundle[]) eventBundles.toArray(new EventBundle[eventBundles.size()]);
            eventBundles.clear();
        }
        // apply filters to bundles
        for (int i = 0; i < bundles.length; i++) {
            List filteredEvents = new ArrayList();
            for (Iterator it = bundles[i].getEvents(); it.hasNext(); ) {
                Event e = (Event) it.next();
                // TODO: this is actually not correct. if filters are empty no event should go out
                if (filters == null || filters.length == 0) {
                    filteredEvents.add(e);
                } else {
                    for (int j = 0; j < filters.length; j++) {
                        if (filters[j].accept(e, bundles[i].isLocal())) {
                            filteredEvents.add(e);
                            break;
                        }
                    }
                }
            }
            bundles[i] = new EventBundleImpl(filteredEvents,
                    bundles[i].isLocal(), bundles[i].getBundleId());
        }
        return bundles;
    }

    //--------------------------------< internal >------------------------------

    private void createEventBundle(javax.jcr.observation.EventIterator events,
                                   boolean isLocal) {
        List spiEvents = new ArrayList();
        while (events.hasNext()) {
            try {
                Session session = sessionInfo.getSession();
                javax.jcr.observation.Event e = events.nextEvent();
                Path p = PathFormat.parse(e.getPath(), nsResolver);
                Path parent = p.getAncestor(1);
                NodeId parentId = idFactory.createNodeId((String) null, parent);
                ItemId itemId = null;
                Node node = null;
                switch (e.getType()) {
                    case Event.NODE_ADDED:
                        node = session.getItem(e.getPath()).getParent();
                    case Event.NODE_REMOVED:
                        itemId = idFactory.createNodeId((String) null, p);
                        break;
                    case Event.PROPERTY_ADDED:
                    case Event.PROPERTY_CHANGED:
                        node = session.getItem(e.getPath()).getParent();
                    case Event.PROPERTY_REMOVED:
                        itemId = idFactory.createPropertyId(parentId,
                                p.getNameElement().getName());
                        break;
                }
                QName nodeTypeName = null;
                QName[] mixinTypes = new QName[0];
                if (node != null) {
                    try {
                        parentId = idFactory.createNodeId(node.getUUID(), null);
                    } catch (UnsupportedRepositoryOperationException ex) {
                        // not referenceable
                    }
                    nodeTypeName = NameFormat.parse(
                            node.getPrimaryNodeType().getName(), nsResolver);
                    mixinTypes = getNodeTypeNames(
                            node.getMixinNodeTypes(), nsResolver);
                }
                Event spiEvent = new EventImpl(e.getType(), p, itemId, parentId,
                        nodeTypeName, mixinTypes, e.getUserID());
                spiEvents.add(spiEvent);
            } catch (Exception ex) {
                log.warn("Unable to create SPI Event: " + ex);
            }
        }
        String bundleId = UUID.randomUUID().toString();
        EventBundle bundle = new EventBundleImpl(spiEvents, isLocal, bundleId);
        synchronized (eventBundles) {
            eventBundles.add(bundle);
            eventBundles.notify();
        }
    }

    /**
     * Returns the qualified names of the passed node types using the namespace
     * resolver to parse the names.
     *
     * @param nt         the node types
     * @param nsResolver the namespace resolver.
     * @return the qualified names of the node types.
     * @throws IllegalNameException   if a node type returns an illegal name.
     * @throws UnknownPrefixException if the nameo of a node type contains a
     *                                prefix that is not known to <code>nsResolver</code>.
     */
    private static QName[] getNodeTypeNames(NodeType[] nt,
                                     NamespaceResolver nsResolver)
            throws IllegalNameException, UnknownPrefixException {
        QName[] names = new QName[nt.length];
        for (int i = 0; i < nt.length; i++) {
            QName ntName = NameFormat.parse(nt[i].getName(), nsResolver);
            names[i] = ntName;
        }
        return names;
    }
}
