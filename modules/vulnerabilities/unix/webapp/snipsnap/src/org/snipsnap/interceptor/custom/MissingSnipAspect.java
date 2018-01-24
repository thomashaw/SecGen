package org.snipsnap.interceptor.custom;

/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002-2003 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */

import org.codehaus.nanning.AspectInstance;
import org.codehaus.nanning.Invocation;
import org.codehaus.nanning.MethodInterceptor;
import org.codehaus.nanning.config.Aspect;
import org.codehaus.nanning.config.P;
import org.codehaus.nanning.config.Pointcut;
import org.snipsnap.snip.SnipSpace;
import org.snipsnap.snip.Snip;
import org.snipsnap.util.ApplicationAwareMap;

import java.util.HashMap;
import java.util.Map;

/**
 * MissingLSnip Aspect caches method call result
 * from SnipSpace (exists, create).
 * When a missing snip is detected the
 * snip name is stored. When the snip is created
 * the snip is removed from the missing list.
 *
 * @author stephan
 * @version $Id: MissingSnipAspect.java,v 1.4 2003/12/11 13:24:56 leo Exp $
 */

public class MissingSnipAspect implements Aspect {
  Pointcut existsPc = P.methodName("exists.*");
  Pointcut createPc = P.methodName("create.*");
  Pointcut removePc = P.methodName("remove.*");

  private ApplicationAwareMap missing;
  private ApplicationAwareMap existing;

  public MissingSnipAspect() {
    this.missing = new ApplicationAwareMap(HashMap.class, HashMap.class);
    this.existing = new ApplicationAwareMap(HashMap.class, HashMap.class);
  }

  public void advise(AspectInstance instance) {
    Class klass = instance.getClassIdentifier();
//    System.out.println("class="+klass);
//    System.out.println("instance="+instance);
    if (klass != null && klass.equals(SnipSpace.class)) {
      existsPc.advise(instance, new MethodInterceptor() {
        public Object invoke(Invocation invocation) throws Throwable {
          String name = ((String)
              invocation.getArgs()[0]).toUpperCase();
          // Snip is in the missing list
          if (missing.getMap().containsKey(name)) {
            //System.out.println("Hit=" + name);
            return new Boolean(false);
          } else if (existing.getMap().containsKey(name)) {
            return new Boolean(true);
          }

          //System.out.println("Miss=" + name);
          Boolean result = (Boolean)
              invocation.invokeNext();
          // System.out.println("Result=" + name + " exists?=" + result);
          // The snip does not exist so put it in the missing list
          if (result.equals(Boolean.FALSE)) {
            missing.getMap().put(name, new Integer(0));
          } else {
            existing.getMap().put(name, new Integer(0));
          }
          return result;
        }
      });


      removePc.advise(instance, new MethodInterceptor() {
        public Object invoke(Invocation invocation) throws Throwable {
          Snip snip = (Snip) invocation.getArgs()[0];
          String name = snip.getName().toUpperCase();

          Object result = invocation.invokeNext();

          if (existing.getMap().containsKey(name)) {
            existing.getMap().remove(name);
          }
          return result;
        }
      });

      createPc.advise(instance, new MethodInterceptor() {
        public Object invoke(Invocation invocation) throws Throwable {
          String name = ((String) invocation.getArgs()[0]).toUpperCase();

          Object result = invocation.invokeNext();

          if (missing.getMap().containsKey(name)) {
            missing.getMap().remove(name);
          }
          return result;
        }
      });
    }
  }

  public void introduce(AspectInstance instance) {
  }
}
