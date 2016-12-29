package com.megaannum.logging.logsf


import scala.collection.mutable.ListBuffer

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.concurrent.Conductors

import com.megaannum.logging.logsf.Log._
import com.megaannum.logging.logsf.Log.Level._

class LogSpec extends FlatSpec with Matchers with Conductors {


  def receive(msg: Message): Unit = {
    levelNameList = msg.level.toString :: levelNameList
    labelOpList = msg.labelOp :: labelOpList
    classNameList = msg.className :: classNameList
    methodNameList = msg.methodName :: methodNameList
    lineNumberList = msg.lineNumber :: lineNumberList
    messageList = msg.message :: messageList
    throwableOpList = msg.throwableOp :: throwableOpList
  }

  var levelNameList: List[String] = Nil
  var labelOpList: List[Option[String]] = Nil
  var timestampList: List[Long] = Nil
  var classNameList: List[String] = Nil
  var methodNameList: List[String] = Nil
  var lineNumberList: List[Int] = Nil
  var messageList: List[String] = Nil
  var throwableOpList: List[Option[Throwable]] = Nil

  var timestampEnterList: List[Long] = Nil
  var classNameEnterList: List[String] = Nil
  var methodNameEnterList: List[String] = Nil
  var lineNumberEnterList: List[Int] = Nil

  var timestampLeaveList: List[Long] = Nil
  var classNameLeaveList: List[String] = Nil
  var methodNameLeaveList: List[String] = Nil
  var durationLeaveList: List[Long] = Nil

  //  var contextList: List[Context] = Nil

  def reset: Unit = {
    levelNameList = Nil
    labelOpList = Nil
    timestampList = Nil
    classNameList = Nil
    methodNameList = Nil
    lineNumberList = Nil
    messageList = Nil
    throwableOpList = Nil

    timestampEnterList = Nil
    classNameEnterList = Nil
    methodNameEnterList = Nil
    lineNumberEnterList = Nil

    timestampLeaveList = Nil
    classNameLeaveList = Nil
    methodNameLeaveList = Nil
    durationLeaveList = Nil
  }


