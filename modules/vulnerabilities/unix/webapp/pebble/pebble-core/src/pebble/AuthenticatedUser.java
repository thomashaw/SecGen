/*
 * Copyright (c) 2003-2004, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pebble;

import java.io.Serializable;

/**
 * Represents an authenticated user.
 *
 * @author    Simon Brown
 */
public class AuthenticatedUser implements Serializable {

  private String name;
  private boolean pebbleAdmin = false;
  private boolean blogOwner = false;
  private boolean blogContributor = false;

  /**
   * Default, no args constructor.
   */
  public AuthenticatedUser() {
  }

  /**
   * Gets the name of this user.
   *
   * @return    the name as a String
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this user.
   *
   * @param name    the name as a String
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Determines whether this user is a Pebble admin user.
   *
   * @return  true if the user is a Pebble admin, false otherwise
   */
  public boolean isPebbleAdmin() {
    return this.pebbleAdmin;
  }

  /**
   * Sets whether this user is a Pebble admin user.
   *
   * @param pebbleAdmin   true if the user is a Pebble admin, false otherwise
   */
  public void setPebbleAdmin(boolean pebbleAdmin) {
    this.pebbleAdmin = pebbleAdmin;
  }

  /**
   * Determines whether this user is a blog owner.
   *
   * @return  true if the user is a blog owner, false otherwise
   */
  public boolean isBlogOwner() {
    return this.blogOwner;
  }

  /**
   * Sets whether this user is a blog owner.
   *
   * @param blogOwner   true if the user is a blog owner, false otherwise
   */
  public void setBlogOwner(boolean blogOwner) {
    this.blogOwner = blogOwner;
  }

  /**
   * Determines whether this user is a blog contributor.
   *
   * @return  true if the user is a blog contributor, false otherwise
   */
  public boolean isBlogContributor() {
    return this.blogContributor;
  }

  /**
   * Sets whether this user is a blog owner.
   *
   * @param blogContributor   true if the user is a blog owner, false otherwise
   */
  public void setBlogContributor(boolean blogContributor) {
    this.blogContributor = blogContributor;
  }

}
