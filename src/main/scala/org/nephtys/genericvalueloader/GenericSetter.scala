package org.nephtys.genericvalueloader

import java.io.PrintWriter

/**
  * Created by nephtys on 9/27/16.
  */
trait GenericSetter[T] {

  def setValue(t : T)
}


object GenericSetter {
  def saveToFile(filename : String, body : String) = {
    new PrintWriter(filename) { write(body); close() }
  }
  def loadFromFile(filename : String) : String = {
    val source = scala.io.Source.fromFile(filename)
    val lines = try source.mkString finally source.close()
    lines
  }
}