package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.Any
import kotlin.Boolean
import kotlin.Byte
import kotlin.Char
import kotlin.CharSequence
import kotlin.Comparable
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Number
import kotlin.Short
import kotlin.String
import kotlin.Throwable
import kotlin.Unit
import kotlin.collections.Collection
import kotlin.collections.Iterable
import kotlin.collections.Iterator
import kotlin.collections.List
import kotlin.collections.ListIterator
import kotlin.collections.Map
import kotlin.collections.Set

public class JavaTypesMock : JavaTypes, Mock {
  public override val uuid: String by uuid()

  public override fun boolean(b: Boolean): Unit {
                return mockUnit(
                        methodName = Method.boolean,
        arguments = mapOf(Arg.b to b)
                    )
  }

  public override fun byte(b: Byte): Unit {
                return mockUnit(
                        methodName = Method.byte,
        arguments = mapOf(Arg.b to b)
                    )
  }

  public override fun char(c: Char): Unit {
                return mockUnit(
                        methodName = Method.char,
        arguments = mapOf(Arg.c to c)
                    )
  }

  public override fun charsequance(cs: CharSequence): Unit {
                return mockUnit(
                        methodName = Method.charsequance,
        arguments = mapOf(Arg.cs to cs)
                    )
  }

  public override fun collection(c: Collection<String>): Unit {
                return mockUnit(
                        methodName = Method.collection,
        arguments = mapOf(Arg.c to c)
                    )
  }

  public override fun comparable(o: Comparable<String>): Unit {
                return mockUnit(
                        methodName = Method.comparable,
        arguments = mapOf(Arg.o to o)
                    )
  }

  public override fun double(d: Double): Unit {
                return mockUnit(
                        methodName = Method.double,
        arguments = mapOf(Arg.d to d)
                    )
  }

  public override fun float(f: Float): Unit {
                return mockUnit(
                        methodName = Method.float,
        arguments = mapOf(Arg.f to f)
                    )
  }

  public override fun int(i: Int): Unit {
                return mockUnit(
                        methodName = Method.int,
        arguments = mapOf(Arg.i to i)
                    )
  }

  public override fun iterable(i: Iterable<String>): Unit {
                return mockUnit(
                        methodName = Method.iterable,
        arguments = mapOf(Arg.i to i)
                    )
  }

  public override fun iterator(i: Iterator<String>): Unit {
                return mockUnit(
                        methodName = Method.iterator,
        arguments = mapOf(Arg.i to i)
                    )
  }

  public override fun list(l: List<String>): Unit {
                return mockUnit(
                        methodName = Method.list,
        arguments = mapOf(Arg.l to l)
                    )
  }

  public override fun listIterator(li: ListIterator<String>): Unit {
                return mockUnit(
                        methodName = Method.listIterator,
        arguments = mapOf(Arg.li to li)
                    )
  }

  public override fun long(l: Long): Unit {
                return mockUnit(
                        methodName = Method.long,
        arguments = mapOf(Arg.l to l)
                    )
  }

  public override fun map(m: Map<String, String>): Unit {
                return mockUnit(
                        methodName = Method.map,
        arguments = mapOf(Arg.m to m)
                    )
  }

  public override fun mapEntry(me: Map.Entry<String, String>): Unit {
                return mockUnit(
                        methodName = Method.mapEntry,
        arguments = mapOf(Arg.me to me)
                    )
  }

  public override fun number(s: Number): Unit {
                return mockUnit(
                        methodName = Method.number,
        arguments = mapOf(Arg.s to s)
                    )
  }

  public override fun obj(o: Any): Unit {
                return mockUnit(
                        methodName = Method.obj,
        arguments = mapOf(Arg.o to o)
                    )
  }

  public override fun `set`(s: Set<String>): Unit {
                return mockUnit(
                        methodName = Method.`set`,
        arguments = mapOf(Arg.s to s)
                    )
  }

  public override fun shorth(s: Short): Unit {
                return mockUnit(
                        methodName = Method.shorth,
        arguments = mapOf(Arg.s to s)
                    )
  }

  public override fun string(s: String): Unit {
                return mockUnit(
                        methodName = Method.string,
        arguments = mapOf(Arg.s to s)
                    )
  }

  public override fun throwable(t: Throwable): Unit {
                return mockUnit(
                        methodName = Method.throwable,
        arguments = mapOf(Arg.t to t)
                    )
  }

  public object Method {
    public const val boolean: String = "boolean"

    public const val byte: String = "byte"

    public const val char: String = "char"

    public const val charsequance: String = "charsequance"

    public const val collection: String = "collection"

    public const val comparable: String = "comparable"

    public const val double: String = "double"

    public const val float: String = "float"

    public const val int: String = "int"

    public const val iterable: String = "iterable"

    public const val iterator: String = "iterator"

    public const val list: String = "list"

    public const val listIterator: String = "listIterator"

    public const val long: String = "long"

    public const val map: String = "map"

    public const val mapEntry: String = "mapEntry"

    public const val number: String = "number"

    public const val obj: String = "obj"

    public const val `set`: String = "set"

    public const val shorth: String = "shorth"

    public const val string: String = "string"

    public const val throwable: String = "throwable"
  }

  public object Arg {
    public const val b: String = "b"

    public const val c: String = "c"

    public const val cs: String = "cs"

    public const val o: String = "o"

    public const val d: String = "d"

    public const val f: String = "f"

    public const val i: String = "i"

    public const val l: String = "l"

    public const val li: String = "li"

    public const val m: String = "m"

    public const val me: String = "me"

    public const val s: String = "s"

    public const val t: String = "t"
  }

  public object Property
}
