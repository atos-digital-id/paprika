package io.github.atos_digital_id.paprika.utils.log;

import java.util.Deque;
import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.logging.Logger;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Paprika logger wrapper.
 **/
@Named
@Singleton
public class PaprikaLogger {

  /**
   * Environment variable name to set for trigger the display of the logs.
   **/
  public static final String VARENV_LOGGER = "PAPRIKA_LOGS";

  private final Logger logger;

  private final boolean disabled;

  private final boolean debug;

  @Data
  private static class LogMessage {

    private final String message;

    private final Object[] args;

    @Getter( value = AccessLevel.PRIVATE, lazy = true )
    @EqualsAndHashCode.Exclude
    private final String string = format();

    private String format() {

      StringBuilder builder = new StringBuilder();

      int i = 0, off = 0, next = 0;
      while( ( next = message.indexOf( "{}", off ) ) != -1 ) {
        builder.append( message.substring( off, next ) );
        off = next + 2;
        if( i < args.length )
          builder.append( args[i++] );
      }

      builder.append( message.substring( off, message.length() ) );

      return builder.toString();

    }

    @Override
    public String toString() {
      return this.getString();
    }

  }

  @Inject
  public PaprikaLogger( Logger logger ) {
    this.logger = logger;
    this.debug = logger.isDebugEnabled();
    String envValue = System.getenv( VARENV_LOGGER );
    this.disabled = !this.debug && ( envValue == null || envValue.isEmpty() );
  }

  private final ThreadLocal<Deque<Deque<LogMessage>>> contexts = new ThreadLocal<>() {

    @Override
    protected Deque<Deque<LogMessage>> initialValue() {
      return new LinkedList<>();
    }

  };

  private final ThreadLocal<Deque<LogMessage>> messageStack = new ThreadLocal<>() {

    @Override
    protected Deque<LogMessage> initialValue() {
      return new LinkedList<>();
    }

  };

  /**
   * Log a message. The placeholders {@code "{}"} are replaced by the result of
   * {@code toString} on each given argument (like log message format in slf4j).
   *
   * @param msg the log message.
   * @param args arguments referenced in the message.
   **/
  public void log( String msg, Object ... args ) {

    if( disabled )
      return;

    StringBuilder builder = new StringBuilder();
    builder.append( "[PAPRIKA] " );

    for( LogMessage log : messageStack.get() )
      builder.append( log );
    builder.append( new LogMessage( msg, args ) );

    String log = builder.toString();

    if( this.debug )
      logger.debug( log );
    else
      logger.info( log );

  }

  /**
   * Stack a message. Every logs after this call will be prepend by this
   * message.
   *
   * @param msg the log message to prepend.
   * @param args arguments referenced in the message.
   **/
  public void stack( String msg, Object ... args ) {
    if( disabled )
      return;
    LogMessage log = new LogMessage( msg, args );
    messageStack.get().addLast( log );
  }

  /**
   * Remove last stacked message.
   **/
  public void unstack() {
    if( disabled )
      return;
    messageStack.get().pollLast();
  }

  /**
   * Save the stacked messages and reset the stack with this only one message.
   *
   * @param msg the only one new message to stack.
   * @param args arguments referenced in the message.
   **/
  public void reset( String msg, Object ... args ) {
    if( disabled )
      return;
    contexts.get().addLast( messageStack.get() );
    messageStack.set( new LinkedList<>() );
    stack( msg, args );
  }

  /**
   * Restore the stack as it was before the last {@link PaprikaLogger#reset}
   * call.
   **/
  public void restore() {
    if( disabled )
      return;
    messageStack.set( contexts.get().pollLast() );
  }

}
