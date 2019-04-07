# Native Image of Akka Stream application using GraalVM

## Usage
### Hints to fill `src/main/graal/*-config.json`
* `make config`
: Produce config files in `target/graal` which can be used to setup the config files actually used by `make native` (which should be put in `src/main/graal`) 

### Others
* `make run-native`
: Build (using `sbt assembly` and `native-image`) and run the produced binary
* `make clean`
: Run `sbt clean`, basically

## Resources
* https://github.com/vmencik/akka-graal-native
* https://github.com/oracle/graal/blob/master/substratevm/CONFIGURE.md