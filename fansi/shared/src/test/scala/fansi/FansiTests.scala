package test.fansi

import utest._


object FansiTests extends TestSuite{

  // Alias a bunch of rendered attributes to short names
  // to use in all our test cases
  val R = fansi.Color.Red.escape
  val G = fansi.Color.Green.escape
  val B = fansi.Color.Blue.escape
  val Y = fansi.Color.Yellow.escape
  val UND = fansi.Underlined.On.escape
  val DUND = fansi.Underlined.Off.escape
  val REV = fansi.Reversed.On.escape
  val DREV = fansi.Reversed.Off.escape
  val DCOL = fansi.Color.Reset.escape
  val RES = fansi.Attr.Reset.escape
  /**
    * ANSI escape sequence to reset text color
    */
  val RTC = fansi.Color.Reset.escape
  val tests = TestSuite{
    val rgbOps = s"+++$R---$G***$B///"
    val rgb = s"$R$G$B"
    'parsing{
      val r = fansi.Str(rgbOps).render
      assert(
        fansi.Str(rgbOps).plainText == "+++---***///",
        fansi.Str(rgb).plainText == "",
        r == rgbOps + RTC,
        fansi.Str(rgb).render == ""
      )
    }

    'equality{
      assert(fansi.Color.Red("foo") == fansi.Color.Red("foo"))
    }
    'concat{
      val concated = (fansi.Str(rgbOps) ++ fansi.Str(rgbOps)).render
      val expected = rgbOps ++ RTC ++ rgbOps ++ RTC

      assert(concated == expected)
    }
    'join{
      val concated = fansi.Str.join(fansi.Str(rgbOps), fansi.Str(rgbOps)).render
      val expected = rgbOps ++ RTC ++ rgbOps ++ RTC

      assert(concated == expected)
    }
    'get{
      val str = fansi.Str(rgbOps)
      val w = fansi.Attrs.Empty.transform(0)
      val r = fansi.Color.Red.transform(0)
      val g = fansi.Color.Green.transform(0)
      val b = fansi.Color.Blue.transform(0)
      assert(
        str.getChars.mkString == "+++---***///",
        str.getChar(0) == '+',
        str.getChar(1) == '+',
        str.getChar(2) == '+',
        str.getChar(3) == '-',
        str.getChar(4) == '-',
        str.getChar(5) == '-',
        str.getChar(6) == '*',
        str.getChar(7) == '*',
        str.getChar(8) == '*',
        str.getChar(9) == '/',
        str.getChar(10) == '/',
        str.getChar(11) == '/',
        str.getColors.toSeq == Seq(w,w,w,r,r,r,g,g,g,b,b,b),
        str.getColor(0) == w,
        str.getColor(1) == w,
        str.getColor(2) == w,
        str.getColor(3) == r,
        str.getColor(4) == r,
        str.getColor(5) == r,
        str.getColor(6) == g,
        str.getColor(7) == g,
        str.getColor(8) == g,
        str.getColor(9) == b,
        str.getColor(10) == b,
        str.getColor(11) == b
      )

    }

    'split{
      val splits = Seq(
        // These are the standard series
        (0,  s"", s"+++$R---$G***$B///$RTC"),
        (1,  s"+", s"++$R---$G***$B///$RTC"),
        (2,  s"++", s"+$R---$G***$B///$RTC"),
        (3,  s"+++", s"$R---$G***$B///$RTC"),
        (4,  s"+++$R-$RTC", s"$R--$G***$B///$RTC"),
        (5,  s"+++$R--$RTC", s"$R-$G***$B///$RTC"),
        (6,  s"+++$R---$RTC", s"$G***$B///$RTC"),
        (7,  s"+++$R---$G*$RTC", s"$G**$B///$RTC"),
        (8,  s"+++$R---$G**$RTC", s"$G*$B///$RTC"),
        (9,  s"+++$R---$G***$RTC", s"$B///$RTC"),
        (10, s"+++$R---$G***$B/$RTC", s"$B//$RTC"),
        (11, s"+++$R---$G***$B//$RTC", s"$B/$RTC"),
        (12, s"+++$R---$G***$B///$RTC", s"")
      )
      for((index, expectedLeft0, expectedRight0) <- splits){
        val (splitLeft, splitRight) = fansi.Str(rgbOps).splitAt(index)
        val (expectedLeft, expectedRight) = (expectedLeft0, expectedRight0)
        val left = splitLeft.render
        val right = splitRight.render
        assert((left, right) == (expectedLeft, expectedRight))
      }
    }
    'substring{
      val substringed = fansi.Str(rgbOps).substring(4, 9).render
      assert(substringed == s"$R--$G***$RTC")

      val default = fansi.Str(rgbOps).render

      val noOpSubstringed1 = fansi.Str(rgbOps).substring().render
      assert(noOpSubstringed1 == default)

      val parsed = fansi.Str(rgbOps)
      val noOpSubstringed2 = parsed.substring(0, parsed.length).render
      assert(noOpSubstringed2 == default)
    }

    'overlay{
      'simple{
        val overlayed = fansi.Str(rgbOps).overlay(fansi.Color.Yellow, 4, 7)
        val expected = s"+++$R-$Y--*$G**$B///$RTC"
        assert(overlayed.render == expected)
      }
      'resetty{
        val resetty = s"+$RES++$R--$RES-$RES$G***$B///"
        val overlayed = fansi.Str(resetty).overlay(fansi.Color.Yellow, 4, 7).render
        val expected = s"+++$R-$Y--*$G**$B///$RTC"
        assert(overlayed == expected)
      }
      'mixedResetUnderline{
        val resetty = s"+$RES++$R--$RES-$UND$G***$B///"
        val overlayed = fansi.Str(resetty).overlay(fansi.Color.Yellow, 4, 7).render.toVector
        val expected = s"+++$R-$Y--$UND*$G**$B///$DCOL$DUND".toVector

        assert(overlayed == expected)
      }
      'underlines{
        val resetty = s"$UND#$RES    $UND#$RES"
        'underlineBug{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 2).render
          val expected = s"$UND$REV#$DUND $DREV   $UND#$DUND"
          assert(overlayed == expected)
        }
        'barelyOverlapping{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 1).render
          val expected = s"$UND$REV#$DUND$DREV    $UND#$DUND"
          assert(overlayed == expected)
        }
        'endOfLine{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 5, 6).render
          val expected = s"$UND#$DUND    $UND$REV#$DUND$DREV"
          assert(overlayed == expected)
        }
        'overshoot{
          intercept[IllegalArgumentException]{
            fansi.Str(resetty).overlay(fansi.Reversed.On, 5, 10)
          }
        }
        'empty{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 0).render
          val expected = s"$UND#$DUND    $UND#$DUND"
          assert(overlayed == expected)
        }
        'singleContent{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 2, 4).render
          val expected = s"$UND#$DUND $REV  $DREV $UND#$DUND"
          assert(overlayed == expected)

        }
      }
      'overallAll{
        //s"+++$R---$G***$B///"
        val overlayed = fansi.Str(rgbOps).overlayAll(Seq(
          (fansi.Color.Yellow, 4, 7),
          (fansi.Underlined.On, 4, 7),
          (fansi.Underlined.Off, 5, 6),
          (fansi.Color.Blue, 7, 9)
        )).render
        val expected = s"+++$R-$Y$UND-$DUND-$UND*$B$DUND**///$DCOL"
        assert(overlayed == expected)
        overlayed
      }
    }
    'attributes{
      * - {
        Console.RESET + fansi.Underlined.On
      }
      * - {
        Console.RESET + (fansi.Underlined.On("Reset ") ++ fansi.Underlined.Off("Underlined"))
      }
      * - {
        Console.RESET + fansi.Bold.On
      }
      * - {
        Console.RESET + (fansi.Bold.On("Reset ") ++ fansi.Bold.Off("Bold"))
      }
      * - {
        Console.RESET + fansi.Reversed.On
      }
      * - {
        Console.RESET + (fansi.Reversed.On("Reset ") ++ fansi.Reversed.Off("Reversed"))
      }
    }
    def tabulate(all: Seq[fansi.Attr]) = {
      all.map(attr => attr.toString + " " * (30 - attr.name.length))
        .grouped(3)
        .map(_.mkString)
        .mkString("\n")
    }

    def square(all : Seq[fansi.Attr]) = {
      all.map( attr => attr.escapeOpt.getOrElse("") + "#")
        .grouped(32)
        .map(_.mkString)
        .mkString("\n")
    }


    'colors - tabulate(fansi.Color.all)

    'backgrounds - tabulate(fansi.Back.all)

    'trueColor - {
      'red - fansi.Color.True(255,0,0)

      'redhexa - fansi.Color.True(0xFF0000)

      'green - fansi.Color.True(0,255,0)

      'greenhexa - fansi.Color.True(0x00FF00)

      'blue - fansi.Color.True(0,0,255)

      'bluehaxe - fansi.Color.True(0x0000FF)

      "256 shades of gray" - square(for(i <- 0 to 255) yield fansi.Color.True(i,i,i))

      'trueColors - tabulate(for(i <- 0 to 0xFFFFFF by 255) yield fansi.Color.True(i))

      'trueBackgrounds - tabulate(for(i <- 0 to 0xFFFFFF by 255) yield fansi.Back.True(i))

      'blackState - assert (fansi.Color.lookupAttr(273 << 3) == fansi.Color.True(0,0,0) )

      'whitState -  assert (fansi.Color.lookupAttr(16777488 << 3) == fansi.Color.True(255,255,255) )

      'redState -  assert (fansi.Color.lookupAttr((0xFF0000 + 273) << 3) == fansi.Color.True(255,0,0))

      'lastFullState - assert ( fansi.Color.lookupAttr(272 << 3) == fansi.Color.Full(255))

      'parsing - {
        def check(frag: fansi.Str) = {
          val parsed = fansi.Str(frag.render)
          assert(parsed == frag)
          parsed
        }
        * - check(fansi.Color.True(255, 0, 0)("lol"))
        * - check(fansi.Color.True(1, 234, 56)("lol"))
        * - check(fansi.Color.True(255, 255, 255)("lol"))
        * - check(fansi.Color.True(10000)("lol"))
        * - {
          (for(i <- 0 to 255) yield check(fansi.Color.True(i,i,i)("x"))).mkString
        }
        * - check(
          "#" + fansi.Color.True(127, 126, 0)("lol") + "omg" + fansi.Color.True(127, 126, 0)("wtf")
        )

        * - check(square(for(i <- 0 to 255) yield fansi.Color.True(i,i,i)))


      }
      'failure{
        'tooLongToParse{
          * - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;0;0;256m").plainText.toSeq.map(_.toInt)
          }
          * - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;0;256;0m").plainText.toSeq.map(_.toInt)
          }
          * - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;256;0;0m").plainText.toSeq.map(_.toInt)
          }
          * - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;1111;0;0m").plainText.toSeq.map(_.toInt)
          }
        }
        'truncatedParsing - {
          val escape = fansi.Color.True(255, 0, 0).escape
          for (i <- 1 until escape.length - 1)
          yield intercept[IllegalArgumentException] {
            fansi.Str(escape.dropRight(i))
          }
        }
        'args{
          * - intercept[IllegalArgumentException]{ fansi.Color.True(256, 0, 0) }
          * - intercept[IllegalArgumentException]{ fansi.Color.True(0, 256, 0) }
          * - intercept[IllegalArgumentException]{ fansi.Color.True(0, 0, 256) }
          * - intercept[IllegalArgumentException]{ fansi.Color.True(-1, 0, 0) }
          * - intercept[IllegalArgumentException]{ fansi.Color.True(0, -1, 0) }
          * - intercept[IllegalArgumentException]{ fansi.Color.True(0, 0, -1) }
        }
      }
    }

    'emitAnsiCodes{
      'basic - assert(
        fansi.Attrs.emitAnsiCodes(0, fansi.Color.Red.applyMask) == Console.RED,
        fansi.Attrs.emitAnsiCodes(fansi.Color.Red.applyMask, 0) == fansi.Color.Reset.escape
      )
      'combo - {
        // One color stomps over the other
        val colorColor = fansi.Color.Red ++ fansi.Color.Blue
        assert(fansi.Attrs.emitAnsiCodes(0, colorColor.applyMask) == Console.BLUE)


        val colorBold = fansi.Color.Red ++ fansi.Bold.On
        assert(fansi.Attrs.emitAnsiCodes(0, colorBold.applyMask) == Console.RED + Console.BOLD)
        // unlike Colors and Underlined and Reversed, Bold needs a hard reset,
        assert(fansi.Attrs.emitAnsiCodes(colorBold.applyMask, 0) == Console.RESET)
      }

    }

    'negative{
      'errorMode{
        // Make sure that fansi.Str throws on most common non-color
        // fansi terminal commands
        //
        // List of common non-color fansi terminal commands taken from
        // https://en.wikipedia.org/wiki/ANSI_escape_code#Non-CSI_codes

        def check(s: String, msg: String) ={
          // If I ask it to throw, it throws
          val thrownError = intercept[IllegalArgumentException]{
            fansi.Str(s, errorMode = fansi.ErrorMode.Throw)
          }
          assert(thrownError.getMessage.contains(msg))
          // If I ask it to sanitize, the escape character is gone but the
          // rest of each escape sequence remains
          val sanitized = fansi.Str(s, errorMode = fansi.ErrorMode.Sanitize)
          assert(sanitized.plainText == ("Hello" + msg + "World"))

          // If I ask it to strip, everything is gone
          val stripped = fansi.Str(s, errorMode = fansi.ErrorMode.Strip)
          assert(stripped.plainText == "HelloWorld")
        }

        'cursorUp - check("Hello\u001b[2AWorld", "[2A")
        'cursorDown- check("Hello\u001b[2BWorld", "[2B")
        'cursorForward - check("Hello\u001b[2CWorld", "[2C")
        'cursorBack - check("Hello\u001b[2DWorld", "[2D")
        'cursorNextLine - check("Hello\u001b[2EWorld", "[2E")
        'cursorPrevLine - check("Hello\u001b[2FWorld", "[2F")
        'cursorHorizontalAbs - check("Hello\u001b[2GWorld", "[2G")
        'cursorPosition- check("Hello\u001b[2;2HWorld", "[2;2H")
        'eraseDisplay - check("Hello\u001b[2JWorld", "[2J")
        'eraseLine - check("Hello\u001b[2KWorld", "[2K")
        'scrollUp - check("Hello\u001b[2SWorld", "[2S")
        'scrollDown - check("Hello\u001b[2TWorld", "[2T")
        'horizontalVerticalPos - check("Hello\u001b[2;2fWorld", "[2;2f")
        'selectGraphicRendition - check("Hello\u001b[2mWorld", "[2m")
        'auxPortOn - check("Hello\u001b[5iWorld", "[5i")
        'auxPortOff - check("Hello\u001b[4iWorld", "[4i")
        'deviceStatusReport - check("Hello\u001b[6nWorld", "[6n")
        'saveCursor - check("Hello\u001b[sWorld", "[s")
        'restoreCursor - check("Hello\u001b[uWorld", "[u")
      }
      'outOfBounds{
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(10) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(4) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(-1) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(0, 4)}
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(-1, 2)}
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(2, 1)}
      }
    }
    'multipleAttrs{
      'identicalMasksGetCollapsed{
        val redRed = fansi.Color.Red ++ fansi.Color.Red
        assert(
          redRed.resetMask == fansi.Color.Red.resetMask,
          redRed.applyMask == fansi.Color.Red.applyMask
        )
      }
      'overlappingMasksGetReplaced{
        val redBlue = fansi.Color.Red ++ fansi.Color.Blue
        assert(
          redBlue.resetMask == fansi.Color.Blue.resetMask,
          redBlue.applyMask == fansi.Color.Blue.applyMask
        )
      }
      'semiOverlappingMasks{
        val resetRed = fansi.Attr.Reset ++ fansi.Color.Red
        val redReset = fansi.Color.Red ++ fansi.Attr.Reset
        assert(
          resetRed != fansi.Attr.Reset,
          resetRed != fansi.Color.Red,
          redReset == fansi.Attr.Reset,
          redReset != fansi.Color.Red,
          redReset != resetRed,
          resetRed.resetMask == fansi.Attr.Reset.resetMask,
          resetRed.applyMask == fansi.Color.Red.applyMask
        )
      }
      'separateMasksGetCombined{
        val redBold = fansi.Color.Red ++ fansi.Bold.On

        assert(
          redBold.resetMask == (fansi.Color.Red.resetMask | fansi.Bold.On.resetMask),
          redBold.applyMask == (fansi.Color.Red.applyMask | fansi.Bold.On.applyMask)
        )
      }
      'applicationWorks{
        val redBlueBold = fansi.Color.Red ++ fansi.Color.Blue ++ fansi.Bold.On
        val colored = redBlueBold("Hello World")
        val separatelyColored = fansi.Bold.On(fansi.Color.Blue(fansi.Color.Red("Hello World")))
        assert(colored.render == separatelyColored.render)
      }
      'equality{
        assert(
          fansi.Color.Blue ++ fansi.Color.Red == fansi.Color.Red,
          fansi.Color.Red == fansi.Color.Blue ++ fansi.Color.Red,
          fansi.Bold.On ++ fansi.Color.Red != fansi.Color.Red,
          fansi.Color.Red != fansi.Bold.On ++ fansi.Color.Red
        )
      }
    }
//    'perf{
//      val input = s"+++$R---$G***$B///" * 1000
//
//      'parsing{
//
//        val start = System.currentTimeMillis()
//        var count = 0
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          fansi.Str(input)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'rendering{
//
//        val start = System.currentTimeMillis()
//        var count = 0
//        val parsed = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          parsed.render
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'concat{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          fansiStr ++ fansiStr
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'splitAt{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          fansiStr.splitAt(count % fansiStr.length)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'substring{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          val start = count % fansiStr.length
//          val end = count % (fansiStr.length - start) + start
//          fansiStr.substring(start, end)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'overlay{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        val attrs =
//          fansi.Color.Red ++
//          fansi.Color.Blue ++
//          fansi.Bold.On ++
//          fansi.Reversed.On ++
//          fansi.Bold.Off ++
//          fansi.Underlined.On
//
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          val start = count % fansiStr.length
//          val end = count % (fansiStr.length - start) + start
//          fansiStr.overlay(attrs, start, end)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//    }
  }
}

