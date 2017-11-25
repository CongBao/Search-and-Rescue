// Agent scout in project rescuer

/* Initial beliefs and rules */

/* Initial goals */

!localize.

/* Plans */

+!localize : true 
           <- localize;
              !pos(_, _).

+!pos(X, Y) : pos(X, Y) 
            <- .print("My position is: (", X, ",", Y, ")");
               .send(doctor, tell, pos(X, Y)).

+!at(X, Y)[source(doctor)] : true 
                           <- .print("The next cell I will travel to: (", X, ",", Y, ")");
                              .abolish(pos(_, _));
                              travel(X, Y);
                              +pos(X, Y);
                              !pos(X, Y).
               
+!check(X, Y)[source(doctor)] : pos(X, Y)
                              <- .print("Checking position: (", X, ",", Y, ")");
                                 check_vic(X, Y).
