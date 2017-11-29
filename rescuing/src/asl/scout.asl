// Agent scout in project rescuer

/* Initial beliefs and rules */

/* Initial goals */

!detect.

/* Localization */

// Detect surroundings
+!detect : true
         <- detect; // -> +data(L, R, F, V)
            !data.

// After data of obstacles and victims are collected, send them to doctor
+!data : data(L, R, F, V)
       <- .print("Obstacles: [", L, ",", R, ",", F, "]; Victim: ", V);
          .send(doctor, achieve, data(L, R, F, V));
          -data(_, _, _, _).

// Explore one side following doctor's instructions
+!explore(S, M)[source(doctor)] : true
                                <- .print("I will try to explore my ", S, " cell.");
                                   move(S, M);
                                   .wait(500);
                                   !detect.

/* Path finding */

// Tell doctor where we are
+!pos(X, Y) : pos(X, Y) 
            <- .print("My position is: (", X, ",", Y, ")");
               .send(doctor, tell, pos(X, Y)).

// Reach some places following doctor's instructions
+!at(X, Y)[source(doctor)] : true 
                           <- .print("The next cell I will travel to: (", X, ",", Y, ")");
                              travel(X, Y);
                              -+pos(X, Y);
                              !pos(X, Y).

// Check and rescue victim
+!check(X, Y)[source(doctor)] : pos(X, Y)
                              <- .print("Checking position: (", X, ",", Y, ")");
                                 check_vic(X, Y).

+!rescue[source(doctor)] : true
                         <- .print("I'll rescue this victim.");
                            rescue. // TODO add parameter of victim value, and implement in java