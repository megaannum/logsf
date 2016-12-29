package com.megaannum.logging.logsf

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.Stack

/** Logger for Scala Script and small applications).
 *
 * This logger is reasonably configurable. It has the following 3 
 * binary major modes: 
 * - Supports both Single Thread and Multi Thread applications.
 * - Allows for either Immediate output or a Buffering with flush to output.
 * - The output target may be Private (requiring no synchronized access)
 *    or Shared (even with external code doing the sharing).
 *
 * It is the expectation that for applications that use only a single thread 
 * that a version of a Single Thread Log would be used. Similarly, for
 * multi-threaded applications, a Multi Thread Log would be used.
 * For a Single Thread Log there is no synchronization on outputting 
 * the log messages while a Multi Thread Log can be particularized 
 * with different synchronization strategies.
 *
 * A Log can have its messages written out immediately (Immediate mode)
 * or they can be buffered (Buffered mode) and, generally, will only be 
 * written upon a call to flush.
 *
 * == How to Log ==
 *
 * In order to Log, the code must call one fo the Log's "logging" methods.
 *
 * There is the generic loggging method which takes a Log.Level.
 * 
 *{{{
 * Log.log(TRACE, "TRACE is the "lowest" log level")
 * Log.log(NOTICE, "NOTICE is just below the WARN level, normal but worth noting.")
 * Log.log(ALERT, """A severe situation requiring "prompt" action has occurred""")
 *}}}
 *
 * Helper logging methods.
 *
 *{{{
 * trace("Large amounts for information")
 * error("Something was not successful")
 * crit("Important problems need to be addressed")
 *}}}
 *
 * There is a "FORCE" log level that is used when a message should be logged
 * regardless of what kind of logger is used or what the current log level is.
 *
 *{{{
 * Log.force("""This message appears at "FORCE" level""")
 * Log.forceAs(INFO, """This message appears at INFO level""")
 *}}}
 *
 * When using a Buffered logger, messages are not output unless they are
 * flushed, unless the "flush" method is called.
 * Additionally, if the "clear: method is called, then ALL messages currently
 * being buffered are removed from the buffer and can no longer be logged.
 * Note that calling flush and clear on an Immediate Log does nothing.
 *
 *{{{
 *  Log.crit"Important problems need to be addressed"
 *  Log.flush // above message is output
 *  Log.alert("Severe situation prompt action needed")
 *  Log.emerg("System is not usable")
 *  Log.clear // above messages are not output
 *}}}
 *
 * == Example Usages ==
 * 
 * === Support Code ===
 *
 * Depending upon the characteristics of a Log instance, different
 * support code is needed. Below are some examples.
 *
 * Receives a Log message and does something with it. Any necessary
 * synchronization has happened in the Log code.
 *
 *{{{
 * def receive(msg: Message): Unit = ???
 * // one can also define a method to handle a List of Messages for bulk processing
 * def receive(msgs: List[Message]): Unit = ???
 *}}}
 *
 * An output object which the Log code uses to synchronizing with.
 *
 *{{{
 * val out = new Log.Out {
 *   def put(msg: Message): Unit = receive(msg)
 *   // also, can define
 *   // override def put(msgs: List[Message]): Unit = msgs.foreach(msg => receive(msg))
 * }
 *}}}
 *
 * This approach is used for Multi Threaded Log instances when each Thread's
 * Log instance will have its own output mechanism which needes to be
 * "closed" when the Thread ends.
 *
 *{{{
 * def outFn(msg: Message): Unit = receive(msg)
 * def closeFn(): Unit = { do resource close here }
 *}}}
 *
 * === 8 Kinds of Log instances ===
 *
 * Single Thread, Immediate (non-Buffered), Private output mechanism:
 *
 *{{{
 * val LogSIP = new Log.Single.ImmediatePrivate {
 *   protected def output(msg: Message): Unit = receive(msg)
 * }
 *}}}
 *
 * Single Thread, Immediate (non-Buffered), Shared output mechanism:
 *
 *{{{
 * val LogSIS = new Log.Single.ImmediateShared {
 *   override def out: Log.Out = out
 * }
 *}}}
 *
 * Single Thread, Buffered, Private output mechanism:
 *
 *{{{
 * val LogSBP = new Log.Single.BufferedPrivate {
 *   protected def output(msg: Message): Unit = receive(msg)
 * }
 *}}}
 *
 * Single Thread, Buffered, Shared output mechanism:
 *
 *{{{
 * val LogSBS = new Log.Single.BufferedShared {
 *   override def out: Log.Out = out
 * }
 *}}}
 *
 * Multi Thread, Immediate, Private output mechanism:
 *
 *{{{
 * val LogMIP = new Log.Multi.ImmediatePrivate {
 *   def makeOutFn(threadId: Long): Message => Unit = outFn
 *   override def makeCloseFn(threadId: Long): () => Unit = closeFn _
 * }
 *}}}
 *
 * Multi Thread, Immediate, Shared output mechanism:
 *
 *{{{
 * val LogMIS = new Log.Multi.ImmediateShared {
 *   def makeOutObject(threadId: Long): Out = out
 * }
 *}}}
 *
 * Multi Thread, Buffered, Private output mechanism:
 *
 *{{{
 * val LogMBP = new Log.Multi.BufferedPrivate {
 *   def makeOutFn(threadId: Long): Message => Unit = outFn
 *   override def makeCloseFn(threadId: Long): () => Unit = closeFn _
 * }
 *}}}
 *
 *
 * Multi Thread, Buffered, Shared output mechanism:
 *
 *{{{
 * val LogMBS = new Log.Multi.BufferedShared {
 *   def makeOutObject(threadId: Long): Out = out
 * }
 *}}}
 *
 * == Example Tailorings ==
 *
 * === Minimum Log Level ===
 *{{{
 *}}}
 * === Enter/Leave Log Level ===
 *{{{
 *}}}
 * === Flush ===
 *{{{
 *}}}
 * === Mark ===
 *{{{
 *}}}
 * === Force ===
 *{{{
 *}}}
 *
 * override protected def flushAndClearOn(msg: Message): Boolean = 
 *  msg.level >= Level.WARN
 *
 * == Log Blocks ==
 * def block
 *{{{
 *}}}
 * def mark
 *{{{
 *}}}
 * def batch
 *{{{
 *}}}
 * ifTrace ...
 *{{{
 *}}}
 */
