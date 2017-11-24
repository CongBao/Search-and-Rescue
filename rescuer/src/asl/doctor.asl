// Agent doctor in project rescuer

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true
    <- for (.member(X, [a, b, c])) {
            .print(X);
        }.
