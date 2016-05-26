package fansi

import java.util

import scala.annotation.tailrec

/**
  * Encapsulates a string with associated ANSI colors and text decorations.
  *
  * This is your primary data-type when you are dealing with colored fansi
  * strings.
  *
  * Contains some basic string methods, as well as some ansi methods to e.g.
  * apply particular colors or other decorations to particular sections of
  * the [[fansi.Str]]. [[render]] flattens it out into a `java.lang.String`
  * with all the colors present as ANSI escapes.
  *
  * Avoids using Scala collections operations in favor of util.Arrays,
  * giving 20% (on `++`) to >1000% (on `splitAt`, `subString`
  * and `Str.parse`) speedups
  */
case class Str private(private val chars: Array[Char], private val colors: Array[Str.State]) {
  require(chars.length == colors.length)

  /**
    * Concatenates two [[fansi.Str]]s, preserving the colors in each one and
    * avoiding any interference between them
    */
  def ++(other: Str) = {
    val chars2 = new Array[Char](length + other.length)
    val colors2 = new Array[Str.State](length + other.length)
    System.arraycopy(chars, 0, chars2, 0, length)
    System.arraycopy(other.chars, 0, chars2, length, other.length)
    System.arraycopy(colors, 0, colors2, 0, length)
    System.arraycopy(other.colors, 0, colors2, length, other.length)

    Str(chars2, colors2)
  }

  /**
    * Splits an [[fansi.Str]] into two sub-strings, preserving the colors in
    * each one.
    *
    * @param index the plain-text index of the point within the [[fansi.Str]]
    *              you want to use to split it.
    */
  def splitAt(index: Int) = (
    new Str(
      util.Arrays.copyOfRange(chars, 0, index),
      util.Arrays.copyOfRange(colors, 0, index)
    ),
    new Str(
      util.Arrays.copyOfRange(chars, index, length),
      util.Arrays.copyOfRange(colors, index, length)
    )
  )

  /**
    * Returns an [[fansi.Str]] which is a substring of this string,
    * and has the same colors as the original section of this string
    * did
    */
  def substring(start: Int = 0, end: Int = length) = {
    if (start < 0 || start >= length) throw new IllegalArgumentException(
      s"substring start parameter [$start] must be between 0 and $length"
    )
    if (end < 0 || end >= length || end < start) throw new IllegalArgumentException(
      s"substring end parameter [$end] must be between start $start and $length"
    )
    new Str(
      util.Arrays.copyOfRange(chars, start, end),
      util.Arrays.copyOfRange(colors, start, end)
    )
  }



  /**
    * The plain-text length of this [[fansi.Str]], in UTF-16 characters (same
    * as `.length` on a `java.lang.String`). If you want fancy UTF-8 lengths,
    * use `.plainText`
    */
  def length = chars.length


  override def toString = render

  /**
    * The plain-text `java.lang.String` represented by this [[fansi.Str]],
    * without all the fansi colors or other decorations
    */
  lazy val plainText = new String(chars)

  /**
    * Returns a copy of the colors array backing this `fansi.Str`, in case
    * you want to use it to
    */
  def getColors = colors.clone()
  /**
    * Returns a copy of the character array backing this `fansi.Str`, in case
    * you want to use it to
    */
  def getChars = chars.clone()

  /**
    * Converts this [[fansi.Str]] into a `java.lang.String`, including all
    * the fancy fansi colors or decorations as fansi escapes embedded within
    * the string. "Terminates" colors at the right-most end of the resultant
    * `java.lang.String`, making it safe to concat-with or embed-inside other
    * `java.lang.String` without worrying about fansi colors leaking out of it.
    */
  def render = {
    // Pre-size StringBuilder with approximate size (ansi colors tend
    // to be about 5 chars long) to avoid re-allocations during growth
    val output = new StringBuilder(chars.length + colors.length * 5)


    var currentState: Str.State = 0
    /**
      * Emit the ansi escapes necessary to transition
      * between two states, if necessary.
      */
    def emitDiff(nextState: Int) = if (currentState != nextState){
      val hardOffMask = Bold.mask
      // Any of these transitions from 1 to 0 within the hardOffMask
      // categories cannot be done with a single ansi escape, and need
      // you to emit a RESET followed by re-building whatever ansi state
      // you previous had from scratch
      if ((currentState & ~nextState & hardOffMask) != 0){
        output.append(Console.RESET)
        currentState = 0
      }

      var categoryIndex = 0
      while(categoryIndex < Attr.categories.length){
        val cat = Attr.categories(categoryIndex)
        if ((cat.mask & currentState) != (cat.mask & nextState)){
          val attr = cat.bitsMap(nextState & cat.mask)

          if (attr.escapeOpt.isDefined) {
            output.append(attr.escapeOpt.get)
          }
        }
        categoryIndex += 1
      }
    }

    var i = 0
    while(i < colors.length){
      // Emit ANSI escapes to change colors where necessary
      emitDiff(colors(i))
      currentState = colors(i)
      output.append(chars(i))
      i += 1
    }

    // Cap off the left-hand-side of the rendered string with any ansi escape
    // codes necessary to rest the state to 0
    emitDiff(0)

    output.toString
  }



  /**
    * Overlays the desired color over the specified range of the [[fansi.Str]].
    */
  def overlay(overlayColor: Attr, start: Int = 0, end: Int = length) = {
    require(end >= start,
      s"end:$end must be greater than start:$end in fansiStr#overlay call"
    )
    val colorsOut = new Array[Int](colors.length)
    var i = 0
    while(i < colors.length){
      if (i >= start && i < end) colorsOut(i) = overlayColor.transform(colors(i))
      else colorsOut(i) = colors(i)
      i += 1
    }
    new Str(chars, colorsOut)
  }

}