object Log {

  // for debuging
  def printStack(nos: Int): Unit = {
    for (e <- new Throwable().getStackTrace().take(nos)) {
      println(s"${e.getClassName}:${e.getMethodName}:${e.getLineNumber}")
    }
  }

  class Message (
    val level: Log.Level.Type,
    val labelOp: Option[String],
    msg: => String,
    val throwableOp: Option[Throwable] = None,
    index: Int
  ) {
    def message: String = msg
    val timestamp = System.currentTimeMillis()
    val el = new Throwable().getStackTrace()(index)
    lazy val (className: String, methodName: String) = labelOp match {
      case Some(label) => label.indexOf(':') match {
            case -1 => ("", label)
            case n => (label.substring(0,n), label.substring(n+1))
          }
      case None => 
        (
          if (el.getClassName == null) "" else el.getClassName, 
          if (el.getMethodName == null) "" else el.getMethodName
        )
      
    }
    def lineNumber: Int = el.getLineNumber

    override def toString: String = labelOp match {
        case Some(label) =>
          s"$level:$timestamp:$label:$lineNumber:$message"
        case None =>
          s"$level:$timestamp:$className:$methodName:$lineNumber:$message"
      }
  }
  object Message {
    def apply(
      level: Log.Level.Type,
      labelOp: Option[String],
      message: => String,
      throwableOp: Option[Throwable] = None,
      index: Int
    ): Message = new Message(level, labelOp, message, throwableOp, index)
  }


  class Mark(val n: Int)
  val emptyMark = new Mark(0)

  // log levels (based upon Apache levels): 
  // emerg:  System is not usable
  // alert:  Severe situation prompt action needed
  // crit:   Important problems need to be addressed
  // error:  Something was not successful
  // warn:   Out of ordinary but not a cause for concern
  // notice: Normal but worth noting
  // info:   Nice to know
  // debug:  Useful for pinpointing problem area
  // trace:  Large amounts for information
  object Level extends Enumeration {
    type Type = Value

