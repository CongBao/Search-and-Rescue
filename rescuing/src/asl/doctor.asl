// Agent doctor in project rescuer

/* Initial beliefs and rules */

path([]).

/* Initial goals */

!start.

/* Plans */

+!start : true <- .print("Waiting for scout to determine location.").

+pos(X, Y)[source(scout)] : path(P) & .empty(P)
                      <- .print("I now know Scout is located in (", X, ",", Y, ")");
                         find_path.
                         
+pos(X, Y)[source(scout)] : path(P) & not .empty(P)
                      <- .print("I know Scout is at (", X, ",", Y, ")");
                         .nth(0, P, pos(X, Y));
                         !next(X, Y).
                         
+path(P)[source(_)] : not .empty(P)
                    <- .print("An optimal path found.");
                        .nth(0, P, pos(X, Y));
                        !next(X, Y).
                        
+!next(Xn, Yn) : path(P) & .empty(P)
               <- .print("Arrival").
+!next(Xn, Yn) : pos(Xc, Yc) & path([pos(Xc, Yc)|L])
               <- .abolish(path(_));
                  +path(L);
                  .nth(0, L, pos(X, Y));
                  .send(scout, achieve, at(X, Y));
                  !next(X, Y).
