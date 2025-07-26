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
    test("parsing"){
      val r = fansi.Str(rgbOps).render
      assert(
        fansi.Str(rgbOps).plainText == "+++---***///",
        fansi.Str(rgb).plainText == "",
        r == rgbOps + RTC,
        fansi.Str(rgb).render == ""
      )
    }

    test("equality"){
      assert(fansi.Color.Red("foo") == fansi.Color.Red("foo"))
    }
    test("concat"){
      val concated = (fansi.Str(rgbOps) ++ fansi.Str(rgbOps)).render
      val expected = rgbOps ++ RTC ++ rgbOps ++ RTC

      assert(concated == expected)
    }
    test("apply"){
      val concated = fansi.Str(fansi.Str(rgbOps), fansi.Str(rgbOps)).render
      val expected = rgbOps ++ RTC ++ rgbOps ++ RTC

      assert(concated == expected)

      val concated2 = fansi.Str("hello", "world", "i am cow")
      val concated3 = fansi.Str("helloworld", "i am cow")
      assert(concated2 == concated3)

      val applied = fansi.Str("hello")
      assert(applied.plainText == "hello")
      assert(applied.getColors.forall(_ == 0))
    }
    test("join"){
      val concated = fansi.Str.join(Seq(fansi.Str(rgbOps), fansi.Str(rgbOps))).render
      val expected = rgbOps ++ RTC ++ rgbOps ++ RTC
      assert(concated == expected)

      val concated2 = fansi.Str.join(Seq(fansi.Str(rgbOps), fansi.Str("xyz"))).render
      val expected2 = rgbOps ++ RTC ++ "xyz"
      assert(concated2 == expected2)

      val concated3 = fansi.Str.join(Seq(fansi.Str(rgbOps)), sep = "lol").render
      val expected3 = rgbOps ++ RTC
      assert(concated3 == expected3)

      val concated4 = fansi.Str.join(Seq(fansi.Str(rgbOps), fansi.Str("xyz")), sep = "lol").render
      val expected4 = rgbOps ++ RTC ++ "lol" ++ "xyz"
      assert(concated4 == expected4)

      val concated5 = fansi.Str.join(Seq(fansi.Str(rgbOps), fansi.Str("xyz"), fansi.Str(rgbOps)), sep = "lol").render
      val expected5 = rgbOps ++ RTC ++ "lol" ++ "xyz" ++ "lol" ++ rgbOps ++ RTC
      assert(concated5 == expected5)

      val concated6 = fansi.Str.join(Nil, "")
      val expected6 = fansi.Str("")
      assert(concated6 == expected6)

      val concated7 = fansi.Str.join(Nil, ",")
      val expected7 = fansi.Str("")
      assert(concated7 == expected7)
    }
    test("get"){
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

    test("split"){
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
    test("substring"){
      val substringed = fansi.Str(rgbOps).substring(4, 9).render
      assert(substringed == s"$R--$G***$RTC")

      val default = fansi.Str(rgbOps).render

      val noOpSubstringed1 = fansi.Str(rgbOps).substring().render
      assert(noOpSubstringed1 == default)

      val parsed = fansi.Str(rgbOps)
      val noOpSubstringed2 = parsed.substring(0, parsed.length).render
      assert(noOpSubstringed2 == default)
    }

    test("overlay"){
      test("simple"){
        val overlayed = fansi.Str(rgbOps).overlay(fansi.Color.Yellow, 4, 7)
        val expected = s"+++$R-$Y--*$G**$B///$RTC"
        assert(overlayed.render == expected)
      }
      test("resetty"){
        val resetty = s"+$RES++$R--$RES-$RES$G***$B///"
        val overlayed = fansi.Str(resetty).overlay(fansi.Color.Yellow, 4, 7).render
        val expected = s"+++$R-$Y--*$G**$B///$RTC"
        assert(overlayed == expected)
      }
      test("mixedResetUnderline"){
        val resetty = s"+$RES++$R--$RES-$UND$G***$B///"
        val overlayed = fansi.Str(resetty).overlay(fansi.Color.Yellow, 4, 7).render.toVector
        val expected = s"+++$R-$Y--$UND*$G**$B///$DCOL$DUND".toVector

        assert(overlayed == expected)
      }
      test("underlines"){
        val resetty = s"$UND#$RES    $UND#$RES"
        test("underlineBug"){
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 2).render
          val expected = s"$UND$REV#$DUND $DREV   $UND#$DUND"
          assert(overlayed == expected)
        }
        test("barelyOverlapping"){
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 1).render
          val expected = s"$UND$REV#$DUND$DREV    $UND#$DUND"
          assert(overlayed == expected)
        }
        test("endOfLine"){
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 5, 6).render
          val expected = s"$UND#$DUND    $UND$REV#$DUND$DREV"
          assert(overlayed == expected)
        }
        test("overshoot"){
          intercept[IllegalArgumentException]{
            fansi.Str(resetty).overlay(fansi.Reversed.On, 5, 10)
          }
        }
        test("empty"){
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 0).render
          val expected = s"$UND#$DUND    $UND#$DUND"
          assert(overlayed == expected)
        }
        test("singleContent"){
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 2, 4).render
          val expected = s"$UND#$DUND $REV  $DREV $UND#$DUND"
          assert(overlayed == expected)

        }
      }
      test("overallAll"){
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
    test("attributes"){
      test{
        Console.RESET + fansi.Underlined.On
      }
      test{
        Console.RESET + (fansi.Underlined.On("Reset ") ++ fansi.Underlined.Off("Underlined"))
      }
      test{
        Console.RESET + fansi.Bold.On
      }
      test{
        Console.RESET + fansi.Bold.Faint
      }
      test{
        Console.RESET + (fansi.Bold.On("Reset ") ++ fansi.Bold.Off("Bold"))
      }
      test{
        Console.RESET + fansi.Reversed.On
      }
      test{
        Console.RESET + (fansi.Reversed.On("Reset ") ++ fansi.Reversed.Off("Reversed"))
      }
    }
    def tabulate(all: Seq[fansi.Attr]) = {
      println(
        all.map(attr => attr.toString + " " * (30 - attr.name.length))
        .grouped(3)
        .map(_.mkString)
        .mkString("\n")
      )
    }

    def square(all : Seq[fansi.Attr]) = {
      println(
        all.map( attr => attr.escapeOpt.getOrElse("") + "#")
        .grouped(32)
        .map(_.mkString)
        .mkString("\n")
      )
    }


    test("colors") - tabulate(fansi.Color.all)

    test("backgrounds") - tabulate(fansi.Back.all)

    test("trueColor"){
      test("red") - fansi.Color.True(255,0,0)

      test("redhexa") - fansi.Color.True(0xFF0000)

      test("green") - fansi.Color.True(0,255,0)

      test("greenhexa") - fansi.Color.True(0x00FF00)

      test("blue") - fansi.Color.True(0,0,255)

      test("bluehaxe") - fansi.Color.True(0x0000FF)

      test("256 shades of gray") - square(for(i <- 0 to 255) yield fansi.Color.True(i,i,i))

      test("trueColors") - tabulate(for(i <- Range(0, 0xFFFFFF, 10000)) yield fansi.Color.True(i))

      test("trueBackgrounds") - tabulate(for(i <- Range(0, 0xFFFFFF, 10000)) yield fansi.Back.True(i))

      test("blackState") - assert (fansi.Color.lookupAttr(273 << 4) == fansi.Color.True(0,0,0) )

      test("whitState") -  assert (fansi.Color.lookupAttr(16777488 << 4) == fansi.Color.True(255,255,255) )

      test("redState") -  assert (fansi.Color.lookupAttr((0xFF0000 + 273) << 4) == fansi.Color.True(255,0,0))

      test("lastFullState") - assert ( fansi.Color.lookupAttr(272 << 4) == fansi.Color.Full(255))

      test("parsing"){
        def check(frag: fansi.Str) = {
          val parsed = fansi.Str(frag.render)
          assert(parsed == frag)
          print(parsed)
        }
        test - check(fansi.Color.True(255, 0, 0)("lol"))
        test - check(fansi.Color.True(1, 234, 56)("lol"))
        test - check(fansi.Color.True(255, 255, 255)("lol"))
        test - check(fansi.Color.True(10000)("lol"))
        test{
          for(i <- 0 to 255) yield check(fansi.Color.True(i,i,i)("x"))
          println()
        }
        test - check(
          "#" + fansi.Color.True(127, 126, 0)("lol") + "omg" + fansi.Color.True(127, 126, 0)("wtf")
        )

        test - square(for(i <- 0 to 255) yield fansi.Color.True(i,i,i))


      }
      test("failure"){
        test("tooLongToParse"){
          test - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;0;0;256m", errorMode = fansi.ErrorMode.Throw).plainText.toSeq.map(_.toInt)
          }
          test - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;0;256;0m", errorMode = fansi.ErrorMode.Throw).plainText.toSeq.map(_.toInt)
          }
          test - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;256;0;0m", errorMode = fansi.ErrorMode.Throw).plainText.toSeq.map(_.toInt)
          }
          test - intercept[IllegalArgumentException]{
            fansi.Str("\u001b[38;2;1111;0;0m", errorMode = fansi.ErrorMode.Throw).plainText.toSeq.map(_.toInt)
          }
        }
        test("truncatedParsing"){
          val escape = fansi.Color.True(255, 0, 0).escape
          for (i <- 1 until escape.length - 1)
          yield intercept[IllegalArgumentException] {
            fansi.Str(escape.dropRight(i), errorMode = fansi.ErrorMode.Throw)
          }
        }
        test("args"){
          test - intercept[IllegalArgumentException]{ fansi.Color.True(256, 0, 0) }
          test - intercept[IllegalArgumentException]{ fansi.Color.True(0, 256, 0) }
          test - intercept[IllegalArgumentException]{ fansi.Color.True(0, 0, 256) }
          test - intercept[IllegalArgumentException]{ fansi.Color.True(-1, 0, 0) }
          test - intercept[IllegalArgumentException]{ fansi.Color.True(0, -1, 0) }
          test - intercept[IllegalArgumentException]{ fansi.Color.True(0, 0, -1) }
        }
      }
    }

    test("emitAnsiCodes"){
      test("basic") - assert(
        fansi.Attrs.emitAnsiCodes(0, fansi.Color.Red.applyMask) == Console.RED,
        fansi.Attrs.emitAnsiCodes(fansi.Color.Red.applyMask, 0) == fansi.Color.Reset.escape
      )
      test("combo"){
        // One color stomps over the other
        val colorColor = fansi.Color.Red ++ fansi.Color.Blue
        assert(fansi.Attrs.emitAnsiCodes(0, colorColor.applyMask) == Console.BLUE)


        val colorBold = fansi.Color.Red ++ fansi.Bold.On
        assert(fansi.Attrs.emitAnsiCodes(0, colorBold.applyMask) == Console.RED + Console.BOLD)
        // unlike Colors and Underlined and Reversed, Bold needs a hard reset,
        assert(fansi.Attrs.emitAnsiCodes(colorBold.applyMask, 0) == Console.RESET)
      }

    }

    test("negative"){
      test("errorMode"){
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
          val thrownError2 = intercept[IllegalArgumentException]{
            fansi.Str.Throw(s)
          }
          assert(thrownError2.getMessage.contains(msg))
          // If I ask it to sanitize, the escape character is gone but the
          // rest of each escape sequence remains
          val sanitized = fansi.Str(s, errorMode = fansi.ErrorMode.Sanitize)
          assert(sanitized.plainText == ("Hello" + msg + "World"))
          val sanitized2 = fansi.Str.Sanitize(s)
          assert(sanitized2.plainText == ("Hello" + msg + "World"))

          // If I ask it to strip, everything is gone
          val stripped = fansi.Str(s, errorMode = fansi.ErrorMode.Strip)
          assert(stripped.plainText == "HelloWorld")
          val stripped2 = fansi.Str.Strip(s)
          assert(stripped2.plainText == "HelloWorld")
        }

        test("cursorUp") - check("Hello\u001b[2AWorld", "[2A")
        test("cursorDown") - check("Hello\u001b[2BWorld", "[2B")
        test("cursorForward") - check("Hello\u001b[2CWorld", "[2C")
        test("cursorBack") - check("Hello\u001b[2DWorld", "[2D")
        test("cursorNextLine") - check("Hello\u001b[2EWorld", "[2E")
        test("cursorPrevLine") - check("Hello\u001b[2FWorld", "[2F")
        test("cursorHorizontalAbs") - check("Hello\u001b[2GWorld", "[2G")
        test("cursorPosition") - check("Hello\u001b[2;2HWorld", "[2;2H")
        test("eraseDisplay") - check("Hello\u001b[2JWorld", "[2J")
        test("eraseLine") - check("Hello\u001b[2KWorld", "[2K")
        test("scrollUp") - check("Hello\u001b[2SWorld", "[2S")
        test("scrollDown") - check("Hello\u001b[2TWorld", "[2T")
        test("horizontalVerticalPos") - check("Hello\u001b[2;2fWorld", "[2;2f")
        test("auxPortOn") - check("Hello\u001b[5iWorld", "[5i")
        test("auxPortOff") - check("Hello\u001b[4iWorld", "[4i")
        test("deviceStatusReport") - check("Hello\u001b[6nWorld", "[6n")
        test("saveCursor") - check("Hello\u001b[sWorld", "[s")
        test("restoreCursor") - check("Hello\u001b[uWorld", "[u")
      }
      test("outOfBounds"){
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(10) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(4) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(-1) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(0, 4)}
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(-1, 2)}
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(2, 1)}
      }
    }
    test("multipleAttrs"){
      test("identicalMasksGetCollapsed"){
        val redRed = fansi.Color.Red ++ fansi.Color.Red
        assert(
          redRed.resetMask == fansi.Color.Red.resetMask,
          redRed.applyMask == fansi.Color.Red.applyMask
        )
      }
      test("overlappingMasksGetReplaced"){
        val redBlue = fansi.Color.Red ++ fansi.Color.Blue
        assert(
          redBlue.resetMask == fansi.Color.Blue.resetMask,
          redBlue.applyMask == fansi.Color.Blue.applyMask
        )
      }
      test("semiOverlappingMasks"){
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
      test("separateMasksGetCombined"){
        val redBold = fansi.Color.Red ++ fansi.Bold.On

        assert(
          redBold.resetMask == (fansi.Color.Red.resetMask | fansi.Bold.On.resetMask),
          redBold.applyMask == (fansi.Color.Red.applyMask | fansi.Bold.On.applyMask)
        )
      }
      test("applicationWorks"){
        val redBlueBold = fansi.Color.Red ++ fansi.Color.Blue ++ fansi.Bold.On
        val colored = redBlueBold("Hello World")
        val separatelyColored = fansi.Bold.On(fansi.Color.Blue(fansi.Color.Red("Hello World")))
        assert(colored.render == separatelyColored.render)
      }
      test("equality"){
        assert(
          fansi.Color.Blue ++ fansi.Color.Red == fansi.Color.Red,
          fansi.Color.Red == fansi.Color.Blue ++ fansi.Color.Red,
          fansi.Bold.On ++ fansi.Color.Red != fansi.Color.Red,
          fansi.Color.Red != fansi.Bold.On ++ fansi.Color.Red
        )
      }
    }
//    test("perf"){
//      val input = s"+++$R---$G***$B///" * 1000
//
//      test("parsing"){
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
//      test("rendering"){
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
//      test("concat"){
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
//      test("splitAt"){
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
//      test("substring"){
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
//      test("overlay"){
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