    val TRACE = Value(10, "TRACE")
    val DEBUG = Value(11, "DEBUG")
    val INFO = Value(12, "INFO")
    val NOTICE = Value(13, "NOTICE")
    val WARN = Value(14, "WARN")
    val ERROR = Value(15, "ERROR")
    val CRIT = Value(16, "CRIT")
    val ALERT = Value(17, "ALERT")
    val EMERG = Value(18, "EMERG")

    // For ENTER and LEAVE levels
    class V(val id: Int, val name: String) extends Value {
      override def toString: String = name
    }

    private[logsf] var ENTER = new V(10, "ENTER")
    private[logsf] var LEAVE = new V(10, "LEAVE")

    def setEnterLeaveLevel(level: Level.Type): Unit = {
      val id = level.id
      ENTER = new V(id, "ENTER")
      LEAVE = new V(id, "LEAVE")
    }

    class Force(val level: Level.Type) extends V(0, "FORCE") {
      override def toString: String = level.toString
    }
   
    def makeForce(level: Level.Type): Level.Type = new Force(level)

    // This is used to immediately output a message regardless of
    // any setting or buffering. It is NOT a normal log level
    // Also, FORCE.id should be less than TRACE.id so that flushAndClearOn
    // is not triggered when it occurs.
    // private[logsf] val FORCE = new V(0, "FORCE")
    private[logsf] val FORCE = new V(0, "FORCE")

    def isForce(level: Level.Type): Boolean = 
      level.toString == "FORCE" || level.isInstanceOf[Force]


    def toLevel(ls: String): Option[Level.Type] = ls.toUpperCase match {
      case "TRACE" => Some(Level.TRACE)
      case "DEBUG" => Some(Level.DEBUG)
      case "INFO" => Some(Level.INFO)
      case "NOTICE" => Some(Level.NOTICE)
      case "WARN" => Some(Level.WARN)
      case "ERROR" => Some(Level.ERROR)
      case "CRIT" => Some(Level.CRIT)
      case "ALERT" => Some(Level.ALERT)
      case "EMERG" => Some(Level.EMERG)
      case "ENTER" => Some(Level.ENTER)
      case "LEAVE" => Some(Level.LEAVE)
      case "FORCE" => Some(Level.FORCE)
      case _ => None
    }
  }

  def setEnterLeaveLevel(level: Level.Type): Unit = Level.setEnterLeaveLevel(level)



  trait LabelStack {
    protected def getStack: Stack[String]

    def pushLabel(label: String): Unit = {
      getStack.push(label)
    }
    def topLabel: Option[String] = {
      val s = getStack
      if (s.isEmpty) None else Some(getStack.top)
    }
    def popLabel: Unit = {
      val s = getStack
      if (! s.isEmpty) s.pop()
    }
  }


  trait GetMake {
    protected val perThreadLogMap = Map[Long, Log]()

    def get: Log = perThreadLogMap.synchronized {
      val threadId = Thread.currentThread.getId
      perThreadLogMap.getOrElseUpdate(threadId, make(threadId))
    }
    def make(threadId: Long): Log
    def cleanup: Unit = {
      val threadId = Thread.currentThread.getId
      perThreadLogMap.remove(threadId)
    }

  }

  trait Out {
    def put(msgs: List[Message]): Unit = msgs.foreach(msg => put(msg))
    def put(msg: Message): Unit
  }

  // Single Thread
  object Single {
    trait SingleLabelStack extends LabelStack {
      protected val labelStack = new Stack[String]()

      protected def getStack: Stack[String] = labelStack
    }

    // Immediate
    // must define  protected def output(msg: Message): Unit 
    trait ImmediatePrivate extends Log with SingleLabelStack {
      override protected def log(msg: Message): Unit = 
        if (minLevel <= msg.level) output(msg)
    }

    // Immediate
    // must define def out: Log.Out 
    trait ImmediateShared extends Log with SingleLabelStack {
      override protected def log(msg: Message): Unit = 
        if (minLevel <= msg.level) output(msg)

