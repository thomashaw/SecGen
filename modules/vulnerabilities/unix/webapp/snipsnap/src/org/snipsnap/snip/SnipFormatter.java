/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
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
package org.snipsnap.snip;


import org.picocontainer.PicoContainer;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.snipsnap.app.Application;
import org.snipsnap.container.Components;
import org.snipsnap.render.context.SnipRenderContext;

import java.util.Collection;

/**
 * SnipFormatter supplies some methods for handling Snip Content.
 *
 * @author stephan
 * @version $Id: SnipFormatter.java,v 1.13 2004/05/17 10:56:18 leo Exp $
 */
public class SnipFormatter
{
	public static String toXML(Snip snip, String content)
	{
    //@FIXME: This duplicates SnipImpl.toXML()
    PicoContainer container = Components.getContainer();
    RenderEngine engine = (RenderEngine) container.getComponentInstance(Components.DEFAULT_ENGINE);
    RenderContext context = new SnipRenderContext(snip, (SnipSpace) container.getComponentInstance(SnipSpace.class));
    context.setParameters(Application.get().getParameters());
    return engine.render(content, context);
  }
}
