package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
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

public class JavaTypesSpy(
  public val javaTypes: JavaTypes,
) : JavaTypes, Spy {
  public override val uuid: String by uuid()

  public override fun boolean(b: Boolean): Unit {
                return spy(
                        methodName = Method.boolean,
        arguments = mapOf(Arg.b to b),
        delegate = { javaTypes.boolean(b) }
                    )
  }

  public override fun byte(b: Byte): Unit {
                return spy(
                        methodName = Method.byte,
        arguments = mapOf(Arg.b to b),
        delegate = { javaTypes.byte(b) }
                    )
  }

  public override fun char(c: Char): Unit {
                return spy(
                        methodName = Method.char,
        arguments = mapOf(Arg.c to c),
        delegate = { javaTypes.char(c) }
                    )
  }

  public override fun charsequance(cs: CharSequence): Unit {
                return spy(
                        methodName = Method.charsequance,
        arguments = mapOf(Arg.cs to cs),
        delegate = { javaTypes.charsequance(cs) }
                    )
  }

  public override fun collection(c: Collection<String>): Unit {
                return spy(
                        methodName = Method.collection,
        arguments = mapOf(Arg.c to c),
        delegate = { javaTypes.collection(c) }
                    )
  }

  public override fun comparable(o: Comparable<String>): Unit {
                return spy(
                        methodName = Method.comparable,
        arguments = mapOf(Arg.o to o),
        delegate = { javaTypes.comparable(o) }
                    )
  }

  public override fun double(d: Double): Unit {
                return spy(
                        methodName = Method.double,
        arguments = mapOf(Arg.d to d),
        delegate = { javaTypes.double(d) }
                    )
  }

  public override fun float(f: Float): Unit {
                return spy(
                        methodName = Method.float,
        arguments = mapOf(Arg.f to f),
        delegate = { javaTypes.float(f) }
                    )
  }

  public override fun int(i: Int): Unit {
                return spy(
                        methodName = Method.int,
        arguments = mapOf(Arg.i to i),
        delegate = { javaTypes.int(i) }
                    )
  }

  public override fun iterable(i: Iterable<String>): Unit {
                return spy(
                        methodName = Method.iterable,
        arguments = mapOf(Arg.i to i),
        delegate = { javaTypes.iterable(i) }
                    )
  }

  public override fun iterator(i: Iterator<String>): Unit {
                return spy(
                        methodName = Method.iterator,
        arguments = mapOf(Arg.i to i),
        delegate = { javaTypes.iterator(i) }
                    )
  }

  public override fun list(l: List<String>): Unit {
                return spy(
                        methodName = Method.list,
        arguments = mapOf(Arg.l to l),
        delegate = { javaTypes.list(l) }
                    )
  }

  public override fun listIterator(li: ListIterator<String>): Unit {
                return spy(
                        methodName = Method.listIterator,
        arguments = mapOf(Arg.li to li),
        delegate = { javaTypes.listIterator(li) }
                    )
  }

  public override fun long(l: Long): Unit {
                return spy(
                        methodName = Method.long,
        arguments = mapOf(Arg.l to l),
        delegate = { javaTypes.long(l) }
                    )
  }

  public override fun map(m: Map<String, String>): Unit {
                return spy(
                        methodName = Method.map,
        arguments = mapOf(Arg.m to m),
        delegate = { javaTypes.map(m) }
                    )
  }

  public override fun mapEntry(me: Map.Entry<String, String>): Unit {
                return spy(
                        methodName = Method.mapEntry,
        arguments = mapOf(Arg.me to me),
        delegate = { javaTypes.mapEntry(me) }
                    )
  }

  public override fun number(s: Number): Unit {
                return spy(
                        methodName = Method.number,
        arguments = mapOf(Arg.s to s),
        delegate = { javaTypes.number(s) }
                    )
  }

  public override fun obj(o: Any): Unit {
                return spy(
                        methodName = Method.obj,
        arguments = mapOf(Arg.o to o),
        delegate = { javaTypes.obj(o) }
                    )
  }

  public override fun `set`(s: Set<String>): Unit {
                return spy(
                        methodName = Method.`set`,
        arguments = mapOf(Arg.s to s),
        delegate = { javaTypes.`set`(s) }
                    )
  }

  public override fun shorth(s: Short): Unit {
                return spy(
                        methodName = Method.shorth,
        arguments = mapOf(Arg.s to s),
        delegate = { javaTypes.shorth(s) }
                    )
  }

  public override fun string(s: String): Unit {
                return spy(
                        methodName = Method.string,
        arguments = mapOf(Arg.s to s),
        delegate = { javaTypes.string(s) }
                    )
  }

  public override fun throwable(t: Throwable): Unit {
                return spy(
                        methodName = Method.throwable,
        arguments = mapOf(Arg.t to t),
        delegate = { javaTypes.throwable(t) }
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