      def out: Log.Out
      override protected def output(msgs: List[Message]): Unit = out.synchronized { out.put(msgs) }
      override protected def output(msg: Message): Unit = out.synchronized { out.put(msg) }
    }

    // Buffered
    // must define def output(msg: Message): Unit
    trait BufferedPrivate extends Log with SingleLabelStack {
      protected val buf = ListBuffer[Message]()

      override def log(msg: Message): Unit = {
        if (forceEnabled && Level.isForce(msg.level)) output(msg)
        else buf += msg

        if (flushAndClearOn(msg)) {
          flushAll
        }
      }
      override def flushAll: Unit = {
        output(buf.toList)
        clear
      }
      override def flush: Unit = {
        buf.foreach{msg=> if (minLevel <= msg.level) output(msg) }
        clear
      }
      override def clear: Unit = buf.clear

      override def makeMark: Mark = new Mark(buf.length)
      override def clearToMark(mark: Mark): Unit = {
        buf.remove(mark.n,buf.length-mark.n)
      }
    }

    // Buffered
    // must define def out: Log.Out
    trait BufferedShared extends Log with SingleLabelStack {
      protected val buf = ListBuffer[Message]()

      def out: Log.Out
      override def log(msg: Message): Unit = {
        if (forceEnabled && Level.isForce(msg.level)) output(msg)
        else buf += msg

        if (flushAndClearOn(msg)) {
          flushAll
        }
      }
      override def flushAll: Unit = {
        output(buf.toList)
          clear
      }
      override def flush: Unit = {
        buf.foreach{msg=> if (minLevel <= msg.level) output(msg) }
        clear
      }
      override def clear: Unit = buf.clear

      override protected def output(msgs: List[Message]): Unit = out.synchronized { out.put(msgs) }
      override protected def output(msg: Message): Unit = out.synchronized { out.put(msg) }

      override def makeMark: Mark = new Mark(buf.length)
      override def clearToMark(mark: Mark): Unit = {
        buf.remove(mark.n,buf.length-mark.n)
      }
    }
  }

  // Multi Thread
  object Multi {
    trait MultiLabelStack extends LabelStack {
      protected val perThreadLabelMap = Map[Long, Stack[String]]()
      protected def getStack: Stack[String] = perThreadLabelMap.synchronized {
        val threadId = Thread.currentThread.getId
        perThreadLabelMap.getOrElseUpdate(threadId, new Stack[String]())
      }
    }

    // Immediate
    // must define def makeOutFn(threadId: Long): Message => Unit
    trait ImmediatePrivate extends Log with Log.GetMake with MultiLabelStack {
      override protected def log(msg: Message): Unit = 
        if (minLevel <= msg.level) output(msg)

      protected def output(msg: Message): Unit = get.output(msg)
      override def close: Unit = get.close
      override def minLevel_=(level: Level.Type): Unit = {
        _minLevel = level
        get.minLevel = level
      }
      override def forceEnabled_=(b: Boolean): Unit = {
        _forceEnabled = b
        get.forceEnabled = b
      }

      def make(threadId: Long): Log = {
        val log = new Log.Single.ImmediatePrivate {
                    val ofn: Message => Unit = makeOutFn(threadId)
                    val cfn: () => Unit = makeCloseFn(threadId)

                    protected def output(msg: Message): Unit = ofn(msg)
                    override def close: Unit = { cfn() }
                  }
        log.minLevel = minLevel
        log
      }

      def makeOutFn(threadId: Long): Message => Unit
      def makeCloseFn(threadId: Long): () => Unit = {()=> }

    }

    // Immediate
    // must define def makeOutObject(threadId: Long): Out
    trait ImmediateShared extends Log with Log.GetMake with MultiLabelStack {
      override protected def log(msg: Message): Unit = 
        if (minLevel <= msg.level) output(msg)

      protected def output(msg: Message): Unit = get.output(msg)
      override def close: Unit = get.close
      override def minLevel_=(level: Level.Type): Unit = {
        _minLevel = level
        get.minLevel = level
      }
      override def forceEnabled_=(b: Boolean): Unit = {
        _forceEnabled = b
        get.forceEnabled = b
      }

