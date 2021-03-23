
import "lib/git_history.asy" as git;

History history = git.History( "master", "release", "develop" );

history.commit( 0, 0, "000", arr( "Init commit" ), arr(), arr() );
history.commit( 2, 1, "001", arr( "+/- altair", "+/- deneb", "+/- vega" ), arr(), arr( "altair:\ 0.1.0-SNAPSHOT.develop", "deneb:\ 0.1.0-SNAPSHOT.develop", "vega:\ 0.1.0-SNAPSHOT.develop" ) );
history.commit( 2, 2, "002", arr( "+/- deneb" ), arr(), arr( "altair:\ 0.1.0-SNAPSHOT.develop", "deneb:\ 0.1.0-SNAPSHOT.develop", "vega:\ 0.1.0-SNAPSHOT.develop" ) );
history.commit( 1, 3, "003", arr( "+/- altair" ), arr(), arr( "altair:\ 0.1.0-SNAPSHOT.release", "deneb:\ 0.1.0-SNAPSHOT.release", "vega:\ 0.1.0-SNAPSHOT.release" ) );
history.commit( 0, 4, "004", arr( "+/- altair", "+/- deneb", "+/- vega" ), arr( "altair/1.0.0", "deneb/1.0.0", "vega/1.0.0" ), arr( "altair:\ 1.0.0", "deneb:\ 1.0.0", "vega:\ 1.0.0" ) );
history.commit( 2, 5, "005", arr( "+/- altair (cherry pick of 003)" ), arr(), arr( "altair:\ 0.1.0-SNAPSHOT.develop", "deneb:\ 0.1.0-SNAPSHOT.develop", "vega:\ 0.1.0-SNAPSHOT.develop" ) );
history.commit( 2, 6, "006", arr( "+/- vega" ), arr(), arr( "altair:\ 0.1.0-SNAPSHOT.develop", "deneb:\ 0.1.0-SNAPSHOT.develop", "vega:\ 0.1.0-SNAPSHOT.develop" ) );
history.commit( 0, 7, "007", arr( "+/- vega" ), arr( "vega/1.1.0" ), arr( "altair:\ 1.0.0", "deneb:\ 1.0.0", "vega:\ 1.1.0" ) );
history.commit( 2, 8, "008", arr( "+/- deneb" ), arr(), arr( "altair:\ 0.1.0-SNAPSHOT.develop", "deneb:\ 0.1.0-SNAPSHOT.develop", "vega:\ 0.1.0-SNAPSHOT.develop" ) );

history.link( 0, 0, 2 ); // 000 -> 001
history.link( 2, 1, 2 ); // 001 -> 002
history.link( 2, 2, 1 ); // 002 -> 003
history.link( 0, 0, 0 ); // 000 -> 004
history.link( 1, 3, 0 ); // 003 -> 004
history.link( 2, 2, 2 ); // 002 -> 005
history.link( 2, 5, 2 ); // 005 -> 006
history.link( 0, 4, 0 ); // 004 -> 007
history.link( 2, 6, 0 ); // 006 -> 007
history.link( 2, 6, 2 ); // 006 -> 008

history.draw();

