In the world of software development your programs interact while executing with numerous systems and 
there exist only a few scenarios if something goes sideways.

Firstly you could investigate the problem and if the error is recoverable for example the resource 
your program requested is down or it's a network issue your program may decide to try calling the 
resource until it becomes alive again.

Secondly you can try to rollback all modifications your program made in external systems and return 
the error to client. That's good for you if all your external systems are able to work in a single 
transaction and you can just rollback all of them at once. Unfortunately we don't live in a perfect 
world and sometimes you need to rollback some of them manually if at all possible and another point 
is that during this action there is a chance to find your external resource unavailable or it can be 
a network issue again. In this case the only thing you can do is call the resource over and over again.

And it turned out to ensure proper execution of your program you need to be ready to repeat your 
external system call until it succeeds. In order to realise these retries you can use messaging 
systems like RabbitMQ or ActivMQ but going this way you make your application code more complex and 
verbose. To resolve the issue I started implementing Fabuco. 

Fabuco is designed as distributed HighLoad asynchronous frameWork for orders processing. In Fabuco 
you accumulate logic of your program execution in fabuco processes which get input parameters, do 
some actions like call other processes or external resources (fabuco performers) in parallel or 
sequentially and eventually return some results. An order is the task which the process has to fulfil 
and the order contains parameters for the process. Every order has own parameter key with defines order 
identity like passport id or mobile number.  

With Fabuco you're able to do the following:

1. To call numerous resources simultaneously and then collect and analyse their results in one place.
2. If some of the resources return errors you are able to retry them asynchronously for a while 
   without thread locking and using customised retry algorithms.
3. Simple code syntax. Each process is realized in one java class.
4. You can keep state in processes.
5. Fabuco process stores all their calls in persistent storage and in case fabuco node is down 
   another node continues to process unfinished work.
6. Horizontal scalability.
7. Orders have customised priorities.
8. Orders related to different processes and grouped by parameter key can be assembled in one 
   sorted group and they will be sorted by order date before processing.
   
At the moment Fabuco is under development and unfortunately some of the features above are not 
implemented yet.     