      def make(threadId: Long): Log = {
        val log = new Log.Single.ImmediateShared {
                    val outObj: Out = makeOutObject(threadId)
                    override def out: Out = outObj
                  }
        log.minLevel = minLevel
        log
      }
      
      def makeOutObject(threadId: Long): Out
    }


    // Buffered
    // must define def makeOutFn(threadId: Long): Message => Unit
    trait BufferedPrivate extends Log with Log.GetMake with MultiLabelStack {
      override def flush: Unit = get.flush
      override def flushAll: Unit = get.flushAll
      override def clear: Unit = get.clear

      protected def output(msg: Message): Unit = {
        get.log(msg)

        if (flushAndClearOn(msg)) {
          flushAll
          clear
        }
      }
      override def close: Unit = get.close
      override def minLevel_=(level: Level.Type): Unit = {
        _minLevel=level
        get.minLevel = level
      }
      override def forceEnabled_=(b: Boolean): Unit = {
        _forceEnabled = b
        get.forceEnabled = b
      }

      def make(threadId: Long): Log = {
        val log = new Log.Single.BufferedPrivate {
                    val ofn: Message => Unit = makeOutFn(threadId)
                    val cfn: () => Unit = makeCloseFn(threadId)

                    protected def output(msg: Message): Unit = ofn(msg)
                    override def close: Unit = { cfn() }
                  }
        log.minLevel = minLevel
        log
      }

      def makeOutFn(threadId: Long): Message => Unit
      def makeCloseFn(threadId: Long): () => Unit = {()=> }

    }

    // Buffered
    // must define def makeOutObject(threadId: Long): Out
    trait BufferedShared extends Log with Log.GetMake with MultiLabelStack {
      override def flush: Unit = get.flush
      override def flushAll: Unit = get.flushAll
      override def clear: Unit = get.clear

      protected def output(msg: Message): Unit = {
        get.log(msg)

        if (flushAndClearOn(msg)) {
          flushAll
          clear
        }
      }
      override def close: Unit = get.close
      override def minLevel_=(level: Level.Type): Unit = {
        _minLevel=level
        get.minLevel = level
      }
      override def forceEnabled_=(b: Boolean): Unit = {
        _forceEnabled = b
        get.forceEnabled = b
      }

      def make(threadId: Long): Log =  {
        val log = new Log.Single.BufferedShared {
                    val outObj: Out = makeOutObject(threadId)
                    override def out: Out = outObj
                  }
        log.minLevel = minLevel
        log
      }
      
      def makeOutObject(threadId: Long): Out

    }
  }
}

/** Primary logging interface with 8 specialized in the Log 
 *  object [[com.megaannum.logging.logsf.Log]] into sub-traits with behaviors
 *  depending upon: 
 *    buffered or non-buffered output,
 *    single or multi threaded applications and
 *    private or shared output capabilities.
 *
 */
trait Log extends Log.LabelStack {
  import Log._
  import Log.Level._

  protected var _minLevel: Level.Type = Level.NOTICE

  def minLevel = _minLevel
  def minLevel_=(level: Level.Type): Unit = _minLevel = level

  def minLevel_=(ls: String): Unit = Log.Level.toLevel(ls) match {
      case Some(level) => minLevel = level
      case None => warn(s"Unknown:log level name:$ls")
    }


  def setEnterLeaveLevel(level: Level.Type): Unit = Log.setEnterLeaveLevel(level)

  /** Block blocks apply to all loggers allowing code to 
   * generate Enter and Leave log messages.
   *
   * Typical usage:
   *{{{
   * class Foo {
   *  def bar(msg: String): Int = block {
   *   ... code ...
   *  }
   * }
   *}}}
   *
   * @tparam T return value type
   * @param code the code to be executed in block
   * @return whatever the code block returns
   */
  def block[T](code: => T): T = {
    if (minLevel <= ENTER) {
      val start = System.currentTimeMillis
      enter
      try {
        code
      } finally {
        val end = System.currentTimeMillis
        val diff = end - start
        leave(diff)
      }
    } else {
      code
    }
  }

