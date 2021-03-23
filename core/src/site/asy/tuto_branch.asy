
import "lib/git_history.asy" as git;

History history = git.History( "master", "experimental/tiger" );

history.commit( 0, 4, "E", arr( "+/- parent" ), arr( "parent/1.1.0" ), arr( "parent:\ 1.1.0", "alpha:\ 1.2.0-SNAPSHOT" ) );
history.commit( 0, 5, "F", arr( "+/- parent (add module)", "+\ \ \ beta" ), arr(), arr( "parent:\ 1.1.0", "alpha:\ 1.2.0-SNAPSHOT", "beta:\ 1.0.0-SNAPSHOT" ) );

history.commit( 1, 6, "G", arr( "+/- alpha" ), arr(), arr( "parent:\ 1.1.0", "alpha:\ 1.2.0-SNAPSHOT.experimental-tiger", "beta:\ 1.0.0-SNAPSHOT.experimental-tiger" ) );
history.commit( 0, 7, "H", arr( "+/- alpha" ), arr(), arr( "parent:\ 1.1.0", "alpha:\ 1.2.0-SNAPSHOT", "beta:\ 1.0.0-SNAPSHOT" ) );

history.link( 0, 4, 0 ); // E -> F
history.link( 0, 5, 1 ); // F -> G
history.link( 0, 5, 0 ); // F -> H
history.link( 1, 6, 0 ); // G -> H

history.draw();

