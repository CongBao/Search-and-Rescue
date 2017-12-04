// Agent doctor in project rescuer

/* Initial beliefs and rules */

// -> [doctor] +vic_pos([pos(X, Y), ...])
// -> [doctor] +remain([pair(pos(X, Y), dir(D1, D2)), ...])
path([]).
vic_rescued(0).

/* Initial goals */

!start.

/* Plans */

+!start :  true
        <- .print("Waiting for scout to send data.").

/* Localization */

// Once scout send its detected data, use these data to reduce the number of possible cells we are located in
// (Left, Right, Front, Back, Victim)
+!data(L, R, F, B, V)[source(scout)] :  remain(M) & .length(M, Len) & Len > 1
                                     <- .print("There are ", Len, " possible status(es) left.");
                                        if (V > 0) {
                                            .print("Please check the victim at your place.");
                                            .send(scout, achieve, check(V));
                                            .wait(500);
                                        };
                                        localize(L, R, F, B, V, M); // -> [doctor] +remain([...])
                                        .wait(1000);
                                        ?remain(N);
                                        !explore(L, R, F, B, N).

// If there is only one possible cell in list, that's where the scout located  
// (_, _, _, reMain)            
+!explore(_, _, _, _, M)[source(self)] :  .length(M, Len) & Len == 1
                                       <- .nth(0, M, pair(pos(X, Y), dir(D1, D2)));
                                          .print("Pos: (", X, ",", Y, "), Dir: (", D1, ",", D2, ")");
                                          determine(X, Y, D1, D2). // -> [doctor] +pos(X, Y)

// If there are more than one possible cells in list, choose one side in the priority of front > left > right,
// and ask scout to explore further in this side
// (Left, Right, Front, reMain)
+!explore(L, R, F, B, M)[source(self)] :  .length(M, Len) & Len > 1
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
                                          }.

/* Path finding */

// After the scout find its location, find an optimal total path
// (X-axis, Y-axis)
+pos(X, Y)[source(percept)] :  path(P) & .empty(P)
                            <- .print("Now I know Scout is located in (", X, ",", Y, ")");
                               .abolish(pos(_, _));
                               find_path. // -> [doctor] +total_path([...])

// Once scout reports its new position, and the path is not empty,
// let scout check if there is victim to rescue and then go to next cell
// (X-axis, Y-axis)
+pos(X, Y)[source(scout)] :  path(P) & not .empty(P)
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

// After an optimal total path is found, let scout start moving
// (Path)
+total_path(P)[source(percept)] :  not .empty(P)
                                <- .print("An optimal path found.");
                                   -+path(P);
                                   !next(P).

// If the remaining path is empty, stop
// (Path)
+!next(P)[source(self)] :  .empty(P)
                        <- .print("Done.").

// If all victims are rescued, stop
// (Path)
+!next(P)[source(self)] :  vic_rescued(N) & N >= 3
                        <- .print("Done.").

// If the remaining path is not empty, find and notify scout the next cell to go
// (Path)
+!next(P)[source(self)] :  not .empty(P) & vic_rescued(N) & N < 3
                        <- .print("Remaining path: ", P);
                           .nth(0, P, pos(X, Y));
                           .print("Next, please go to (", X, ",", Y, ")");
                           .send(scout, achieve, at(X, Y)).