  it should "support single thread immediate private" in {
    reset
    val LogSIP = new Log.Single.ImmediatePrivate {
      protected def output(msg: Message): Unit = receive(msg)
    }
    LogSIP.minLevel = TRACE

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    // Log.printStack(7)
    
    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    LogSIP.trace("Msg1") // ln+2
    levelNameList should be (List("TRACE"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$1"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+2))
    messageList should be (List("Msg1"))
    reset

    levelNameList should be (List())
    messageList should be (List())
    reset

    LogSIP.trace("Msg1") // ln+14
    LogSIP.info("Msg2")
    LogSIP.warn("Msg3")
    levelNameList should be (List("WARN", "INFO", "TRACE"))
    lineNumberList should be (List(ln+16, ln+15, ln+14))
    messageList should be (List("Msg3", "Msg2", "Msg1"))

    reset
  }

  it should "support single thread immediate shared" in {
    reset
    val outSIS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }

    val LogSIS = new Log.Single.ImmediateShared {
      override def out: Log.Out = outSIS
    }
    LogSIS.minLevel = TRACE

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    LogSIS.trace("Msg1") // ln+2
    levelNameList should be (List("TRACE"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$2"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+2))
    messageList should be (List("Msg1"))
    reset

    levelNameList should be (List())
    messageList should be (List())
    reset

    LogSIS.trace("Msg1") // ln+14
    LogSIS.info("Msg2")
    LogSIS.warn("Msg3")
    levelNameList should be (List("WARN", "INFO", "TRACE"))
    lineNumberList should be (List(ln+16, ln+15, ln+14))
    messageList should be (List("Msg3", "Msg2", "Msg1"))
    reset
  }


  it should "support single thread buffered private" in {
    reset
    val LogSBP = new Log.Single.BufferedPrivate {
      protected def output(msg: Message): Unit = receive(msg)
    }
    LogSBP.minLevel = TRACE

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    //Log.printStack(7)

    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    LogSBP.trace("Msg1") // ln+2
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    LogSBP.clear
    reset

    LogSBP.trace("Msg2") // ln+11
    LogSBP.flush
    levelNameList should be (List("TRACE"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$3"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+11))
    messageList should be (List("Msg2"))
    reset

    LogSBP.trace("Msg3") // ln+20
    LogSBP.clear
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    LogSBP.trace("Msg3") // ln+29
    LogSBP.clear
    LogSBP.flush
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    LogSBP.trace("Msg1") // ln+39
    LogSBP.info("Msg2")
    LogSBP.warn("Msg3")
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())

    LogSBP.flush
    levelNameList should be (List("WARN", "INFO", "TRACE"))
    lineNumberList should be (List(ln+41, ln+40, ln+39))
    messageList should be (List("Msg3", "Msg2", "Msg1"))
    reset
  }
  it should "support single thread buffered shared" in {
    reset

    val outSBS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }
    val LogSBS = new Log.Single.BufferedShared {
      override def out: Log.Out = outSBS
    }
    LogSBS.minLevel = TRACE

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    LogSBS.trace("Msg1") // ln+2
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    LogSBS.clear
    reset

    LogSBS.trace("Msg2") // ln+11
    LogSBS.flush
    levelNameList should be (List("TRACE"))
    // classNameList should be (List("com.omegaannum.logging.logsf.LogSpec$$anonfun$4"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+11))
    messageList should be (List("Msg2"))
    reset

    LogSBS.trace("Msg3") // ln+20
    LogSBS.clear
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    LogSBS.trace("Msg3") // ln+29
    LogSBS.clear
    LogSBS.flush
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    LogSBS.trace("Msg1") // ln+39
    LogSBS.info("Msg2")
    LogSBS.warn("Msg3")
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())

    LogSBS.flush
    levelNameList should be (List("WARN", "INFO", "TRACE"))
    lineNumberList should be (List(ln+41, ln+40, ln+39))
    messageList should be (List("Msg3", "Msg2", "Msg1"))
    reset
  }

  it should "support multi thread immediate private" in {
    reset

    var didClose = false
    def outFn(msg: Message): Unit = receive(msg)
    def closeFn(): Unit = { didClose = true }

    val LogMIP = new Log.Multi.ImmediatePrivate {
      def makeOutFn(threadId: Long): Message => Unit = outFn
      override def makeCloseFn(threadId: Long): () => Unit = closeFn _
    }
    LogMIP.minLevel = TRACE

    try {

      levelNameList should be (List())
      classNameList should be (List())
      methodNameList should be (List())
      lineNumberList should be (List())
      messageList should be (List())
      reset

      // if you add lines below this, you need to adjust linenumber test
      val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

      LogMIP.info("Msg44") // ln+2
      levelNameList should be (List("INFO"))
      // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$10"))
      // methodNameList should be (List("apply"))
      lineNumberList should be (List(ln+2))
      messageList should be (List("Msg44"))
      reset

      didClose should be (false)

    } finally {
      LogMIP.close
    }

    didClose should be (true)
  }

  it should "support multi thread immediate shared" in {
    reset

    val outMIS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }

    val LogMIS = new Log.Multi.ImmediateShared {
      def makeOutObject(threadId: Long): Out = outMIS
    }

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    LogMIS.warn("Msg33") // ln+2
    levelNameList should be (List("WARN"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$5"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+2))
    messageList should be (List("Msg33"))
    reset
  }
  it should "support multi thread buffered private" in {
    reset

    var didClose = false
    def outFn(msg: Message): Unit = receive(msg)
    def closeFn(): Unit = { didClose = true }

    val LogMBP = new Log.Multi.BufferedPrivate {
      def makeOutFn(threadId: Long): Message => Unit = outFn
      override def makeCloseFn(threadId: Long): () => Unit = closeFn _
    }

    try {

      levelNameList should be (List())
      classNameList should be (List())
      methodNameList should be (List())
      lineNumberList should be (List())
      messageList should be (List())
      reset

      // if you add lines below this, you need to adjust linenumber test
      val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

      LogMBP.info("Msg53") // ln+2
      levelNameList should be (List())
      classNameList should be (List())
      methodNameList should be (List())
      lineNumberList should be (List())
      messageList should be (List())
      LogMBP.clear
      reset

      LogMBP.alert("Msg54") // ln+11
      LogMBP.flush
      levelNameList should be (List("ALERT"))
      // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$11"))
      // methodNameList should be (List("apply"))
      lineNumberList should be (List(ln+11))
      messageList should be (List("Msg54"))
      reset

      didClose should be (false)

    } finally {
      LogMBP.close
    }

    didClose should be (true)
  }

  it should "support multi thread buffered shared" in {
    reset

    val outMCS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }

    val LogMBS = new Log.Multi.BufferedShared {
      def makeOutObject(threadId: Long): Out = outMCS
    }

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    LogMBS.notice("Msg51") // ln+2
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    LogMBS.clear
    reset

    LogMBS.notice("Msg50") // ln+11
    LogMBS.flush
    levelNameList should be (List("NOTICE"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$6"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+11))
    messageList should be (List("Msg50"))
    reset
  }

  it should "support single thread immediate private set log level" in {
    reset
    val log = new Log.Single.ImmediatePrivate {
      protected def output(msg: Message): Unit = receive(msg)
    }

    log.minLevel = "TRACE"
    log.minLevel should be (Level.TRACE)

    log.minLevel = Level.TRACE
    log.minLevel should be (Level.TRACE)

    log.minLevel = "INFO"
    log.minLevel should be (Level.INFO)

    log.minLevel = Level.INFO
    log.minLevel should be (Level.INFO)

    log.minLevel = "CRIT"
    log.minLevel should be (Level.CRIT)

    log.minLevel = Level.CRIT
    log.minLevel should be (Level.CRIT)
  }

  it should "support multi thread immediate private set log level" in {
    reset

    def outFn(msg: Message): Unit = receive(msg)
    val log = new Log.Multi.ImmediatePrivate {
      def makeOutFn(threadId: Long): Message => Unit = outFn
    }

    // Note that changing container Log does not change child Log 
    log.minLevel = "TRACE"
    log.minLevel should be (Level.TRACE)
    log.get.minLevel should be (Level.TRACE)

    log.minLevel = Level.TRACE
    log.minLevel should be (Level.TRACE)
    log.get.minLevel should be (Level.TRACE)

    log.minLevel = "INFO"
    log.minLevel should be (Level.INFO)
    log.get.minLevel should be (Level.INFO)

    log.minLevel = Level.INFO
    log.minLevel should be (Level.INFO)
    log.get.minLevel should be (Level.INFO)

    log.minLevel = "CRIT"
    log.minLevel should be (Level.CRIT)
    log.get.minLevel should be (Level.CRIT)

    log.minLevel = Level.CRIT
    log.minLevel should be (Level.CRIT)
    log.get.minLevel should be (Level.CRIT)
  }

  it should "Log single thread immediate private at given level" in {
    reset

    val log = new Log.Single.ImmediatePrivate {
      protected def output(msg: Message): Unit = receive(msg)
    }

    log.minLevel = "TRACE"

    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    log.trace("Msg 1") // ln+2
    levelNameList should be (List("TRACE"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$7"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+2))
    messageList should be (List("Msg 1"))
    reset

    log.minLevel = "WARN"

    log.trace("Msg 2") // ln+12
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())

    log.warn("Msg 2") // ln+19
    levelNameList should be (List("WARN"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$7"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+19))
    messageList should be (List("Msg 2"))
    reset

    log.crit("Msg 1") // ln+27
    levelNameList should be (List("CRIT"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$7"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+27))
    messageList should be (List("Msg 1"))
    reset

    log.minLevel = "WARN"

    log.trace("Msg 1") // ln+37
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    log.warn("Msg 4") // ln+45
    levelNameList should be (List("WARN"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$7"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+45))
    messageList should be (List("Msg 4"))
    reset

    log.crit("Msg 5") // ln+53
    levelNameList should be (List("CRIT"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$7"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+53))
    messageList should be (List("Msg 5"))
    reset

    log.minLevel = "ERROR"

    log.trace("Msg 1") // ln+63
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    log.warn("Msg 10") // ln+71
    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    log.crit("Msg 11") // ln+79
    levelNameList should be (List("CRIT"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$7"))
    // methodNameList should be (List("apply$mcV$sp"))
    lineNumberList should be (List(ln+79))
    messageList should be (List("Msg 11"))
    reset
  }

  it should "Log multi thread buffered shared" in {
    reset


    val outMCS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }

    val LogMBS = new Log.Multi.BufferedShared {
      def makeOutObject(threadId: Long): Out = outMCS
    }
    LogMBS.minLevel = TRACE

    val conductor1 = new Conductor

    conductor1.thread("t1") {
      LogMBS.notice("Msg50") // ln+3
      LogMBS.cleanup
    }
    conductor1.whenFinished {
      levelNameList should be (List())
      classNameList should be (List())
      methodNameList should be (List())
      lineNumberList should be (List())
      messageList should be (List())
    }
    LogMBS.clear
    reset

    val conductor2 = new Conductor

    conductor2.thread("t2") {
      LogMBS.notice("Msg51") // ln+3
      LogMBS.flush
      LogMBS.cleanup
    }
    conductor2.whenFinished {
      levelNameList should be (List("NOTICE"))
      messageList should be (List("Msg51"))
    }
    reset

    val conductor3 = new Conductor

    conductor3.thread("t3") {
      LogMBS.info("Msg52")
      LogMBS.warn("Msg53")
      LogMBS.error("Msg54")
      LogMBS.flush
      LogMBS.cleanup
    }
    conductor3.whenFinished {
      levelNameList should be (List("ERROR","WARN","INFO"))
      messageList should be (List("Msg54","Msg53","Msg52"))
    }
    reset


    // NOTE: for this only the order of the flush matters
    val conductor4 = new Conductor

    conductor4.thread("t3") {
      LogMBS.info("Msg52")
      conductor4.waitForBeat(1)
      LogMBS.warn("Msg53")
      conductor4.waitForBeat(3)
      LogMBS.error("Msg54")
      LogMBS.flush
      LogMBS.cleanup
    }
    conductor4.thread("t4") {
      LogMBS.trace("Msg62")
      conductor4.waitForBeat(2)
      LogMBS.debug("Msg63")
      conductor4.waitForBeat(4)
      LogMBS.info("Msg64")
      LogMBS.flush
      LogMBS.cleanup
    }

    conductor4.whenFinished {
      levelNameList should be (List("INFO", "DEBUG", "TRACE", "ERROR", "WARN", "INFO"))
      messageList should be (List("Msg64", "Msg63", "Msg62", "Msg54", "Msg53", "Msg52"))
    }
    reset

    // NOTE: mixing flush commands
    val conductor5 = new Conductor

    conductor5.thread("t3") {
      LogMBS.info("Msg52")
      conductor5.waitForBeat(1)
      LogMBS.warn("Msg53")
      conductor5.waitForBeat(3)
      LogMBS.flush
      LogMBS.error("Msg54")
      LogMBS.flush
      LogMBS.cleanup
    }
    conductor5.thread("t4") {
      LogMBS.trace("Msg62")
      conductor5.waitForBeat(2)
      LogMBS.flush
      LogMBS.debug("Msg63")
      conductor5.waitForBeat(4)
      LogMBS.info("Msg64")
      LogMBS.flush
      LogMBS.cleanup
    }

    conductor5.whenFinished {
      levelNameList should be (List("INFO", "DEBUG", "ERROR", "WARN", "INFO", "TRACE"))
      messageList should be (List("Msg64", "Msg63", "Msg54", "Msg53", "Msg52", "Msg62"))
    }
    reset
  }

  it should "Log enter leave" in {
    reset

    val LogSIP = new Log.Single.ImmediatePrivate {
      protected def output(msg: Message): Unit = receive(msg)
    }
    LogSIP.minLevel = "TRACE"


    // if you add lines below this, you need to adjust linenumber test
    val ln = Thread.currentThread.getStackTrace()(1).getLineNumber

    def foo(i: Int): Unit = LogSIP.block {
      // Log.printStack(15)
      LogSIP.trace(s"foo.i=$i") // ln+2
    }
    def bar(i: Int): Unit = LogSIP.block("Bar:bar") {
      // Log.printStack(15)
      LogSIP.trace(s"bar.i=$i") // ln+2
    }

    levelNameList should be (List())
    classNameList should be (List())
    methodNameList should be (List())
    lineNumberList should be (List())
    messageList should be (List())
    reset

    foo(33)

    levelNameList should be (List("LEAVE", "TRACE", "ENTER"))
    // println(lineNumberList)
    // methodNameList should be (List("foo$1", "apply$mcV$sp", "foo$1"))
    // classNameList should be (List("com.megaannum.logging.logsf.LogSpec$$anonfun$8", "com.megaannum.logging.logsf.LogSpec$$anonfun$8$$anonfun$foo$1$1", "com.megaannum.logging.logsf.LogSpec$$anonfun$8"))
    // lineNumberList should be (List())
    // messageList should be (List("LEAVE: 2", "foo.i=33", "ENTER"))
    messageList.length should be (3)
    messageList(0)substring(0,7) should be ("LEAVE: ")
    messageList(1) should be ("foo.i=33")
    messageList(2) should be ("ENTER")
    reset

    bar(11)
    levelNameList should be (List("LEAVE", "TRACE", "ENTER"))
    messageList.length should be (3)
    messageList.length should be (3)
    messageList(0)substring(0,8) should be ("Bar:bar ")
    messageList(1) should be ("bar.i=11")
    messageList(2) should be ("Bar:bar")
    reset
  }

  it should "Log block" in {
    reset

    val LogSIP = new Log.Single.ImmediatePrivate {
      protected def output(msg: Message): Unit = receive(msg)
    }
    LogSIP.minLevel = "TRACE"

    def foo(i: Int): Unit = LogSIP.block("LogSpec:foo") {
      // Log.printStack(15)
      LogSIP.trace(s"foo.i=$i") // ln+2
    }
    def bar(i: Int): Unit = LogSIP.block("LogSIP:bar") {
      // Log.printStack(15)
      LogSIP.trace(s"bar.i=$i") // ln+2
    }

    foo(33)

    levelNameList should be (List("LEAVE", "TRACE", "ENTER"))
    // println(messageList)
    // println(labelOpList)
  }

  it should "flush single thread buffered private" in {
    reset
    val LogSBP = new Log.Single.BufferedPrivate {
      protected def output(msg: Message): Unit = receive(msg)
      override protected def flushAndClearOn(msg: Message): Boolean = msg.level >= Level.CRIT
    }
    
    LogSBP.minLevel = "INFO"

    LogSBP.trace("Msg trace")
    LogSBP.debug("Msg debug")
    LogSBP.info("Msg info")
    LogSBP.error("Msg error")
    messageList should be (List())

    LogSBP.flush
    messageList should be (List("Msg error", "Msg info"))

    LogSBP.clear
    reset

    LogSBP.trace("Msg trace")
    LogSBP.debug("Msg debug")
    LogSBP.info("Msg info")
    LogSBP.error("Msg error")
    LogSBP.flushAll
    messageList should be (List("Msg error", "Msg info","Msg debug", "Msg trace"))

    LogSBP.clear
    reset

    LogSBP.trace("Msg trace")
    LogSBP.debug("Msg debug")
    LogSBP.flushAll
    messageList should be (List("Msg debug", "Msg trace"))

    LogSBP.clear
    reset

    LogSBP.trace("Msg trace")
    LogSBP.debug("Msg debug")
    LogSBP.info("Msg info")
    LogSBP.error("Msg error")
    LogSBP.crit("Msg crit")
    messageList should be (List("Msg crit", "Msg error", "Msg info", "Msg debug", "Msg trace"))
  }

  it should "flush single thread buffered shared" in {
    reset
    val outSBS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }
    val LogSBS = new Log.Single.BufferedShared {
      override def out: Log.Out = outSBS
      override protected def flushAndClearOn(msg: Message): Boolean = msg.level >= Level.CRIT
    }
    
    LogSBS.minLevel = "INFO"

    LogSBS.trace("Msg trace")
    LogSBS.debug("Msg debug")
    LogSBS.info("Msg info")
    LogSBS.error("Msg error")
    messageList should be (List())

    LogSBS.flush
    messageList should be (List("Msg error", "Msg info"))

    LogSBS.clear
    reset

    LogSBS.trace("Msg trace")
    LogSBS.debug("Msg debug")
    LogSBS.info("Msg info")
    LogSBS.error("Msg error")
    LogSBS.flushAll
    messageList should be (List("Msg error", "Msg info","Msg debug", "Msg trace"))

    LogSBS.clear
    reset

    LogSBS.trace("Msg trace")
    LogSBS.debug("Msg debug")
    LogSBS.flushAll
    messageList should be (List("Msg debug", "Msg trace"))

    LogSBS.clear
    reset

    LogSBS.trace("Msg trace")
    LogSBS.debug("Msg debug")
    LogSBS.info("Msg info")
    LogSBS.error("Msg error")
    LogSBS.crit("Msg crit")
    messageList should be (List("Msg crit", "Msg error", "Msg info", "Msg debug", "Msg trace"))
  }

  it should "flush multi thread buffered private" in {
    reset

    def outFn(msg: Message): Unit = receive(msg)

    val LogMBP = new Log.Multi.BufferedPrivate {
      def makeOutFn(threadId: Long): Message => Unit = outFn
      override protected def flushAndClearOn(msg: Message): Boolean = msg.level >= Level.CRIT
    }
    
    LogMBP.minLevel = "INFO"

    LogMBP.trace("Msg trace")
    LogMBP.debug("Msg debug")
    LogMBP.info("Msg info")
    LogMBP.error("Msg error")
    messageList should be (List())

    LogMBP.flush
    messageList should be (List("Msg error", "Msg info"))

    LogMBP.clear
    reset

    LogMBP.trace("Msg trace")
    LogMBP.debug("Msg debug")
    LogMBP.info("Msg info")
    LogMBP.error("Msg error")
    LogMBP.get.flushAll
    messageList should be (List("Msg error", "Msg info","Msg debug", "Msg trace"))

    LogMBP.clear
    reset

    LogMBP.trace("Msg trace")
    LogMBP.debug("Msg debug")
    LogMBP.get.flushAll
    messageList should be (List("Msg debug", "Msg trace"))

    LogMBP.clear
    reset

    LogMBP.trace("Msg trace")
    LogMBP.debug("Msg debug")
    LogMBP.info("Msg info")
    LogMBP.error("Msg error")
    LogMBP.crit("Msg crit")
    messageList should be (List("Msg crit", "Msg error", "Msg info", "Msg debug", "Msg trace"))
  }

  it should "flush multi thread buffered shared" in {
    reset
    val outMCS = new Log.Out {
      def put(msg: Message): Unit = receive(msg)
    }

    val LogMBS = new Log.Multi.BufferedShared {
      def makeOutObject(threadId: Long): Out = outMCS
      override protected def flushAndClearOn(msg: Message): Boolean = msg.level >= Level.CRIT
    }
    
    LogMBS.minLevel = "INFO"

    LogMBS.trace("Msg trace")
    LogMBS.debug("Msg debug")
    LogMBS.info("Msg info")
    LogMBS.error("Msg error")
    messageList should be (List())

    LogMBS.flush
    messageList should be (List("Msg error", "Msg info"))

    LogMBS.clear
    reset

    LogMBS.trace("Msg trace")
    LogMBS.debug("Msg debug")
    LogMBS.info("Msg info")
    LogMBS.error("Msg error")
    LogMBS.get.flushAll
    messageList should be (List("Msg error", "Msg info","Msg debug", "Msg trace"))

    LogMBS.clear
    reset

    LogMBS.trace("Msg trace")
    LogMBS.debug("Msg debug")
    LogMBS.get.flushAll
    messageList should be (List("Msg debug", "Msg trace"))

    LogMBS.clear
    reset

    LogMBS.trace("Msg trace")
    LogMBS.debug("Msg debug")
    LogMBS.info("Msg info")
    LogMBS.error("Msg error")
    LogMBS.crit("Msg crit")
    messageList should be (List("Msg crit", "Msg error", "Msg info", "Msg debug", "Msg trace"))
  }

  it should "support force-as" in {
    reset
    val LogSIP = new Log.Single.BufferedPrivate {
      protected def output(msg: Message): Unit = receive(msg)
      override protected def flushAndClearOn(msg: Message): Boolean = msg.level >= Level.CRIT
    }

    LogSIP.minLevel = ALERT
    

    def foo(i: Int): Unit = LogSIP.mark("foo") {
      LogSIP.error("Msg error")
      LogSIP.forceAs(INFO, "Msg info")
      LogSIP.warn("Msg warn")
      LogSIP.force("Msg force")
    }

    foo(44)
    levelNameList should be (List("FORCE", "INFO"))
    messageList should be (List("Msg force", "Msg info"))

    LogSIP.clear
    reset

    LogSIP.forceEnabled = false

    foo(55)
    levelNameList should be (List())
    messageList should be (List())

    LogSIP.clear
    reset
  }

}

