// Agent scout in project rescuer

/* Initial beliefs and rules */

at(P) :- pos(P, X, Y) & pos(scout, X, Y).

/* Initial goals */

!start.

//!check(cell).

/* Plans */

/*+!check(cell) : not victim(scout)
    <- next(cell);
        !check(cell).
+!check(cell).*/


+!start : true <- .print("hello world.").