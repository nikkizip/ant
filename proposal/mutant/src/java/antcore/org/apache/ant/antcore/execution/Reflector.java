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
package org.apache.ant.antcore.execution;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.ant.common.converter.ConversionException;
import org.apache.ant.common.converter.Converter;
import org.apache.ant.common.task.TaskException;

/**
 * A reflector is used to set attributes and add nested elements to an
 * instance of an object using reflection. It is the result of class
 * introspection.
 *
 * @author <a href="mailto:conor@apache.org">Conor MacNeill</a>
 * @created 19 January 2002
 */
public class Reflector {

    /**
     * AttributeSetter classes are created at introspection time for each
     * setter method a class provides and for which a conversion from a
     * String value is available.
     *
     * @author <a href="mailto:conor@apache.org">Conor MacNeill</a>
     * @created 19 January 2002
     */
    private interface AttributeSetter {
        /**
         * Set the attribute value on an object
         *
         * @param obj the object on which the set method is to be invoked
         * @param value the string representation of the value
         * @exception InvocationTargetException if the method cannot be
         *      invoked
         * @exception IllegalAccessException if the method cannot be invoked
         * @exception ExecutionException if the conversion of the value
         *      fails
         * @exception ConversionException if the string value cannot be
         *      converted to the required type
         */
        void set(Object obj, String value)
             throws InvocationTargetException, IllegalAccessException,
            ExecutionException, ConversionException;
    }

    /**
     * An element adder is used to add an instance of an element to an of an
     * object. The object being added will have been fully configured by Ant
     * prior to calling this method.
     *
     * @author <a href="mailto:conor@apache.org">Conor MacNeill</a>
     * @created 19 January 2002
     */
    private interface ElementAdder {
        /**
         * Add an object to the this container object
         *
         * @param container the object to which the element is the be added
         * @param obj an instance of the nested element
         * @exception InvocationTargetException if the method cannot be
         *      invoked
         * @exception IllegalAccessException if the method cannot be invoked
         */
        void add(Object container, Object obj)
             throws InvocationTargetException, IllegalAccessException;
    }


    /** The method used to add content to the element */
    private Method addTextMethod;

    /** the list of attribute setters indexed by their property name */
    private Map attributeSetters = new HashMap();

    /**
     * A list of the Java class or interface accetpted by each element adder
     * indexed by the element name
     */
    private Map elementTypes = new HashMap();

    /** the list of element adders indexed by their element names */
    private Map elementAdders = new HashMap();

