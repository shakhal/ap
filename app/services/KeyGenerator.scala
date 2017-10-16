package services

import models.Bookmark
import scala.util.Random

object KeyGenerator {
  val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  val size = alpha.size
  val slugLength = 6;
  val sessionKeyLength = 50;

  def randStr(n:Int) : String = {
    (1 to n).map(x => alpha(Random.nextInt.abs % size)).mkString
  }

  //assumes to be unique
  def generateSessionKey(): String = {
    return randStr(sessionKeyLength);
  }

  def generateSlug(url: String) : String = {
    //if shortened url is already in DB return slug
    var found = Bookmark.findByUrl(url)
    if (!found.isEmpty){
      return found.get.slug
    }

    //generate unique
    var key = randStr(slugLength);

    //if slug is already in db, try to create a new one
    found = Bookmark.findBySlug(key)
    if (!found.isEmpty){
      val maxRetries = 10;
      var retryCount = 0;

      while (!found.isEmpty && retryCount <= maxRetries) {
        retryCount += 1
        key = randStr(slugLength);
        found = Bookmark.findBySlug(key)
      }

    }
    key
  }
}