  /** Label Block blocks apply to all loggers allowing code to specify the
   * className:methodName associate with the block of code and
   * Enter and Leave log messages are generated by this block.
   *
   * Typical usage:
   *{{{
   * class Foo {
   *  def bar(msg: String): Int = block("Foo:bar") {
   *   ... code ...
   *  }
   * }
   *}}}
   *
   * @tparam T return value type
   * @param label the class:method name
   * @param code the code to be executed in block
   * @return whatever the code block returns
   */
  def block[T](label: String)(code: => T): T = {
    pushLabel(label)
    try {
      if (minLevel <= ENTER) {
        val start = System.currentTimeMillis
        enter(label)
        try {
          code
        } finally {
          val end = System.currentTimeMillis
          val diff = end - start
          leave(label, diff)
        }
      } else {
        code
      }
    } finally {
      popLabel
    }
  }

  /** Mark blocks apply to Buffered loggers and should be used if one 
   * wishes to ignore non-flushed log messages within the block and
   * Enter and Leave log messages are generated by this block.
   *
   * Mark blocks have no effect on Immediate loggers.
   * For Buffered loggers, unless the logger is flushed within the
   * mark block, messages are flushed (those whose level is equal to or
   * above the current minLevel) and then all log messages are removed from 
   * the Buffer on block exit.
   *
   * Generally, the minLevel is set to INFO or NOTICE while the 
   * flushAndClearOn value is at WARN. So, if no WARN or above occurred 
   * in the mark block, then at the mark block exit, all of the
   * NOTICE (and INFO depending on minLevel setting) messages are 
   * flushed and all message that where within the mark block are
   * removed from the buffer.
   *
   * Typical usage:
   *{{{
   * class Foo {
   *  def bar(msg: String): Int = mark("Foo:bar") {
   *   ... code ...
   *  }
   * }
   *}}}
   *
   * @tparam T return value type
   * @param label the class:method name
   * @param code the code to be executed in block
   * @return whatever the code block returns
   */
  def mark[T](label: String)(code: => T): T = {
    val mark = makeMark
    pushLabel(label)
    try {
      if (minLevel <= ENTER) {
        val start = System.currentTimeMillis
        enter(label)
        try {
          code
        } finally {
          val end = System.currentTimeMillis
          val diff = end - start
          leave(label, diff)
        }
      } else {
        code
      }
    } finally {
      popLabel
      if (doMark) {
        clearToMark(mark)
      }
    }
  }

  /** Batch blocks apply to Buffered loggers and are the same as:
   * {{{
   * try { code } finally { logger.flush }
   * }}}
   * flushes at the end of executing the code block.
   *
   * Batch blocks have no effect on Immediate loggers.
   *
   * @tparam T return value type
   * @param code the code to be executed in block
   * @return whatever the code block returns
   */
  def batch[T](code: => T): T = try { code } finally { flush } 


  // can override
  def stackIndex: Int = 7

  // can override
  def trimClassName: Int = 13

  // can override
  def adjustClassName(cn: String): String = cn

  def adjustClassNameScalaScript(cn: String): String = {
    val cn1 = cn.replaceAll("\\$anonfun\\$", "")
    val cn2 = cn1.replaceAll("\\$.\\$apply", "")
    var i = cn2.length-1
    while (i > 0 && Character.isDigit(cn2(i))) i -= 1
    if (i > 0 && cn2(i) == '$') i -= 1
    val cn3 = cn2.take(i+1)
    cn3
  }

  // can override
  def adjustMethodName(mn: String, cn: String): String = cn

  def adjustMethodNameScalaScript(mn: String, cn: String): String = {
    // drop "Main$$anon$"
    val mn1 = if (mn.startsWith("Main$$anon$")) mn.drop(11) else mn

    // remove all "$anonfun$"
    val mn2 = mn1.replaceAll("\\$anonfun\\$", "")

    // remove what appears in classname
    var i = 0
    while (i < cn.length && i < mn2.length && cn(i) == mn2(i)) i += 1
    val mn3 = mn2.drop(i)

    // remove somename$nnnn
    i = mn3.length-1
    while (i > 0 && Character.isDigit(mn3(i))) i -= 1
    val mn4 = if (i > 0 && mn3(i) == '$') mn3.take(i) else mn3

    if (mn4.startsWith("apply$")) "apply"
    else if (mn4.length > 0 && mn4(0) == '$') mn4.drop(1)
    else mn4
  }

