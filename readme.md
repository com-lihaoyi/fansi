Fansi 0.1.0
===========

```scala
"com.lihaoyi" % "fansi" % "0.1.1"
```

Fansi is a Scala library to make it easy to deal with colored Ansi 
strings within your command-line programs.

Unlike normal `java.lang.String`s with Ansi escapes embedded inside,
`fansi.Str` allows you to perform a range of operations in an efficient
manner:

- Extracting the non-Ansi `plainText` version of the string

- Get the non-Ansi `length`

- Concatenate colored Ansi strings without worrying about leaking
  colors between them

- Applying colors to certain portions of an existing `fansi.Str`,
  and ensuring that the newly-applied colors get properly terminated
  while existing colors are unchanged

- Splitting colored Ansi strings at a `plainText` index

- Rendering to colored `java.lang.String`s with Ansi escapes embedded,
  which can be passed around or concatenated without worrying about
  leaking colors.

These are tasks which are possible to do with normal `java.lang.String`,
but are tedious, error-prone and typically inefficient. `fansi.Str`
allows you to perform these tasks safely and easily.


The main operations you need to know are:

- `fansi.Str(raw: CharSequence): fansi.String`, to construct colored
  Ansi strings from plain (or colored) text

- `fansi.Str`, the primary data-type that you will use to pass-around
  colored Ansi strings and manipulate them: concatenating, splitting,
  applying or removing colors, etc.

- `fansi.Bold.{On, Off}`, `fansi.Reversed.{On, Off}`, `fansi.Underlined.{On, Off}`,
  `fansi.Color.*`, `fansi.Back.*`, `fansi.Attr.Reset`: `fansi.Attr`s that you use to
  apply (or remove) colors and other decorations from `fansi.Str`s.

