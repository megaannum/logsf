# logsf

scala simple flexible logger

----

# Scala Simple Flexible Logger

Nothing too special here.
I wanted a small, stand-alone logger for some Scala code
that could support 
  both single-threaded as well as multi-threaded application.


This logger is reasonably configurable. It has the following 3 
binary major modes: 
- Supports both Single Thread and Multi Thread applications.
- Allows for either Immediate output or a Buffering with flush to output.
- The output target may be Private (requiring no synchronized access)
   or Shared (even with external code doing the sharing).

It is the expectation that for applications that use only a single thread 
that a version of a Single Thread Log would be used. Similarly, for
multi-threaded applications, a Multi Thread Log would be used.
For a Single Thread Log there is no synchronization on outputting 
the log messages while a Multi Thread Log can be particularized 
with different synchronization strategies.

A Log can have its messages written out immediately (Immediate mode)
or they can be buffered (Buffered mode) and, generally, will only be 
written upon a call to flush.

== How to Log ==

In order to Log, the code must call one fo the Log's "logging" methods.

There is the generic loggging method which takes a Log.Level.
 
  Log.log(TRACE, "TRACE is the "lowest" log level")
  Log.log(NOTICE, "NOTICE is just below the WARN level, normal but worth noting.")
  Log.log(ALERT, """A severe situation requiring "prompt" action has occurred""")

Helper logging methods.

  trace("Large amounts for information")
  error("Something was not successful")
  crit("Important problems need to be addressed")

There is a "FORCE" log level that is used when a message should be logged
regardless of what kind of logger is used or what the current log level is.

  Log.force("""This message appears at "FORCE" level""")
  Log.forceAs(INFO, """This message appears at INFO level""")

When using a Buffered logger, messages are not output unless they are
flushed, unless the "flush" method is called.
Additionally, if the "clear: method is called, then ALL messages currently
being buffered are removed from the buffer and can no longer be logged.
Note that calling flush and clear on an Immediate Log does nothing.

  Log.crit"Important problems need to be addressed"
  Log.flush // above message is output
  Log.alert("Severe situation prompt action needed")
  Log.emerg("System is not usable")
  Log.clear // above messages are not output


== Example Usages ==
 
=== Support Code ===

Depending upon the characteristics of a Log instance, different
support code is needed. Below are some examples.

Receives a Log message and does something with it. Any necessary
synchronization has happened in the Log code.

  def receive(msg: Message): Unit = ???
  // one can also define a method to handle a List of Messages for bulk processing
  def receive(msgs: List[Message]): Unit = ???

An output object which the Log code uses to synchronizing with.

  val out = new Log.Out {
    def put(msg: Message): Unit = receive(msg)
    // also, can define
    // override def put(msgs: List[Message]): Unit = msgs.foreach(msg => receive(msg))
  }

This approach is used for Multi Threaded Log instances when each Thread's
Log instance will have its own output mechanism which needes to be
"closed" when the Thread ends.

  def outFn(msg: Message): Unit = receive(msg)
  def closeFn(): Unit = { do resource close here }

=== 8 Kinds of Log instances ===

Single Thread, Immediate (non-Buffered), Private output mechanism:

  val LogSIP = new Log.Single.ImmediatePrivate {
    protected def output(msg: Message): Unit = receive(msg)
  }

Single Thread, Immediate (non-Buffered), Shared output mechanism:

  val LogSIS = new Log.Single.ImmediateShared {
    override def out: Log.Out = out
  }

Single Thread, Buffered, Private output mechanism:

  val LogSBP = new Log.Single.BufferedPrivate {
    protected def output(msg: Message): Unit = receive(msg)
  }

Single Thread, Buffered, Shared output mechanism:

  val LogSBS = new Log.Single.BufferedShared {
    override def out: Log.Out = out
  }

Multi Thread, Immediate, Private output mechanism:

  val LogMIP = new Log.Multi.ImmediatePrivate {
    def makeOutFn(threadId: Long): Message => Unit = outFn
    override def makeCloseFn(threadId: Long): () => Unit = closeFn _
  }

Multi Thread, Immediate, Shared output mechanism:

  val LogMIS = new Log.Multi.ImmediateShared {
    def makeOutObject(threadId: Long): Out = out
  }

Multi Thread, Buffered, Private output mechanism:

  val LogMBP = new Log.Multi.BufferedPrivate {
    def makeOutFn(threadId: Long): Message => Unit = outFn
    override def makeCloseFn(threadId: Long): () => Unit = closeFn _
  }

Multi Thread, Buffered, Shared output mechanism:

  val LogMBS = new Log.Multi.BufferedShared {
    def makeOutObject(threadId: Long): Out = out
  }

== Example Tailorings ==

=== Minimum Log Level ===
  TBD

=== Enter/Leave Log Level ===
  TBD

=== Flush ===
  TBD

=== Mark ===
  TBD

=== Force ===

  override protected def flushAndClearOn(msg: Message): Boolean = 
    msg.level >= Level.WARN

== Log Blocks ==

  def block

  def mark

  def batch

  ifTrace ...