object Str{

  /**
    * An [[fansi.Str]]'s `color`s array is filled with Ints, each representing
    * the ANSI state of one character encoded in its bits. Each [[Attr]] belongs
    * to a [[Category]] that occupies a range of bits within each int:
    *
    * 31... 22 21 20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
    *  |--------|  |-----------------------|  |-----------------------|  |  |  |bold
    *           |                          |                          |  |  |reversed
    *           |                          |                          |  |underlined
    *           |                          |                          |foreground-color
    *           |                          |background-color
    *           |unused
    *
    *
    * The `0000 0000 0000 0000` int corresponds to plain text with no decoration
    *
    */
  type State = Int

  /**
    * Make the construction of [[fansi.Str]]s from `String`s and other
    * `CharSequence`s automatic
    */
  implicit def implicitApply(raw: CharSequence): fansi.Str = apply(raw)

  /**
    * Creates an [[fansi.Str]] from a non-fansi `java.lang.String` or other
    * `CharSequence`.
    *
    * Note that this method is implicit, meaning you can pass in a
    * `java.lang.String` anywhere an `fansi.Str` is required and it will be
    * automatically parsed and converted for you.
    *
    * @param strict throw an exception if an unrecognized fansi sequence exists.
    *               Off by default
    */
  def apply(raw: CharSequence, strict: Boolean = false): fansi.Str = {
    // Pre-allocate some arrays for us to fill up. They will probably be
    // too big if the input has any ansi codes at all but that's ok, we'll
    // trim them later.
    val chars = new Array[Char](raw.length)
    val colors = new Array[Int](raw.length)

    var currentColor = 0
    var sourceIndex = 0
    var destIndex = 0
    val length = raw.length
    while(sourceIndex < length){
      val char = raw.charAt(sourceIndex)
      if (char == '\u001b' || char == '\u009b') {
        ParseMap.query(raw, sourceIndex) match{
          case Some(tuple) =>
            currentColor = tuple._2.transform(currentColor)
            sourceIndex += tuple._1
          case None =>
            if (strict) {
              // If we found the start of an escape code that was missed by our
              // regex, also bail out and just report the index since that's all we
              // know about it
              throw new IllegalArgumentException(
                s"Unknown fansi-escape at index $sourceIndex inside string cannot be " +
                  "parsed into an fansi.Str"
              )
            }
        }
      }else {
        colors(destIndex) = currentColor
        chars(destIndex) = char
        sourceIndex += 1
        destIndex += 1
      }
    }

    Str(
      util.Arrays.copyOfRange(chars, 0, destIndex),
      util.Arrays.copyOfRange(colors, 0, destIndex)
    )
  }

  /**
    * Constructs a [[fansi.Str]] from an array of characters and an array
    * of colors. Performs a defensive copy of the arrays, and validates that
    * they both have the same length
    *
    * Useful together with `getChars` and `getColors` if you want to do manual
    * work on the two mutable arrays before stitching them back together into
    * one immutable [[fansi.Str]]
    */
  def fromArrays(chars: Array[Char], colors: Array[Str.State]) = {
    new fansi.Str(chars.clone(), colors.clone())
  }

