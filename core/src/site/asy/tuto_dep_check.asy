
import "lib/git_history.asy" as git;

History history = git.History( "master" );

history.commit( 0, 0, "A", arr( "+\ \ \ parent" ), arr(), arr( "parent:\ 0.1.0-SNAPSHOT" ) );
history.commit( 0, 1, "B", arr( "+/- parent (add module)", "+\ \ \ alpha" ), arr( "parent/1.0.0", "alpha/1.0.0" ), arr( "parent:\ 1.0.0", "alpha:\ 1.0.0" ) );
history.commit( 0, 2, "C", arr( "+/- alpha" ), arr(), arr( "parent:\ 1.0.0", "alpha:\ 1.1.0-SNAPSHOT" ) );
history.commit( 0, 3, "D", arr( "+/- alpha" ), arr( "alpha/1.1.0" ), arr( "parent:\ 1.0.0", "alpha:\ 1.1.0" ) );
history.commit( 0, 4, "E", arr( "+/- parent" ), arr(), arr( "parent:\ 1.1.0-SNAPSHOT", "alpha:\ 1.2.0-SNAPSHOT" ) );

history.link( 0, 0, 0 ); // A -> B
history.link( 0, 1, 0 ); // B -> C
history.link( 0, 2, 0 ); // C -> D
history.link( 0, 3, 0 ); // D -> E

history.draw();