  def currentStackInfo(index: Int): Tuple3[String,String,Int] = {
    val st = new Throwable().getStackTrace()(index)
    (st.getClassName, st.getMethodName, st.getLineNumber)
  }
  def getPrefix(index: Int): Tuple3[String, String, Int] = {
    val el = new Throwable().getStackTrace()(index)
    // val el = Thread.currentThread.getStackTrace()(index)
    val ln = el.getLineNumber
    val cn = adjustClassName(el.getClassName.drop(trimClassName))
    val mn = adjustMethodName(el.getMethodName, cn)
    Tuple3(cn, mn, ln)
  }


  def ifTrace(code: => Unit): Unit = if (minLevel <= TRACE) code
  def ifDebug(code: => Unit): Unit = if (minLevel <= DEBUG) code
  def ifInfo(code: => Unit): Unit = if (minLevel <= INFO) code
  def ifNotice(code: => Unit): Unit = if (minLevel <= NOTICE) code
  def ifWarn(code: => Unit): Unit = if (minLevel <= WARN) code
  def ifError(code: => Unit): Unit = if (minLevel <= ERROR) code
  def ifCrit(code: => Unit): Unit = if (minLevel <= CRIT) code
  def ifAlert(code: => Unit): Unit = if (minLevel <= ALERT) code
  def ifEmerg(code: => Unit): Unit = if (minLevel <= EMERG) code

  protected def dolog(
    level: Level.Type, 
    msg: => String, 
    throwableOp: Option[Throwable],
    index: Int
  ): Unit = 
    log(Message(level, topLabel, msg, throwableOp, index))

  protected def log(msg: Message): Unit = output(msg)

  /** For Buffered Loggers, flushs stored messages to output.
   *
   * This is redefined in the Single Buffered loggers to both flush
   * the stored messages and then clears the storage by calling "clear".
   */
  def flush: Unit = {}

  def flushAll: Unit = flush
  //protected def flushAll: Unit = flush

  /** For Buffered Loggers, flushs and clears based upon Message.
   *
   * This can be redefined to act on, for instance, when Message is
   * at a given log level.
   */
  protected def flushAndClearOn(msg: Message): Boolean = false

  /** For Buffered Loggers, clears stored messages without outputting.
   *
   */
  def clear: Unit = {}

  var doMark: Boolean = true
  def makeMark: Mark = emptyMark
  def clearToMark(mark: Mark): Unit = {}

  def close: Unit = {}

  protected def output(msgs: List[Message]): Unit = msgs.foreach(output(_))

  ////////////////////////////////////////////////////////////////
  // must define
  protected def output(msg: Message): Unit
  // must define
  ////////////////////////////////////////////////////////////////
  
  // getting this value correct may depend upon Scala version
  // val levelIndex = 6
  val levelIndex = 8

  def log(level: Level.Type, msg: => String): Unit = 
    dolog(level, msg, None, levelIndex)
  def log(level: Level.Type, msg: => String, throwable: Throwable): Unit = 
    dolog(level, msg, Some(throwable), levelIndex)

  protected var _forceEnabled: Boolean = true

  def forceEnabled = _forceEnabled
  def forceEnabled_=(b: Boolean): Unit = _forceEnabled = b

  def force(msg: => String): Unit = 
    dolog(FORCE, msg, None, levelIndex)
  def force(msg: => String, throwable: Throwable): Unit = 
    dolog(FORCE, msg, Some(throwable), levelIndex)

  def forceAs(level: Level.Type, msg: => String): Unit = 
    dolog(Level.makeForce(level), msg, None, levelIndex)
  def forceAs(level: Level.Type, msg: => String, throwable: Throwable): Unit = 
    dolog(Level.makeForce(level), msg, Some(throwable), levelIndex)

  def trace(msg: => String): Unit = 
    dolog(TRACE, msg, None, levelIndex)
  def trace(msg: => String, throwable: Throwable): Unit = 
    dolog(TRACE, msg, Some(throwable), levelIndex)