  private[this] val ParseMap = {
    val pairs = for {
      cat <- Attr.categories
      color <- cat.all
      str <- color.escapeOpt
    } yield (str, color)
    new Trie(pairs :+ (Console.RESET -> Attr.Reset))
  }

}

/**
  * Represents a single, atomic ANSI escape sequence that results in a
  * color, background or decoration being added to the output. May or may not
  * have an escape sequence (`escapeOpt`), as some attributes (e.g. [[Bold.Off]])
  * are not widely/directly supported by terminals and so fansi.Str supports them
  * by rendering a hard [[Attr.Reset]] and then re-rendering other [[Attr]]s that are
  * active.
  *
  * Many of the codes were stolen shamelessly from
  *
  * http://misc.flogisoft.com/bash/tip_colors_and_formatting
  */
sealed trait Attr{
  /**
    * escapeOpt the actual ANSI escape sequence corresponding to this Attr
    */
  def escapeOpt: Option[String]
  def resetMask: Int
  def applyMask: Int
  def transform(state: Int) = (state & ~resetMask) | applyMask
  def name: String
  def apply(s: fansi.Str) = s.overlay(this, 0, s.length)
}
object Attr{
  /**
    * Represents the removal of all ansi text decoration. Doesn't fit into any
    * convenient category, since it applies to them all.
    */
  val Reset = new EscapeAttr(Console.RESET, Int.MaxValue, 0)

  /**
    * A list of possible categories
    */
  val categories = Vector(
    Color,
    Back,
    Bold,
    Underlined,
    Reversed
  )
}
/**
  * An [[Attr]] represented by an fansi escape sequence
  */
case class EscapeAttr private[fansi](escape: String, resetMask: Int, applyMask: Int)
                                   (implicit sourceName: sourcecode.Name) extends Attr{
  def escapeOpt = Some(escape)
  val name = sourceName.value
  override def toString = escape + name + Console.RESET
}

/**
  * An [[Attr]] for which no fansi escape sequence exists
  */
case class ResetAttr private[fansi](resetMask: Int, applyMask: Int)
                                  (implicit sourceName: sourcecode.Name) extends Attr{
  def escapeOpt = None
  val name = sourceName.value
  override def toString = name
}



/**
  * Represents a set of [[fansi.Attr]]s all occupying the same bit-space
  * in the state `Int`
  */
sealed abstract class Category(offset: Int, width: Int)(implicit catName: sourcecode.Name){
  def mask = ((1 << width) - 1) << offset
  val all: Seq[Attr]
  lazy val bitsMap = all.map{ m => m.applyMask -> m}.toMap
  def makeAttr(s: String, applyValue: Int)(implicit name: sourcecode.Name) = {
    new EscapeAttr(s, mask, applyValue << offset)(catName.value + "." + name.value)
  }
  def makeNoneAttr(applyValue: Int)(implicit name: sourcecode.Name) = {
    new ResetAttr(mask, applyValue << offset)(catName.value + "." + name.value)
  }
}

/**
  * [[Attr]]s to turn text bold/bright or disable it
  */
object Bold extends Category(offset = 0, width = 1){
  val On  = makeAttr(Console.BOLD, 1)
  val Off = makeNoneAttr(          0)
  val all = Seq(On, Off)
}

/**
  * [[Attr]]s to reverse the background/foreground colors of your text,
  * or un-reverse them
  */
object Reversed extends Category(offset = 1, width = 1){
  val On  = makeAttr(Console.REVERSED,   1)
  val Off = makeAttr("\u001b[27m",       0)
  val all = Seq(On, Off)
}
/**
  * [[Attr]]s to enable or disable underlined text
  */
object Underlined extends Category(offset = 2, width = 1){
  val On  = makeAttr(Console.UNDERLINED, 1)
  val Off = makeAttr("\u001b[24m",       0)
  val all = Seq(On, Off)
}

/**
  * [[Attr]]s to set or reset the color of your foreground text
  */
object Color extends Category(offset = 3, width = 9){

