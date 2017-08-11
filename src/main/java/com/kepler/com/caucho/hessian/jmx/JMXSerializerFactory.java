/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc.  All rights reserved.
 *
 * The Apache Software License, Version 1.1
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
 *        Caucho Technology (http://www.caucho.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Burlap", "Resin", and "Caucho" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    info@caucho.com.
 *
 * 5. Products derived from this software may not be called "Resin"
 *    nor may "Resin" appear in their names without prior written
 *    permission of Caucho Technology.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Scott Ferguson
 */

package com.kepler.com.caucho.hessian.jmx;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.kepler.com.caucho.hessian.io.AbstractSerializerFactory;
import com.kepler.com.caucho.hessian.io.Deserializer;
import com.kepler.com.caucho.hessian.io.HessianProtocolException;
import com.kepler.com.caucho.hessian.io.Serializer;
import com.kepler.com.caucho.hessian.io.StringValueDeserializer;
import com.kepler.com.caucho.hessian.io.StringValueSerializer;

/**
 * Serializers for JMX classes.
 */
public class JMXSerializerFactory extends AbstractSerializerFactory {
  /**
   * Returns the serializer for a class.
   *
   * @param cl the class of the object that needs to be serialized.
   *
   * @return a serializer object for the serialization.
   */
  public Serializer getSerializer(Class cl)
    throws HessianProtocolException
  {
    if (ObjectName.class.equals(cl)) {
      return new StringValueSerializer();
    }
    
    return null;
  }
  
  /**
   * Returns the deserializer for a class.
   *
   * @param cl the class of the object that needs to be deserialized.
   *
   * @return a deserializer object for the serialization.
   */
  public Deserializer getDeserializer(Class cl)
    throws HessianProtocolException
  {
    if (ObjectName.class.equals(cl)) {
      return new StringValueDeserializer(cl);
    }
    else if (ObjectInstance.class.equals(cl)) {
      return new ObjectInstanceDeserializer();
    }
    else if (MBeanAttributeInfo.class.isAssignableFrom(cl)) {
      return new MBeanAttributeInfoDeserializer();
    }
    else if (MBeanConstructorInfo.class.isAssignableFrom(cl)) {
      return new MBeanConstructorInfoDeserializer();
    }
    else if (MBeanOperationInfo.class.isAssignableFrom(cl)) {
      return new MBeanOperationInfoDeserializer();
    }
    else if (MBeanParameterInfo.class.isAssignableFrom(cl)) {
      return new MBeanParameterInfoDeserializer();
    }
    else if (MBeanNotificationInfo.class.isAssignableFrom(cl)) {
      return new MBeanNotificationInfoDeserializer();
    }
    /*
    else if (MBeanInfo.class.equals(cl)) {
      return new MBeanInfoDeserializer();
    }
    */
    
    return null;
  }
}
