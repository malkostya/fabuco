# Distributed HighLoad asynchronous framework for orders processing

If you're reading this on github.com, please note that this is the readme
for the development version and that some features described here might
not yet have been implemented.

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

Fabuco is designed as distributed HighLoad asynchronous frameWork for order processing. In Fabuco 
you accumulate logic of your program execution in fabuco processes which get input parameters, do 
some actions like call other processes or external resources (fabuco performers) in parallel or 
sequentially and eventually return some results. An order is the task which the process has to fulfil 
and the order contains parameters for the process. Every order has own parameter key which defines order 
identity like passport id or mobile number. 

**Features:**

* To call numerous resources simultaneously and then collect and analyse their results in one place.
* If some of the resources return errors you are able to retry them asynchronously for a while 
  without thread locking and using customised retry algorithms.
* Simple code syntax. Every process is realized in one java class.
* You can keep state in fabuco process.
* Fabuco process stores all their calls in persistent storage and in case of fabuco node is down 
  another node continues to process unfinished work.
* Horizontal scalability.
* Orders have customised priorities.
* Orders related to different processes and grouped by parameter key can be assembled in one 
  sorted group and they will be sorted by order date before processing.
   
You can find more details about how to use fabuco in fabuco-examples module.

## License
Copyright 2018, Kostya Malko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
