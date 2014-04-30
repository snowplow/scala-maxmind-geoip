/*
 * Copyright (c) 2012-2013 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.maxmind.geoip

// Java
import java.io.File

// LRU
import com.twitter.util.LruMap

// MaxMind
import com.maxmind.geoip.{Location, LookupService}

// This library
import IpLocation._

/**
 * Companion object to hold alternative constructors.
 *
 */
object IpGeo {

  /**
   * Alternative constructor taking a String rather than File
   */
  def apply(dbFile: String, memCache: Boolean = true, lruCache: Int = 10000) = {
    new IpGeo(new File(dbFile), memCache, lruCache)
  }
}

/**
 * IpGeo is a Scala wrapper around MaxMind's own LookupService Java class.
 *
 * Two main differences:
 *
 * 1. getLocation(ip: String) now returns an IpLocation
 *    case class, not a raw MaxMind Location
 * 2. IpGeo introduces an LRU cache to improve
 *    lookup performance
 *
 * Inspired by:
 * https://github.com/jt6211/hadoop-dns-mining/blob/master/src/main/java/io/covert/dns/geo/IpGeo.java
 */
class IpGeo(dbFile: File, memCache: Boolean = true, lruCache: Int = 10000) {

  // Initialise the cache
  private val lru = if (lruCache > 0) new LruMap[String, Option[IpLocation]](lruCache) else null // Of type mutable.Map[String, Option[IpLocation]]

  // Configure the lookup service
  private val options = if (memCache) LookupService.GEOIP_MEMORY_CACHE else LookupService.GEOIP_STANDARD
  private val maxmind = new LookupService(dbFile, options)

  /**
   * Returns the MaxMind location for this IP address
   * as an IpLocation, or None if MaxMind cannot find
   * the location.
   */
  def getLocation = if (lruCache <= 0) getLocationWithoutLruCache _ else getLocationWithLruCache _

  /**
   * Returns the MaxMind location for this IP address
   * as an IpLocation, or None if MaxMind cannot find
   * the location.
   *
   * This version does not use the LRU cache.
   */
  private def getLocationWithoutLruCache(ip: String): Option[IpLocation] = {
    try {
      Option(maxmind.getLocation(ip)) map IpLocation.apply
    } finally {
      maxmind.close
    }
  }

  /**
   * Returns the MaxMind location for this IP address
   * as an IpLocation, or None if MaxMind cannot find
   * the location.
   *
   * This version uses and maintains the LRU cache.
   *
   * Don't confuse the LRU returning None (meaning that no
   * cache entry could be found), versus an extant cache entry
   * containing None (meaning that the IP address is unknown).
   */
  private def getLocationWithLruCache(ip: String): Option[IpLocation] = lru.get(ip) match {
    case Some(loc) => loc // In the LRU cache
    case None => // Not in the LRU cache
      val loc = getLocationWithoutLruCache(ip)
      lru.put(ip, loc)
      loc
  }
}