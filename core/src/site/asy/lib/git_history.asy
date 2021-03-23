
import flowchart;

defaultpen( Courier() );

string[] arr( ... string[] elts ) {
  return elts;
}

pair TopLeftAlign = sqrtEpsilon*NE;
pair LeftAlign = sqrtEpsilon*E;
pair RightAlign = sqrtEpsilon*W;

real dx = 3;
real dy = 3;

pair O = (0,0);
pair X = (1,0);
pair Y = (0,1);

frame pack( pair align=2S ... frame[] frames ) {
  frame f;
  pair z;
  add( f, frames[0], z );
  for( int i = 1; i < frames.length; ++i ) {
    frame prev = frames[i-1];
    frame curr = frames[i];
    z += align + realmult( unit( align ), max( prev ) + min( curr ) );
    add( f, curr, z );
  }
  return f;
}

frame shiftCenter( frame f ) {
  return shift( -0.5*( max( f ) + min( f ) ) )*f;
}

frame badge( string label, pen p ) {
  frame f;
  roundbox( f, Label( label ), filltype = Fill( p ), above = false );
  return f;
}

frame badges( string [] labels, pair align, pen p ) {

  frame f;

  int N = labels.length;

  frame[] badges = new frame[ N ];
  for( int i = 0; i < N; ++i )
    badges[i] = align( badge( labels[i], p ), align );

  real h = (N-1)*dy;
  for( frame badge: badges )
    h += size( badge ).y;

  for( frame badge: badges ) {
    h -= size( badge ).y;
    add( f, badge, (0, h) );
    h -= dy;
  }

  return shiftCenter( f );

}

struct BoxedFrame {

  frame f;
  path box;
  pair origin;

  void operator init( frame f, path box, pair origin = (0,0) ) {
    this.f = f;
    this.box = box;
    this.origin = origin;
  }

}

BoxedFrame operator * ( transform t, BoxedFrame boxed ) {
  return BoxedFrame( t*boxed.f, t*boxed.box, t*boxed.origin );
}

BoxedFrame commitContent( string name, string[] diffs ) {

  frame[] diffframes = new frame[ diffs.length ];
  for( int i = 0; i < diffs.length; ++i ) {
    frame f;
    label( f, diffs[i], align = TopLeftAlign );
    diffframes[i] = f;
  }
  frame content = shiftCenter( pack( ... diffframes ) );

  frame header;
  label( header, name );
  header = shiftCenter( header );

  pair sContent = size( content );
  pair sHeader = size( header );

  real width = 2*dx + max( sContent.x, sHeader.x );
  real bottom = 2*dx + sContent.y;
  real top = 2*dx + sHeader.y;
  real height = bottom + top;

  frame f;

  add( f, header, 0.5*width*X + ( bottom + 0.5*top )*Y );
  add( f, content, 0.5*width*X + 0.5*bottom*Y );

  real r = 10;
  pair A = ( 0, r );
  pair B = ( 0, height - r );
  pair C = ( r, height );
  pair D = ( width - r, height );
  pair E = ( width, height - r );
  pair F = ( width, r );
  pair G = ( width - r, 0 );
  pair H = ( r, 0 );

  pair I = ( 0, bottom );
  pair J = ( width, bottom );

  frame headerFill;
  fill( headerFill, I -- B{up} .. {right}C -- D{right} .. {down}E -- J -- cycle, lightgray );
  prepend( f, headerFill );

  frame contentFill;
  fill( contentFill, A -- I -- J -- F{down} .. {left}G -- H{left} .. {up}cycle, white );
  prepend( f, contentFill );

  path border = A -- B{up} .. {right}C -- D{right} .. {down}E -- F{down} .. {left}G -- H{left} .. {up}cycle;
  draw( f, border );
  draw( f, I -- J );

  transform t = shift( -0.5*( max( f ) + min( f ) ) );

  return BoxedFrame( t*f, t*border );

}

