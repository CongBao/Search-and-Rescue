// Agent doctor in project rescuer

/* Initial beliefs and rules */

path([]).

/* Initial goals */

!start.

/* Plans */

+!start : true 
        <- .print("Waiting for scout to determine location.").

// once scout reports its new position, and the path is not empty,
// let scout check if there is victim to rescue and then go to next cell
+pos(X, Y)[source(scout)] : path(P) & not .empty(P)
                          <- .print("I know Scout is at (", X, ",", Y, ")");
                             ?vic_pos(V);
                             if (.member(pos(X, Y), V)) {
                                 .print("Please check if there is a victim.");
                                 .send(scout, achieve, check(X, Y));
                                 .wait(500);
                                 ?vic_pos([_|R]);
                                 .abolish(vic_pos(_));
                                 +vic_pos(R);
                             };
                             .abolish(pos(_, _));
                             ?path([_|L]);
                             .abolish(path(_));
                             +path(L);
                             !next(L).

// after the scout find its location, find an optimal total path
+pos(X, Y)[source(scout)] : path(P) & .empty(P)
                          <- .print("I now know Scout is located in (", X, ",", Y, ")");
                             .abolish(pos(_, _));
                             find_path.

// after an optimal total path is found, let scout start moving
+total_path(P)[source(_)] : not .empty(P)
                          <- .print("An optimal path found.");
                             .abolish(path(_));
                             +path(P);
                             !next(P).

// if the remaining path is empty, stop
+!next(P) : .empty(P)
          <- .print("Done.").

// if the remaining path is not empty, find and notify scout the next cell to go
+!next(P) : not .empty(P)
          <- .print("Remaining path: ", P);
             .nth(0, P, pos(X, Y));
             .print("Next, please go to (", X, ",", Y, ")");
             .send(scout, achieve, at(X, Y)).