  def debug(msg: => String): Unit = 
    dolog(DEBUG, msg, None, levelIndex)
  def debug(msg: => String, throwable: Throwable): Unit = 
    dolog(DEBUG, msg, Some(throwable), levelIndex)

  def info(msg: => String): Unit = 
    dolog(INFO, msg, None, levelIndex)
  def info(msg: => String, throwable: Throwable): Unit = 
    dolog(INFO, msg, Some(throwable), levelIndex)

  def notice(msg: => String): Unit = 
    dolog(NOTICE, msg, None, levelIndex)
  def notice(msg: => String, throwable: Throwable): Unit = 
    dolog(NOTICE, msg, Some(throwable), levelIndex)

  def warn(msg: => String): Unit = 
    dolog(WARN, msg, None, levelIndex)
  def warn(msg: => String, throwable: Throwable): Unit = 
    dolog(WARN, msg, Some(throwable), levelIndex)

  def error(msg: => String): Unit = 
    dolog(ERROR, msg, None, levelIndex)
  def error(msg: => String, throwable: Throwable): Unit = 
    dolog(ERROR, msg, Some(throwable), levelIndex)

  def crit(msg: => String): Unit = 
    dolog(CRIT, msg, None, levelIndex)
  def crit(msg: => String, throwable: Throwable): Unit = 
    dolog(CRIT, msg, Some(throwable), levelIndex)

  def alert(msg: => String): Unit = 
    dolog(ALERT, msg, None, levelIndex)
  def alert(msg: => String, throwable: Throwable): Unit = 
    dolog(ALERT, msg, Some(throwable), levelIndex)

  def emerg(msg: => String): Unit = 
    dolog(EMERG, msg, None, levelIndex)
  def emerg(msg: => String, throwable: Throwable): Unit = 
    dolog(EMERG, msg, Some(throwable), levelIndex)

  val enterLeaveIndex = 9

  def enter: Unit = 
    dolog(ENTER, "ENTER", None, enterLeaveIndex)
  def enter(label: String): Unit = 
    dolog(ENTER, label, None, enterLeaveIndex)

  def leave(diff: Long): Unit = 
    dolog(LEAVE, s"LEAVE: $diff", None, enterLeaveIndex)
  def leave(label: String, diff: Long): Unit = 
    dolog(LEAVE, s"$label $diff", None, enterLeaveIndex)
}




// The following code will cause an immediate compile time error if
// something above is messed up.
object LogExample {
  import Log._

  val LogSIP = new Log.Single.ImmediatePrivate {
    protected def output(msg: Message): Unit = ???
  }

  val outSIS = new Out {
    def put(msg: Message): Unit = msg.toString
  }
  val LogSIS = new Log.Single.ImmediateShared {
    override def out: Log.Out = outSIS
  }


  val LogSBP = new Log.Single.BufferedPrivate {
    protected def output(msg: Message): Unit = msg.toString
  }
  val outSBS = new Out {
    def put(msg: Message): Unit = msg.toString
  }
  val LogSBS = new Log.Single.BufferedShared {
    override def out: Log.Out = outSBS
  }


  val LogMIP = new Log.Multi.ImmediatePrivate {
    def makeOutFn(threadId: Long): Message => Unit = ???
  }

  val outMIS = new Out {
    def put(msg: Message): Unit = msg.toString
  }
  val LogMIS = new Log.Multi.ImmediateShared {
    def makeOutObject(threadId: Long): Out = outMIS
  }


  val LogMBP = new Log.Multi.BufferedPrivate {
    def makeOutFn(threadId: Long): Message => Unit = ???
  }

  val outMBS = new Out {
    def put(msg: Message): Unit = msg.toString
  }
  val LogMBS = new Log.Multi.BufferedShared {
    def makeOutObject(threadId: Long): Out = outMBS
  }


  def foo(s: String): Unit = LogSIP.block("LogExample:foo") {
    LogSIP.trace(s"HI: $s")
  }

  def bar(s: String): Unit = LogSIP.block {
    LogSIP.trace(s"HI: $s")
  }
}
