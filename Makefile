GRAAL_HOME := /usr/lib/jvm/java-8-graal
JAVA_HOME := $(GRAAL_HOME)
PATH := "$(JAVA_HOME)/bin:$(PATH)"
# Doubled $ is because of make which interpret $,
GRAAL_VERSION := $(shell java -version 2>&1 | sed -nre 's,^.*GraalVM CE ([0-9a-z\.-]+) .*$$,\1,p')

define sbt
	sbt -java-home "$(JAVA_HOME)" 'set graalVersion := "$(GRAAL_VERSION)"' $(1)
endef

.PHONY: debug
debug:
	@echo "GRAAL_VERSION=$(GRAAL_VERSION)"

.PHONY: clean
clean:
	$(call sbt,clean)

target/graalvm-native-image/example:
	$(call sbt,graalvm-native-image:packageBin)

target/example: target/graalvm-native-image/example
	cp "target/graalvm-native-image/example" "target/example"

.PHONY: package
package: target/example

.PHONY: run
run: target/example
	target/example

target/example.jar: target/scala-2.12/example.jar
	cp "target/scala-2.12/example.jar" "target/example.jar"

target/scala-2.12/example.jar:
	$(call sbt,assembly)

.PHONY: config
config: target/graal/jni-config.json target/graal/reflect-config.json target/graal/proxy-config.json target/graal/resource-config.json

target/graal/trace.json: target/example.jar
	mkdir -p "target/graal"
	java -agentlib:native-image-agent=output="target/graal/trace.json" -jar "target/example.jar"

target/graal/%-config.json: CONFIG_TYPE = $(shell basename "$@" | sed 's,-config.json,,g')
target/graal/%-config.json: target/graal/native-image-configure target/graal/trace.json
	target/graal/native-image-configure \
		process-trace \
			--$(CONFIG_TYPE)-output="target/graal/$(CONFIG_TYPE)-config.json" \
			"target/graal/trace.json"

target/graal/native-image-configure:
	mkdir -p "target/graal" && \
	cd "target/graal" && \
	native-image --tool:native-image-configure

target/graal/example: target/example.jar
	mkdir -p "target/graal" && \
	native-image \
		--no-server \
		-H:JNIConfigurationFiles="src/main/graal/jni-config.json" \
		-H:ReflectionConfigurationFiles="src/main/graal/reflect-config.json" \
		-H:DynamicProxyConfigurationFiles="src/main/graal/proxy-config.json" \
		-H:ResourceConfigurationFiles="src/main/graal/resource-config.json" \
		-jar "target/example.jar" \
		"target/graal/example"


.PHONY: native-package
native-package: target/graal/example

.PHONY: native-run
native-run: target/graal/example
	target/graal/example