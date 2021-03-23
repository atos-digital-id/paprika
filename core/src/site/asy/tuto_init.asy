
import "lib/git_history.asy" as git;

History history = git.History( "master" );

history.commit( 0, 0, "A", arr( "+\ \ \ parent" ), arr(), arr( "parent:\ 0.1.0-SNAPSHOT" ) );
history.commit( 0, 1, "B", arr( "+/- parent (add module)", "+\ \ \ alpha" ), arr(), arr( "parent:\ 0.1.0-SNAPSHOT", "alpha:\ 0.1.0-SNAPSHOT" ) );

history.link( 0, 0, 0 ); // A -> B

history.draw();