    /**
     * Set an attribute value on an object
     *
     * @param obj the object on which the value is being set
     * @param attributeName the name of the attribute
     * @param value the string represenation of the attribute's value
     * @exception ExecutionException if the object does not support the
     *      attribute
     * @exception TaskException if the object has a problem setting the
     *      value
     */
    public void setAttribute(Object obj, String attributeName,
                             String value)
         throws ExecutionException, TaskException {
        AttributeSetter as
             = (AttributeSetter)attributeSetters.get(attributeName);
        if (as == null) {
            throw new ExecutionException("Class " + obj.getClass().getName() +
                " doesn't support the \"" + attributeName + "\" attribute");
        }
        try {
            as.set(obj, value);
        } catch (IllegalAccessException e) {
            // impossible as getMethods should only return public methods
            throw new ExecutionException(e);
        } catch (ConversionException e) {
            throw new ExecutionException(e);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof TaskException) {
                throw (TaskException)t;
            }
            throw new ExecutionException(t);
        }
    }

    /**
     * Set the method used to add content to the element
     *
     * @param addTextMethod the new addTextMethod value
     */
    public void setAddTextMethod(Method addTextMethod) {
        this.addTextMethod = addTextMethod;
    }

    /**
     * Get the type of the given nested element
     *
     * @param elementName the nested element whose type is desired
     * @return the class instance representing the type of the element adder
     */
    public Class getType(String elementName) {
        return (Class)elementTypes.get(elementName);
    }

    /**
     * Adds PCDATA to the element
     *
     * @param obj the instance whose content is being provided
     * @param text the required content
     * @exception ExecutionException if the object does not support content
     * @exception TaskException if the object has a problem setting the
     *      content
     */
    public void addText(Object obj, String text)
         throws ExecutionException, TaskException {

        if (addTextMethod == null) {
            throw new ExecutionException("Class " + obj.getClass().getName() +
                " doesn't support content");
        }
        try {
            addTextMethod.invoke(obj, new String[]{text});
        } catch (IllegalAccessException ie) {
            // impossible as getMethods should only return public methods
            throw new ExecutionException(ie);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof TaskException) {
                throw (TaskException)t;
            }
            throw new ExecutionException(t);
        }
    }

    /**
     * Add an element to the given object
     *
     * @param obj The object to which the element is being added
     * @param elementName the name of the element
     * @param value the object to be added - the nested element
     * @exception ExecutionException if the object does not support content
     * @exception TaskException if the object has a problem setting the
     *      content
     */
    public void addElement(Object obj, String elementName, Object value)
         throws ExecutionException, TaskException {
        ElementAdder ea = (ElementAdder)elementAdders.get(elementName);
        if (ea == null) {
            throw new ExecutionException("Class " + obj.getClass().getName() +
                " doesn't support the \"" + elementName + "\" nested element");
        }
        try {
            ea.add(obj, value);
        } catch (IllegalAccessException ie) {
            // impossible as getMethods should only return public methods
            throw new ExecutionException(ie);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof TaskException) {
                throw (TaskException)t;
            }
            throw new ExecutionException(t);
        }

    }

    /**
     * Determine if the class associated with this reflector supports a
     * particular nested element
     *
     * @param elementName the name of the element
     * @return true if the class supports addition of that element
     */
    public boolean supportsNestedElement(String elementName) {
        return elementAdders.containsKey(elementName);
    }

    /**
     * Add a method to the reflector for setting an attribute value
     *
     * @param m the method, obtained by introspection.
     * @param propertyName the property name the method will set.
     * @param converters A map of converter classes used to convert strings
     *      to different types.
     */
    public void addAttributeMethod(final Method m, String propertyName,
                                         Map converters) {
        final Class type = m.getParameterTypes()[0];

        if (converters != null && converters.containsKey(type)) {
            // we have a converter to use to convert the String
            // value into something the set method expects.
            final Converter converter = (Converter)converters.get(type);
            attributeSetters.put(propertyName,
                new AttributeSetter() {
                    public void set(Object obj, String value)
                         throws InvocationTargetException, ExecutionException,
                        IllegalAccessException, ConversionException {
                        Object convertedValue = converter.convert(value, type);
                        m.invoke(obj, new Object[]{convertedValue});
                    }
                });
        } else if (type.equals(String.class)) {
            attributeSetters.put(propertyName,
                new AttributeSetter() {
                    public void set(Object parent, String value)
                         throws InvocationTargetException,
                        IllegalAccessException {
                        m.invoke(parent, new String[]{value});
                    }
                });
        } else {
            try {
                final Constructor c =
                    type.getConstructor(new Class[]{java.lang.String.class});
                attributeSetters.put(propertyName,
                    new AttributeSetter() {
                        public void set(Object parent, String value)
                             throws InvocationTargetException,
                            IllegalAccessException, ExecutionException {
                            try {
                                Object newValue
                                     = c.newInstance(new String[]{value});
                                m.invoke(parent, new Object[]{newValue});
                            } catch (InstantiationException ie) {
                                throw new ExecutionException(ie);
                            }
                        }
                    });
            } catch (NoSuchMethodException nme) {
                // ignore
            }
        }
    }

    /**
     * Add an element adder method to the list of element adders in the
     * reflector
     *
     * @param m the adder method
     * @param elementName The name of the element for which this adder works
     */
    public void addElementMethod(final Method m, String elementName) {
        final Class type = m.getParameterTypes()[0];
        elementTypes.put(elementName, type);
        elementAdders.put(elementName,
            new ElementAdder() {
                public void add(Object container, Object obj)
                     throws InvocationTargetException, IllegalAccessException {
                    m.invoke(container, new Object[]{obj});
                }
            });
    }
}