  val Reset        = makeAttr("\u001b[39m",     0)
  val Black        = makeAttr(Console.BLACK,    1)
  val Red          = makeAttr(Console.RED,      2)
  val Green        = makeAttr(Console.GREEN,    3)
  val Yellow       = makeAttr(Console.YELLOW,   4)
  val Blue         = makeAttr(Console.BLUE,     5)
  val Magenta      = makeAttr(Console.MAGENTA,  6)
  val Cyan         = makeAttr(Console.CYAN,     7)
  val LightGray    = makeAttr("\u001b[37m",     8)
  val DarkGray     = makeAttr("\u001b[90m",     9)
  val LightRed     = makeAttr("\u001b[91m",    10)
  val LightGreen   = makeAttr("\u001b[92m",    11)
  val LightYellow  = makeAttr("\u001b[93m",    12)
  val LightBlue    = makeAttr("\u001b[94m",    13)
  val LightMagenta = makeAttr("\u001b[95m",    14)
  val LightCyan    = makeAttr("\u001b[96m",    15)
  val White        = makeAttr("\u001b[97m",    16)
  /**
    * Foreground 256 color [[Attr]]s, for those terminals that support it
    */
  val Full =
    for(x <- 0 to 256)
    yield makeAttr(s"\u001b[38;5;${x}m", 17 + x)(s"Color.Full($x)")

  val all = Vector(
    Reset, Black, Red, Green, Yellow, Blue, Magenta, Cyan, LightGray, DarkGray,
    LightRed, LightGreen, LightYellow, LightBlue, LightMagenta, LightCyan, White
  ) ++ Full
}

/**
  * [[Attr]]s to set or reset the color of your background
  */
object Back extends Category(offset = 12, width = 9){

  val Reset        = makeAttr("\u001b[49m",       0)
  val Black        = makeAttr(Console.BLACK_B,    1)
  val Red          = makeAttr(Console.RED_B,      2)
  val Green        = makeAttr(Console.GREEN_B,    3)
  val Yellow       = makeAttr(Console.YELLOW_B,   4)
  val Blue         = makeAttr(Console.BLUE_B,     5)
  val Magenta      = makeAttr(Console.MAGENTA_B,  6)
  val Cyan         = makeAttr(Console.CYAN_B,     7)
  val LightGray    = makeAttr("\u001b[47m",       8)
  val DarkGray     = makeAttr("\u001b[100m",      9)
  val LightRed     = makeAttr("\u001b[101m",     10)
  val LightGreen   = makeAttr("\u001b[102m",     11)
  val LightYellow  = makeAttr("\u001b[103m",     12)
  val LightBlue    = makeAttr("\u001b[104m",     13)
  val LightMagenta = makeAttr("\u001b[105m",     14)
  val LightCyan    = makeAttr("\u001b[106m",     15)
  val White        = makeAttr("\u001b[107m",     16)
  /**
    * Background 256 color [[Attr]]s, for those terminals that support it
    */
  val Full =
    for(x <- 0 to 256)
    yield makeAttr(s"\u001b[48;5;${x}m", 17 + x)(s"Back.Full($x)")

  val all = Vector(
    Reset, Black, Red, Green, Yellow, Blue, Magenta, Cyan, LightGray, DarkGray,
    LightRed, LightGreen, LightYellow, LightBlue, LightMagenta, LightCyan, White
  ) ++ Full
}


/**
  * An string trie for quickly looking up values of type [[T]]
  * using string-keys. Used to speed up
  */
private[this] final class Trie[T](strings: Seq[(String, T)]){

  val (min, max, arr, value) = {
    strings.partition(_._1.isEmpty) match{
      case (Nil, continuations) =>
        val allChildChars = continuations.map(_._1(0))
        val min = allChildChars.min
        val max = allChildChars.max

        val arr = new Array[Trie[T]](max - min + 1)
        for( (char, ss) <- continuations.groupBy(_._1(0)) ){
          arr(char - min) = new Trie(ss.map{case (k, v) => (k.tail, v)})
        }

        (min, max, arr, None)

      case (Seq((_, terminalValue)), Nil) =>
        (
          0.toChar,
          0.toChar,
          new Array[Trie[T]](0),
          Some(terminalValue)
        )

      case _ => ???
    }
  }

  def apply(c: Char): Trie[T] = {
    if (c > max || c < min) null
    else arr(c - min)
  }

  /**
    * Returns the length of the matching string, or -1 if not found
    */
  def query(input: CharSequence, index: Int): Option[(Int, T)] = {

    @tailrec def rec(offset: Int, currentNode: Trie[T]): Option[(Int, T)] = {
      if (currentNode.value.isDefined) currentNode.value.map(offset - index -> _)
      else if (offset >= input.length) None
      else {
        val char = input.charAt(offset)
        val next = currentNode(char)
        if (next == null) None
        else rec(offset + 1, next)
      }
    }
    rec(index, this)
  }
}

