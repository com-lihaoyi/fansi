package test.fansi

import utest._


object Scala2FansiTests extends TestSuite{

  val tests = TestSuite{

    test("implicitConstructorOnlyForLiterals"){
      compileError("""{val x = ""; x: fansi.Str }""")
      "": fansi.Str
    }
  }
}

