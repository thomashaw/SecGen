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
package pebble.comparator;

import java.util.Comparator;

import pebble.logging.CountedUrl;
import pebble.logging.CountedUrl;

/**
 * A comparator used to order CountedUrl instances, in reverse order
 * of the count, and then alphabetically if instances have the same count.
 *
 * @author    Simon Brown
 */
public class CountedUrlComparator implements Comparator {

  /**
   * Compares two objects.
   *
   * @param o1  object 1
   * @param o2  object 2
   * @return  -n, 0 or +n if the count of the second visited URL is less than,
   *          the same as or greater than the first, respectively. However, if
   *          the counts are the same, then -n, 0 or +n is returned if the name
   *          of the first is less than, the same as or greater than the second
   */
  public int compare(Object o1, Object o2) {
    CountedUrl r1 = (CountedUrl)o1;
    CountedUrl r2 = (CountedUrl)o2;

    if (r1.getCount() != r2.getCount()) {
      return r2.getCount() - r1.getCount();
    } else {
      return r1.getName().compareTo(r2.getName());
    }
  }

}