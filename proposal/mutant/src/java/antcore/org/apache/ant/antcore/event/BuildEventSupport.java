/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.ant.antcore.event;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import org.apache.ant.antcore.model.ModelElement;

/**
 * BuildEventSupport is used by classes which which to send build events to
 * the BuildListeners
 *
 * @author <a href="mailto:conor@apache.org">Conor MacNeill</a>
 * @created 15 January 2002
 */
public class BuildEventSupport {
    /**
     * The listeners attached to the object which contains this support
     * object
     */
    private List listeners = new ArrayList();

    /**
     * Gets the listeners of the BuildEventSupport
     *
     * @return the listeners value
     */
    public Iterator getListeners() {
        return listeners.iterator();
    }

    /**
     * Add a listener
     *
     * @param listener the listener to be added
     */
    public void addBuildListener(BuildListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     *
     * @param listener the listener to be removed
     */
    public void removeBuildListener(BuildListener listener) {
        listeners.remove(listener);
    }

    /**
     * Forward the given event to the subscibed listeners
     *
     * @param event the event to be forwarded to the listeners
     */
    public void forwardEvent(BuildEvent event) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            BuildListener listener = (BuildListener)i.next();

            listener.processBuildEvent(event);
        }
    }

    /**
     * Fire a build started event
     *
     * @param element the build element with which the event is associated
     */
    public void fireBuildStarted(ModelElement element) {
        BuildEvent event = new BuildEvent(element, BuildEvent.BUILD_STARTED);
        forwardEvent(event);
    }

    /**
     * Fir a build finished event
     *
     * @param element the build element with which the event is associated
     * @param cause an exception if there was a failure in the build
     */
    public void fireBuildFinished(ModelElement element,
                                  Throwable cause) {
        BuildEvent event = new BuildEvent(element, BuildEvent.BUILD_FINISHED,
            cause);
        forwardEvent(event);
    }

    /**
     * fire a target started event
     *
     * @param element the build element with which the event is associated
     */
    public void fireTargetStarted(ModelElement element) {
        BuildEvent event = new BuildEvent(element, BuildEvent.TARGET_STARTED);
        forwardEvent(event);
    }

    /**
     * fire a target finished event
     *
     * @param element the build element with which the event is associated
     * @param cause an exception if there was a failure in the target's task
     */
    public void fireTargetFinished(ModelElement element,
                                   Throwable cause) {
        BuildEvent event = new BuildEvent(element, BuildEvent.TARGET_FINISHED,
            cause);
        forwardEvent(event);
    }

    /**
     * fire a task started event
     *
     * @param element the build element with which the event is associated
     */
    public void fireTaskStarted(ModelElement element) {
        BuildEvent event = new BuildEvent(element, BuildEvent.TASK_STARTED);
        forwardEvent(event);
    }

    /**
     * fire a task finished event
     *
     * @param element the build element with which the event is associated
     * @param cause an exception if there was a failure in the task
     */
    public void fireTaskFinished(ModelElement element,
                                 Throwable cause) {
        BuildEvent event = new BuildEvent(element, BuildEvent.TASK_FINISHED,
            cause);
        forwardEvent(event);
    }

    /**
     * Send a message event
     *
     * @param element the build element with which the event is associated
     * @param message the message to be sent
     * @param priority the priority of the message
     */
    public void fireMessageLogged(ModelElement element,
                                  String message, int priority) {
        BuildEvent event = new BuildEvent(element, message, priority);
        forwardEvent(event);
    }
}

