
import "lib/git_history.asy" as git;

History history = git.History( "bugfix/stupid-bug", "master", "feature/kickass-feature" );

history.commit( 1, 0, "A", arr( "+/- parent", "+/- alpha" ), arr(), arr( "parent:\ 0.1.0-SNAPSHOT", "alpha:\ 0.1.0-SNAPSHOT" ) );
history.commit( 1, 1, "B", arr( "+/- alpha" ), arr( "parent/1.0.0", "alpha/1.0.0" ), arr( "parent:\ 1.0.0", "alpha:\ 1.0.0" ) );
history.commit( 1, 2, "C", arr( "+/- parent (add module)", "+/- beta" ), arr(), arr( "parent:\ 1.0.0", "alpha:\ 1.0.0", "beta:\ 0.1.0-SNAPSHOT" ) );
history.commit( 1, 3, "D", arr( "+/- beta" ), arr( "beta/1.0.0" ), arr( "parent:\ 1.0.0", "alpha:\ 1.0.0", "beta:\ 1.0.0" ) );
history.commit( 1, 4, "E", arr( "+/- alpha" ), arr(), arr( "parent:\ 1.0.0", "alpha:\ 1.1.0-SNAPSHOT", "beta:\ 1.1.0-SNAPSHOT" ) );
history.commit( 1, 5, "F", arr( "+/- alpha", "+/- beta" ), arr( "alpha/1.1.0", "beta/1.1.0" ), arr( "parent:\ 1.0.0", "alpha:\ 1.1.0", "beta:\ 1.1.0" ) );

history.commit( 2, 6, "G", arr( "+/- beta" ), arr(), arr( "parent:\ 1.0.0", "alpha:\ 1.1.0", "beta:\ 1.2.0-SNAPSHOT.feature/kickass-feature" ) );
history.commit( 2, 7, "H", arr( "+/- parent" ), arr(), arr( "parent:\ 1.1.0-SNAPSHOT.feature/kickass-feature", "alpha:\ 1.2.0-SNAPSHOT.feature/kickass-feature", "beta:\ 1.2.0-SNAPSHOT.feature/kickass-feature" ) );

history.commit( 0, 6, "I", arr( "+/- beta" ), arr( "beta/1.1.1" ), arr( "parent:\ 1.0.0", "alpha:\ 1.1.0", "beta:\ 1.1.1" ) );

history.commit( 1, 8, "J", arr( "merge feature branch" ), arr( "parent/1.1.0", "alpha/1.2.0" ), arr( "parent:\ 1.1.0", "alpha:\ 1.2.0", "beta:\ 1.2.0-SNAPSHOT" ) );
history.commit( 1, 9, "K", arr( "cherry pick of I", "+/- beta" ), arr( "beta/1.2.0" ), arr( "parent:\ 1.1.0", "alpha:\ 1.2.0", "beta:\ 1.2.0" ) );

history.link( 1, 0, 1 ); // A -> B
history.link( 1, 1, 1 ); // B -> C
history.link( 1, 2, 1 ); // C -> D
history.link( 1, 3, 1 ); // D -> E
history.link( 1, 4, 1 ); // E -> F

history.link( 1, 5, 0 ); // F -> I

history.link( 1, 5, 2 ); // F -> G
history.link( 2, 6, 2 ); // G -> H

history.link( 1, 5, 1 ); // F -> J
history.link( 2, 7, 1 ); // H -> J
history.link( 1, 8, 1 ); // J -> K

history.draw();

