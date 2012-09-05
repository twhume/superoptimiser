To get started with the superoptimiser: 

1. Install Leinengen as per the instructions at https://github.com/technomancy/leiningen

2. Make a working directory and from the shell, do (what you type in black, sample
responses in grey):

$ git clone https://github.com/twhume/superoptimiser.git
Cloning into 'superoptimiser'...
remote: Counting objects: 1927, done.
remote: Compressing objects: 100% (477/477), done.
remote: Total 1927 (delta 1175), reused 1927 (delta 1175)
Receiving objects: 100% (1927/1927), 6.50 MiB | 48 KiB/s, done.
Resolving deltas: 100% (1175/1175), done.

$ cd superoptimiser/SuperOptimiser/

$ lein deps
Copying 40 files to /private/tmp/superoptimiser/SuperOptimiser/lib

$ lein run -m Drivers.Identity
Aug 22, 2012 8:15:02 AM sun.reflect.NativeMethodAccessorImpl invoke0 INFO: PASS IdentityTest.identity {:seq-num 0, :vars 1, :length 2, :code ((:iload_0) (:ireturn)), :jumps {}}
"Elapsed time: 292.711 msecs"
(nil)

That’s it; you’ve superoptimised the identity function and been shown a matching result.
Now try other functions (though some may take a long time to run; edit the source code
to define what length of sequence to search up to): Drivers.Abs, Drivers.Negate,
Drivers.Min, Drivers.Max, or Drivers.Signum;

It should then be easy to add code to superoptimise other target functions.  

You can find out more about the superoptimiser in my original dissertation, available at

https://docs.google.com/file/d/0B_8w6H4BG5E_TmxwbkRKRnhUM0k/edit

Please feel free to comment or ask questions here.

Tom Hume
twhume@gmail.com
