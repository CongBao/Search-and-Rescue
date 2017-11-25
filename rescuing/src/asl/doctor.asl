// Agent doctor in project rescuer

/* Initial beliefs and rules */

path([]).

/* Initial goals */

!start.

/* Plans */

+!start : true <- .print("Waiting for scout to determine location.").
                         
+pos(X, Y)[source(scout)] : path(P) & not .empty(P)
                      <- .print("I know Scout is at (", X, ",", Y, ")");
                         .abolish(pos(_, _));
                         ?path([_|L]);
                         .abolish(path(_));
                         +path(L);
                         !next(L).
                         
+pos(X, Y)[source(scout)] : path(P) & .empty(P)
                          <- .print("I now know Scout is located in (", X, ",", Y, ")");
                             .abolish(pos(_, _));
                             find_path.
                         
+total_path(P)[source(_)] : not .empty(P)
                          <- .print("An optimal path found.");
                             .abolish(path(_));
                             +path(P);
                             !next(P).
                            
+!next(P) : .empty(P)
          <- .print("Done.").
+!next(P) : not .empty(P)
          <- .print("Remaining path: ", P);
             .nth(0, P, pos(X, Y));
             .print("Next, please go to (", X, ",", Y, ")");
             .send(scout, achieve, at(X, Y)).
