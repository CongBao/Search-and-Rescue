// Agent scout in project rescuer

/* Initial beliefs and rules */

/* Initial goals */

!detect.

/* Plans */

+!detect : true
         <- detect; // -> +data(L, R, F, V)
            !data.
            
+!data : data(L, R, F, V)
       <- .print("Obstacles: [", L, ",", R, ",", F, "]; Victim: ", V);
          .send(doctor, achieve, data(L, R, F, V));
          -data(_, _, _, _).

+!explore(S, M)[source(doctor)] : true
                                <- .print("I will try to explore my ", S, " cell.");
                                   move(S, M);
                                   .wait(500);
                                   !detect.

+!pos(X, Y) : pos(X, Y) 
            <- .print("My position is: (", X, ",", Y, ")");
               .send(doctor, tell, pos(X, Y)).

+!at(X, Y)[source(doctor)] : true 
                           <- .print("The next cell I will travel to: (", X, ",", Y, ")");
                              travel(X, Y);
                              -+pos(X, Y);
                              !pos(X, Y).
               
+!check(X, Y)[source(doctor)] : pos(X, Y)
                              <- .print("Checking position: (", X, ",", Y, ")");
                                 check_vic(X, Y).