BoxedFrame commit( string name, string[] diffs, string[] tags, string[] versions ) {

  BoxedFrame commit = commitContent( name, diffs );
  pair esp = ( 0.5 * size( commit.f ).x + 2 ) * X;

  frame tagsFrame = badges( tags, RightAlign, lightgreen );
  add( commit.f, shift( -esp )*align( tagsFrame, RightAlign ) );

  frame versionsFrame = badges( versions, LeftAlign, rgb( "AFEEEE" ) );
  add( commit.f, shift( esp )*align( versionsFrame, LeftAlign ) );

  return commit;

}

struct History {

  string[] branches;
  BoxedFrame[][] commits;
  frame F;

  void operator init( ... string[] branches ) {
    this.branches = branches;
    this.commits = new BoxedFrame[branches.length][];
  }

  void fill( int branch, int time ) {
    BoxedFrame[] array = commits[branch];
    while( !array.initialized( time ) )
      array.push( null );
  }

  void commit( int branch, int time, string name, string[] diffs, string[] tags, string[] versions ) {
    fill( branch, time );
    commits[branch][time] = commit( name, diffs, tags, versions );
  }

  void place() {

    int N = branches.length;

    int T = 0;
    for( BoxedFrame[] array: commits )
      T = max( T, array.length );
    for( int b = 0; b < N; ++b )
      fill( b, T );

    real[] maxLeft = array( N, 0 );
    real[] maxRight = array( N, 0 );
    real[] maxBottom = array( T, 0 );
    real[] maxTop = array( T, 0 );

    for( int b = 0; b < N; ++b ) {
      for( int t = 0; t < T; ++t ) {

        if( commits[b][t] == null )
          continue;

        path box = commits[b][t].box;
        pair m = min( box );
        pair M = max( box );

        maxLeft[b] = max( maxLeft[b], -m.x );
        maxRight[b] = max( maxRight[b], M.x );
        maxBottom[t] = max( maxBottom[t], -m.y );
        maxTop[t] = max( maxTop[t], M.y );

      }
    }

    real[] branchX = new real[N];
    branchX[0] = 0;
    for( int b = 1; b < N; ++b )
      branchX[b] = branchX[b-1] + maxRight[b-1] + maxLeft[b] + 10;

    real[] timeY = new real[T];
    timeY[0] = 0;
    for( int t = 1; t < T; ++t )
      timeY[t] = timeY[t-1] + maxTop[t-1] + maxBottom[t] + 40;

    for( int b = 0; b < N; ++b ) {
      for( int t = 0; t < T; ++t ) {

        if( commits[b][t] == null )
          continue;

        commits[b][t] = shift( branchX[b], timeY[t] ) * commits[b][t];

        add( this.F, commits[b][t].f );

      }
    }

    real branchY = timeY[T-1] + maxTop[T-1] + 20;
    for( int b = 0; b < N; ++b ) {

      pair last = (0,0);
      for( int t = T-1; t >= 0; --t ) {
        if( commits[b][t] != null ) {
          last = commits[b][t].origin;
          break;
        }
      }

      frame f;
      draw( f, ( branchX[b], branchY ) -- last, mediumgray+1 );
      prepend( this.F, f );

      add( this.F, shift( branchX[b], branchY ) * badge( branches[b], lightyellow ) );

    }

  }

  void link( int fromBranch, int fromTime, int toBranch ) {

    if( empty( this.F ) )
      this.place();

    int toTime = 0;
    for( int t = fromTime + 1; t < commits[toBranch].length; ++t ) {
      if( commits[toBranch][t] != null ) {
        toTime = t;
        break;
      }
    }

    frame f;

    path p = commits[toBranch][toTime].origin -- commits[fromBranch][fromTime].origin;
    p = firstcut( p, commits[toBranch][toTime].box ).after;
    p = firstcut( p, commits[fromBranch][fromTime].box ).before;

    draw( f, p, arrow = Arrow, p = black+1 );

    prepend( this.F, f );

  }

  void draw() {

    if( empty( this.F ) )
      this.place();

    add( this.F );

  }

}

