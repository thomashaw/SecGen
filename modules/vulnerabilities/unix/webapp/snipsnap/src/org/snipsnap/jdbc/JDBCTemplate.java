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

package org.snipsnap.jdbc;

import org.snipsnap.util.ConnectionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Encapsulates JDBC calls into a template method like in Spring (good idea!)
 *
 * @author stephan
 * @version $Id: JDBCTemplate.java,v 1.2 2003/12/11 13:36:54 leo Exp $
 */

public class JDBCTemplate {
  private DataSource ds;
  private PreparedStatementSetter setter;

  public JDBCTemplate(DataSource ds) {
    this.ds = ds;
  }

  public void query(String query, RowCallbackHandler handler, PreparedStatementSetter setter) {
    this.setter = setter;
    query(query, handler);
  }

  public int update(String query) {
    PreparedStatement statement = null;
    ResultSet result = null;
    Connection connection = null;
    int count = -1;

    try {
      connection = ds.getConnection();
      statement = connection.prepareStatement(query);
      if (null != setter) {
        setter.setValues(statement);
      }
      count = statement.executeUpdate();
      // connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException("JDBCTemplate: unable to execute query", e);
    } finally {
      ConnectionManager.close(result);
      ConnectionManager.close(statement);
      ConnectionManager.close(connection);
    }
    return count;
  }

  public int update(String query, PreparedStatementSetter setter) {
    this.setter = setter;
    return update(query);
  }

  public void query(String query, RowCallbackHandler handler) {
    PreparedStatement statement = null;
    ResultSet result = null;
    Connection connection = null;

    try {
      connection = ds.getConnection();
      statement = connection.prepareStatement(query);
      if (null != setter) {
        setter.setValues(statement);
      }

      result = statement.executeQuery();
      while (result.next()) {
        handler.processRow(result);
      }

    } catch (SQLException e) {
      throw new RuntimeException("JDBCTemplate: unable to execute query", e);
    } finally {
      ConnectionManager.close(result);
      ConnectionManager.close(statement);
      ConnectionManager.close(connection);
    }

  }
}
