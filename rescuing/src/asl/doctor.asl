// Agent doctor in project rescuer

/* Initial beliefs and rules */

// vic_pos([pos(), ...]) // TODO change the format of vic_pos
// remain([pair(pos(), dir()), ...])
path([]).

/* Initial goals */

!start.

/* Plans */

+!start : true
        <- .print("Waiting for scout to send data.").

/* Localization */

// Once scout send its detected data, use these data to reduce the number of possible cells we are located in
+!data(L, R, F, V)[source(scout)] : remain(M) & .length(M, Len) & Len > 1
                                  <- .print("There are ", Len, " possible status(es) left.");
                                     -remain(_);
                                     if (V > 0) {
                                         .print("Please rescue the victim at your place.");
                                         //.send(scout, achieve, rescue);
                                         .wait(500);
                                     } else {
                                         // TODO if there's no victim in the possible place, just remove it
                                     }; // TODO: add victim value in vic_pos
                                     localize(L, R, F, V, M); // -> +remain([...])
                                     .wait(500);
                                     ?remain(N);
                                     !explore(L, R, F, N).

// If there is only one possible cell in list, that's where the scout located              
+!explore(_, _, _, M) : .length(M, Len) & Len == 1
                      <- .nth(0, M, pair(pos(X, Y), dir(D1, D2)));
                         .print("Pos: (", X, ",", Y, "), Dir: (", D1, ",", D2, ")");
                         -remain(_);
                         determine(X, Y).

// If there are more than one possible cells in list, choose one side in the priority of front > left > right,
// and ask scout to explore further in this side
+!explore(L, R, F, M) : .length(M, Len) & Len > 1
                      <- if (F == 0) {
                             .print("Please try to explore your front cell.");
                             .send(scout, achieve, explore(front, M));
                         } else {
                             if (L == 0) {
                                 .print("Please try to explore your left cell.");
                                 .send(scout, achieve, explore(left, M));
                             } else {
                                 if (R == 0) {
                                     .print("Please try to explore your right cell.");
                                     .send(scout, achieve, explore(right, M));
                                 };
                             };
                         };
                         -remain(_).

/* Path finding */

// Once scout reports its new position, and the path is not empty,
// let scout check if there is victim to rescue and then go to next cell
+pos(X, Y)[source(scout)] : path(P) & not .empty(P)
                          <- .print("I know Scout is at (", X, ",", Y, ")");
                             ?vic_pos(V);
                             if (.member(pos(X, Y), V)) {
                                 .print("Please check if there is a victim.");
                                 .send(scout, achieve, check(X, Y));
                                 .wait(500);
                                 .delete(pos(X, Y), V, R);
                                 -+vic_pos(R);
                             };
                             .abolish(pos(_, _));
                             ?path([_|L]);
                             -+path(L);
                             !next(L).

// After the scout find its location, find an optimal total path
+pos(X, Y)[source(percept)] : path(P) & .empty(P)
                            <- .print("Now I know Scout is located in (", X, ",", Y, ")");
                               .abolish(pos(_, _));
                               find_path. // -> +total_path([...]) // TODO: add parameter of victim

// After an optimal total path is found, let scout start moving
+total_path(P)[source(percept)] : not .empty(P)
                                <- .print("An optimal path found.");
                                   -+path(P);
                                   !next(P).

// If the remaining path is empty, stop
+!next(P) : .empty(P)
          <- .print("Done.").

// If the remaining path is not empty, find and notify scout the next cell to go
+!next(P) : not .empty(P)
          <- .print("Remaining path: ", P);
             .nth(0, P, pos(X, Y));
             .print("Next, please go to (", X, ",", Y, ")");
             .send(scout, achieve, at(X, Y)).
