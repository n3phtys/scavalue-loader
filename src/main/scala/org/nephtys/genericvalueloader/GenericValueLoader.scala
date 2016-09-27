package org.nephtys.genericvalueloader

/**
  * Created by nephtys on 9/27/16.
  */
case class GenericValueLoader[T](
                                  filename : () => String,
                                  timeout : Long, serialize : T => String,
                                  deseralize : T => String,
                                  defaultFunc : Option[() => T]
                                ) extends GenericGetter[T] with GenericSetter[T] {
  override def getValue: T = ???

  override def setValue(t: T): Unit = ???
}
