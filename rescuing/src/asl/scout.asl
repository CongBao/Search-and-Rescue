// Agent scout in project rescuer

/* Initial beliefs and rules */

/* Initial goals */

!detect.

/* Localization */

// Detect surroundings
+!detect :  true
         <- detect. // -> [scout] +data(L, R, F, V, T)

// After data of obstacles and victims are collected, send them to doctor
// (Left, Right, Front, Victim, _)
+data(L, R, F, V, _)[source(percept)] :  true
                                      <- .print("Obstacles: [", L, ",", R, ",", F, "]; Victim: ", V);
                                         .send(doctor, achieve, data(L, R, F, V)).

// Explore one side following doctor's instructions
// (Side, reMain)
+!explore(S, M)[source(doctor)] :  true
                                <- .print("I will try to explore my ", S, " cell.");
                                   move(S, M). // -> [doctor] +remain([...]), [scout] +arrive(T)

// Once scout arrived, detect the new location          
+arrive(_)[source(percept)] :  true
                            <- !detect.

// Check and rescue victim
// (Victim)
+!check(V)[source(doctor)] :  true
                           <- .print("I'll check my place.");
                              check_vic(V). // -> [doctor] +vic_rescued(N)

/* Path finding */

// Tell doctor where we are
// (X-axis, Y-axis)
+!pos(X, Y)[source(self)] :  pos(X, Y) 
                          <- .print("My position is: (", X, ",", Y, ")");
                             .send(doctor, tell, pos(X, Y)).

// Reach some places following doctor's instructions
// (X-axis, Y-axis)
+!at(X, Y)[source(doctor)] :  true 
                           <- .print("The next cell I will travel to: (", X, ",", Y, ")");
                              travel(X, Y). // -> [scout] +at(X, Y, T)

// Once scout arrived, inform the position to doctor
// (X-axis, Y-axis, _)
+at(X, Y, _)[source(percept)] :  true
                              <- -+pos(X, Y);
                                 !pos(X, Y).

// Check and rescue victim
// (X-axis, Y-axis)
+!check(X, Y)[source(doctor)] :  pos(X, Y)
                              <- .print("Checking position: (", X, ",", Y, ")");
                                 check_vic(X, Y). // -> [doctor] +vic_rescued(N)
